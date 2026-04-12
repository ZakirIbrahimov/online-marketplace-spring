package com.marketplace.service;

import com.marketplace.domain.ListingStatus;
import com.marketplace.domain.MerchantApprovalStatus;
import com.marketplace.domain.MerchantProfile;
import com.marketplace.domain.Product;
import com.marketplace.domain.ProductImage;
import com.marketplace.domain.Role;
import com.marketplace.domain.User;
import com.marketplace.repository.MerchantProfileRepository;
import com.marketplace.repository.ProductRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductService {

    private static final int MAX_IMAGES = 5;

    private final ProductRepository productRepository;
    private final MerchantProfileRepository merchantProfileRepository;
    private final CurrentUserService currentUserService;
    private final Path uploadRoot;

    public ProductService(
            ProductRepository productRepository,
            MerchantProfileRepository merchantProfileRepository,
            CurrentUserService currentUserService,
            @Value("${marketplace.upload-dir}") String uploadDir) {
        this.productRepository = productRepository;
        this.merchantProfileRepository = merchantProfileRepository;
        this.currentUserService = currentUserService;
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Transactional(readOnly = true)
    public List<Product> merchantProducts(User merchant) {
        return productRepository.findByMerchantIdOrderByIdDesc(merchant.getId());
    }

    @Transactional(readOnly = true)
    public Product requireOwnedProduct(Long productId, User merchant) {
        Product p =
                productRepository.findById(productId).orElseThrow(() -> new BusinessException("Product not found."));
        if (!Objects.equals(p.getMerchant().getId(), merchant.getId())) {
            throw new BusinessException("Not your product.");
        }
        return p;
    }

    @Transactional
    public Product create(String title, String description, BigDecimal price, int stock, List<MultipartFile> images)
            throws IOException {
        User merchant = requireApprovedMerchant();
        validateListing(title, price, stock);
        Product p = new Product();
        p.setMerchant(merchant);
        p.setTitle(title.trim());
        p.setDescription(description != null ? description.trim() : null);
        p.setPrice(price);
        p.setStock(stock);
        p.setListingStatus(ListingStatus.PENDING_APPROVAL);
        productRepository.save(p);
        saveImages(p, images);
        return productRepository.save(p);
    }

    @Transactional
    public Product update(
            Long productId,
            String title,
            String description,
            BigDecimal price,
            int stock,
            List<MultipartFile> newImages,
            boolean replaceImages)
            throws IOException {
        User merchant = requireApprovedMerchant();
        Product p = requireOwnedProduct(productId, merchant);
        validateListing(title, price, stock);
        p.setTitle(title.trim());
        p.setDescription(description != null ? description.trim() : null);
        p.setPrice(price);
        p.setStock(stock);
        p.setListingStatus(ListingStatus.PENDING_APPROVAL);
        if (replaceImages) {
            p.getImages().clear();
            productRepository.saveAndFlush(p);
        }
        saveImages(p, newImages);
        return productRepository.save(p);
    }

    @Transactional
    public void delete(Long productId) {
        User merchant = requireApprovedMerchant();
        Product p = requireOwnedProduct(productId, merchant);
        for (ProductImage img : p.getImages()) {
            deleteFileIfExists(img.getFileName());
        }
        productRepository.delete(p);
    }

    private User requireApprovedMerchant() {
        User u = currentUserService.requireCurrentUser();
        if (!u.getRoles().contains(Role.MERCHANT)) {
            throw new BusinessException("Merchant access required.");
        }
        MerchantProfile mp = merchantProfileRepository
                .findByUserId(u.getId())
                .orElseThrow(() -> new BusinessException("Merchant profile missing."));
        if (mp.getApprovalStatus() != MerchantApprovalStatus.APPROVED) {
            throw new BusinessException("Your merchant account is not approved yet.");
        }
        return u;
    }

    private static void validateListing(String title, BigDecimal price, int stock) {
        if (title == null || title.isBlank()) {
            throw new BusinessException("Title is required.");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Invalid price.");
        }
        if (stock < 0) {
            throw new BusinessException("Stock cannot be negative.");
        }
    }

    private void saveImages(Product product, List<MultipartFile> images) throws IOException {
        if (images == null || images.isEmpty()) {
            return;
        }
        int start = product.getImages().size();
        if (start + images.size() > MAX_IMAGES) {
            throw new BusinessException("At most " + MAX_IMAGES + " images per product.");
        }
        Files.createDirectories(uploadRoot);
        int order = start;
        for (MultipartFile file : images) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            String orig = Objects.requireNonNullElse(file.getOriginalFilename(), "image");
            String ext = extension(orig);
            String stored = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
            Path target = uploadRoot.resolve(stored);
            file.transferTo(target.toFile());
            ProductImage pi = new ProductImage();
            pi.setFileName(stored);
            pi.setSortOrder(order++);
            product.addImage(pi);
        }
    }

    private void deleteFileIfExists(String fileName) {
        try {
            Path p = uploadRoot.resolve(fileName).normalize();
            if (p.startsWith(uploadRoot)) {
                Files.deleteIfExists(p);
            }
        } catch (IOException ignored) {
            // best effort
        }
    }

    private static String extension(String name) {
        int i = name.lastIndexOf('.');
        if (i < 0 || i == name.length() - 1) {
            return "";
        }
        return name.substring(i + 1).toLowerCase();
    }

    @Transactional
    public void adminSetListingStatus(Long productId, ListingStatus status) {
        Product p = productRepository.findById(productId).orElseThrow(() -> new BusinessException("Product not found."));
        if (!Set.of(ListingStatus.ACTIVE, ListingStatus.REJECTED, ListingStatus.REMOVED, ListingStatus.PENDING_APPROVAL)
                .contains(status)) {
            throw new BusinessException("Invalid status.");
        }
        p.setListingStatus(status);
        productRepository.save(p);
    }
}

package com.marketplace.service;

import com.marketplace.domain.Cart;
import com.marketplace.domain.CartItem;
import com.marketplace.domain.ListingStatus;
import com.marketplace.domain.Product;
import com.marketplace.domain.Role;
import com.marketplace.domain.User;
import com.marketplace.repository.CartRepository;
import com.marketplace.repository.ProductRepository;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CurrentUserService currentUserService;

    public CartService(
            CartRepository cartRepository, ProductRepository productRepository, CurrentUserService currentUserService) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public Optional<Cart> getCart(User user) {
        return cartRepository.findWithItemsByUserId(user.getId());
    }

    @Transactional
    public void addItem(Long productId, int quantity) {
        if (quantity <= 0) {
            throw new BusinessException("Quantity must be positive.");
        }
        User u = currentUserService.requireCurrentUser();
        if (!u.getRoles().contains(Role.SHOPPER)) {
            throw new BusinessException("Shopper role required for cart.");
        }
        Product p = productRepository
                .findById(productId)
                .orElseThrow(() -> new BusinessException("Product not found."));
        if (p.getListingStatus() != ListingStatus.ACTIVE) {
            throw new BusinessException("Product is not available.");
        }
        if (p.getStock() < quantity) {
            throw new BusinessException("Not enough stock.");
        }

        Cart cart = cartRepository
                .findByUserId(u.getId())
                .orElseGet(
                        () -> {
                            Cart c = new Cart();
                            c.setUser(u);
                            return cartRepository.save(c);
                        });

        for (CartItem existing : cart.getItems()) {
            if (!Objects.equals(
                    existing.getProduct().getMerchant().getId(), p.getMerchant().getId())) {
                throw new BusinessException(
                        "Your cart contains items from another seller. Remove them or checkout first.");
            }
        }

        Optional<CartItem> line =
                cart.getItems().stream().filter(ci -> ci.getProduct().getId().equals(productId)).findFirst();
        if (line.isPresent()) {
            int next = line.get().getQuantity() + quantity;
            if (next > p.getStock()) {
                throw new BusinessException("Not enough stock.");
            }
            line.get().setQuantity(next);
        } else {
            CartItem ci = new CartItem();
            ci.setProduct(p);
            ci.setQuantity(quantity);
            cart.addItem(ci);
        }
        cartRepository.save(cart);
    }

    @Transactional
    public void updateQuantity(Long cartItemId, int quantity) {
        User u = currentUserService.requireCurrentUser();
        Cart cart = cartRepository
                .findWithItemsByUserId(u.getId())
                .orElseThrow(() -> new BusinessException("Cart not found."));
        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Line not found."));
        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            Product p = productRepository
                    .findById(item.getProduct().getId())
                    .orElseThrow(() -> new BusinessException("Product not found."));
            if (p.getListingStatus() != ListingStatus.ACTIVE) {
                throw new BusinessException("Product is no longer available.");
            }
            if (quantity > p.getStock()) {
                throw new BusinessException("Not enough stock.");
            }
            item.setQuantity(quantity);
        }
        cartRepository.save(cart);
    }

    @Transactional
    public void clear(User user) {
        cartRepository.findByUserId(user.getId()).ifPresent(c -> {
            c.getItems().clear();
            cartRepository.save(c);
        });
    }
}

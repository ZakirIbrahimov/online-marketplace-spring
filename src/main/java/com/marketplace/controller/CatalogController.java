package com.marketplace.controller;

import com.marketplace.domain.ListingStatus;
import com.marketplace.domain.Product;
import com.marketplace.repository.ProductRepository;
import com.marketplace.repository.ProductSpecifications;
import com.marketplace.service.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class CatalogController {

    private final ProductRepository productRepository;

    public CatalogController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/products")
    public String list(
            @org.springframework.web.bind.annotation.RequestParam(required = false) String q,
            @PageableDefault(size = 12) Pageable pageable,
            Model model) {
        Page<Product> page = productRepository.findAll(ProductSpecifications.catalogVisible(q), pageable);
        model.addAttribute("page", page);
        model.addAttribute("q", q);
        return "products/list";
    }

    @GetMapping("/products/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Product p = productRepository.findById(id).orElseThrow(() -> new BusinessException("Product not found."));
        if (p.getListingStatus() != ListingStatus.ACTIVE || p.getStock() <= 0) {
            throw new BusinessException("Product not available.");
        }
        model.addAttribute("product", p);
        return "products/detail";
    }
}

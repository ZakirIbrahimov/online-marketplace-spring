package com.marketplace.controller;

import com.marketplace.domain.OrderStatus;
import com.marketplace.domain.User;
import com.marketplace.repository.CustomerOrderRepository;
import com.marketplace.repository.OrderStatusEventRepository;
import com.marketplace.service.BusinessException;
import com.marketplace.service.CurrentUserService;
import com.marketplace.repository.MerchantProfileRepository;
import com.marketplace.service.MerchantAnalyticsService;
import com.marketplace.service.OrderFulfillmentService;
import com.marketplace.service.ProductService;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/merchant")
public class MerchantController {

    private final CurrentUserService currentUserService;
    private final ProductService productService;
    private final CustomerOrderRepository customerOrderRepository;
    private final OrderStatusEventRepository orderStatusEventRepository;
    private final OrderFulfillmentService orderFulfillmentService;
    private final MerchantAnalyticsService merchantAnalyticsService;
    private final MerchantProfileRepository merchantProfileRepository;

    public MerchantController(
            CurrentUserService currentUserService,
            ProductService productService,
            CustomerOrderRepository customerOrderRepository,
            OrderStatusEventRepository orderStatusEventRepository,
            OrderFulfillmentService orderFulfillmentService,
            MerchantAnalyticsService merchantAnalyticsService,
            MerchantProfileRepository merchantProfileRepository) {
        this.currentUserService = currentUserService;
        this.productService = productService;
        this.customerOrderRepository = customerOrderRepository;
        this.orderStatusEventRepository = orderStatusEventRepository;
        this.orderFulfillmentService = orderFulfillmentService;
        this.merchantAnalyticsService = merchantAnalyticsService;
        this.merchantProfileRepository = merchantProfileRepository;
    }

    @GetMapping
    public String dashboard(Model model) {
        User u = currentUserService.requireCurrentUser();
        model.addAttribute(
                "profile",
                merchantProfileRepository.findByUserId(u.getId()).orElse(null));
        model.addAttribute("earnings", merchantAnalyticsService.earnings(u));
        model.addAttribute("products", productService.merchantProducts(u));
        return "merchant/dashboard";
    }

    @GetMapping("/products")
    public String products(Model model) {
        User u = currentUserService.requireCurrentUser();
        model.addAttribute("products", productService.merchantProducts(u));
        return "merchant/products";
    }

    @GetMapping("/products/new")
    public String newProductForm() {
        return "merchant/product-form";
    }

    @PostMapping("/products")
    public String create(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam BigDecimal price,
            @RequestParam int stock,
            @RequestParam(required = false) List<MultipartFile> images)
            throws IOException {
        productService.create(title, description, price, stock, images);
        return "redirect:/merchant/products";
    }

    @GetMapping("/products/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        User u = currentUserService.requireCurrentUser();
        model.addAttribute("product", productService.requireOwnedProduct(id, u));
        return "merchant/product-edit";
    }

    @PostMapping("/products/{id}")
    public String update(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam BigDecimal price,
            @RequestParam int stock,
            @RequestParam(required = false) List<MultipartFile> images,
            @RequestParam(defaultValue = "false") boolean replaceImages)
            throws IOException {
        productService.update(id, title, description, price, stock, images, replaceImages);
        return "redirect:/merchant/products";
    }

    @PostMapping("/products/{id}/delete")
    public String delete(@PathVariable Long id) {
        productService.delete(id);
        return "redirect:/merchant/products";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        User m = currentUserService.requireCurrentUser();
        model.addAttribute("orders", customerOrderRepository.findDistinctByMerchantId(m.getId()));
        return "merchant/orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        User m = currentUserService.requireCurrentUser();
        var order = customerOrderRepository
                .findWithDetailsById(id)
                .orElseThrow(() -> new BusinessException("Order not found."));
        boolean mine = order.getLines().stream().allMatch(l -> l.getMerchant().getId().equals(m.getId()));
        if (!mine) {
            throw new BusinessException("Order not found.");
        }
        model.addAttribute("order", order);
        model.addAttribute("events", orderStatusEventRepository.findByOrderIdOrderByCreatedAtAsc(id));
        return "merchant/order-detail";
    }

    @PostMapping("/orders/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam OrderStatus next) {
        orderFulfillmentService.merchantUpdateStatus(id, next);
        return "redirect:/merchant/orders/" + id;
    }
}

package com.marketplace.controller;

import com.marketplace.domain.ListingStatus;
import com.marketplace.domain.User;
import com.marketplace.repository.CustomerOrderRepository;
import com.marketplace.repository.MerchantProfileRepository;
import com.marketplace.repository.OrderStatusEventRepository;
import com.marketplace.repository.ProductRepository;
import com.marketplace.repository.UserRepository;
import com.marketplace.service.AdminMetricsService;
import com.marketplace.service.AdminUserService;
import com.marketplace.service.BusinessException;
import com.marketplace.service.DisputeService;
import com.marketplace.service.MerchantApprovalService;
import com.marketplace.service.ProductService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminMetricsService adminMetricsService;
    private final MerchantProfileRepository merchantProfileRepository;
    private final MerchantApprovalService merchantApprovalService;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final CustomerOrderRepository customerOrderRepository;
    private final OrderStatusEventRepository orderStatusEventRepository;
    private final DisputeService disputeService;
    private final UserRepository userRepository;
    private final AdminUserService adminUserService;

    public AdminController(
            AdminMetricsService adminMetricsService,
            MerchantProfileRepository merchantProfileRepository,
            MerchantApprovalService merchantApprovalService,
            ProductRepository productRepository,
            ProductService productService,
            CustomerOrderRepository customerOrderRepository,
            OrderStatusEventRepository orderStatusEventRepository,
            DisputeService disputeService,
            UserRepository userRepository,
            AdminUserService adminUserService) {
        this.adminMetricsService = adminMetricsService;
        this.merchantProfileRepository = merchantProfileRepository;
        this.merchantApprovalService = merchantApprovalService;
        this.productRepository = productRepository;
        this.productService = productService;
        this.customerOrderRepository = customerOrderRepository;
        this.orderStatusEventRepository = orderStatusEventRepository;
        this.disputeService = disputeService;
        this.userRepository = userRepository;
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("metrics", adminMetricsService.snapshot());
        return "admin/dashboard";
    }

    @GetMapping("/merchants/pending")
    public String pendingMerchants(Model model) {
        model.addAttribute(
                "profiles",
                merchantProfileRepository.findByApprovalStatusOrderByIdAsc(
                        com.marketplace.domain.MerchantApprovalStatus.PENDING));
        return "admin/merchants-pending";
    }

    @PostMapping("/merchants/{id}/approve")
    public String approveMerchant(@PathVariable Long id) {
        merchantApprovalService.approve(id);
        return "redirect:/admin/merchants/pending";
    }

    @PostMapping("/merchants/{id}/decline")
    public String declineMerchant(@PathVariable Long id, @RequestParam(required = false) String reason) {
        merchantApprovalService.decline(id, reason);
        return "redirect:/admin/merchants/pending";
    }

    @GetMapping("/products")
    public String products(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {
        model.addAttribute("page", productRepository.findAll(pageable));
        return "admin/products";
    }

    @PostMapping("/products/{id}/listing")
    public String setListing(@PathVariable Long id, @RequestParam ListingStatus status) {
        productService.adminSetListingStatus(id, status);
        return "redirect:/admin/products";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("orders", customerOrderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")));
        return "admin/orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        var order = customerOrderRepository
                .findWithDetailsById(id)
                .orElseThrow(() -> new BusinessException("Order not found."));
        model.addAttribute("order", order);
        model.addAttribute("events", orderStatusEventRepository.findByOrderIdOrderByCreatedAtAsc(id));
        return "admin/order-detail";
    }

    @GetMapping("/disputes")
    public String disputes(Model model) {
        model.addAttribute("disputes", disputeService.all());
        return "admin/disputes";
    }

    @PostMapping("/disputes/{id}/resolve")
    public String resolveDispute(@PathVariable Long id, @RequestParam(required = false) String notes) {
        disputeService.resolve(id, notes);
        return "redirect:/admin/disputes";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAll(Sort.by("email")));
        return "admin/users";
    }

    @PostMapping("/users/{id}/enabled")
    public String setEnabled(@PathVariable Long id, @RequestParam boolean enabled) {
        adminUserService.setEnabled(id, enabled);
        return "redirect:/admin/users";
    }
}

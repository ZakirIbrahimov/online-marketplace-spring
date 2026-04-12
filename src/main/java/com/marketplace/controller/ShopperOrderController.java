package com.marketplace.controller;

import com.marketplace.domain.CustomerOrder;
import com.marketplace.domain.User;
import com.marketplace.repository.CustomerOrderRepository;
import com.marketplace.repository.OrderStatusEventRepository;
import com.marketplace.service.BusinessException;
import com.marketplace.service.CurrentUserService;
import com.marketplace.service.DisputeService;
import com.marketplace.service.PaymentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/orders")
public class ShopperOrderController {

    private final CustomerOrderRepository customerOrderRepository;
    private final OrderStatusEventRepository orderStatusEventRepository;
    private final CurrentUserService currentUserService;
    private final PaymentService paymentService;
    private final DisputeService disputeService;

    public ShopperOrderController(
            CustomerOrderRepository customerOrderRepository,
            OrderStatusEventRepository orderStatusEventRepository,
            CurrentUserService currentUserService,
            PaymentService paymentService,
            DisputeService disputeService) {
        this.customerOrderRepository = customerOrderRepository;
        this.orderStatusEventRepository = orderStatusEventRepository;
        this.currentUserService = currentUserService;
        this.paymentService = paymentService;
        this.disputeService = disputeService;
    }

    @GetMapping
    public String list(Model model) {
        User u = currentUserService.requireCurrentUser();
        model.addAttribute("orders", customerOrderRepository.findByUserIdOrderByCreatedAtDesc(u.getId()));
        return "orders/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        User u = currentUserService.requireCurrentUser();
        CustomerOrder o = customerOrderRepository
                .findWithDetailsById(id)
                .orElseThrow(() -> new BusinessException("Order not found."));
        if (!o.getUser().getId().equals(u.getId())) {
            throw new BusinessException("Not your order.");
        }
        model.addAttribute("order", o);
        model.addAttribute("events", orderStatusEventRepository.findByOrderIdOrderByCreatedAtAsc(id));
        return "orders/detail";
    }

    @GetMapping("/{id}/pay")
    public String payForm(@PathVariable Long id, Model model) {
        User u = currentUserService.requireCurrentUser();
        CustomerOrder o = customerOrderRepository
                .findWithDetailsById(id)
                .orElseThrow(() -> new BusinessException("Order not found."));
        if (!o.getUser().getId().equals(u.getId())) {
            throw new BusinessException("Not your order.");
        }
        model.addAttribute("order", o);
        return "orders/pay";
    }

    @PostMapping("/{id}/pay")
    public String pay(@PathVariable Long id) {
        User u = currentUserService.requireCurrentUser();
        paymentService.completeMockPayment(id, u);
        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/disputes")
    public String dispute(@PathVariable Long id, @RequestParam String description) {
        disputeService.raise(id, description);
        return "redirect:/orders/" + id;
    }
}

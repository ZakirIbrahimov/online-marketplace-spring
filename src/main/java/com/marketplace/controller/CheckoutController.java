package com.marketplace.controller;

import com.marketplace.service.CartService;
import com.marketplace.service.CurrentUserService;
import com.marketplace.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final CartService cartService;
    private final CurrentUserService currentUserService;
    private final OrderService orderService;

    public CheckoutController(
            CartService cartService, CurrentUserService currentUserService, OrderService orderService) {
        this.cartService = cartService;
        this.currentUserService = currentUserService;
        this.orderService = orderService;
    }

    @GetMapping
    public String review(Model model) {
        var u = currentUserService.requireCurrentUser();
        var cart = cartService.getCart(u).orElse(null);
        model.addAttribute("cart", cart);
        return "checkout/review";
    }

    @PostMapping
    public String place() {
        var order = orderService.checkout();
        return "redirect:/orders/" + order.getId() + "/pay";
    }
}

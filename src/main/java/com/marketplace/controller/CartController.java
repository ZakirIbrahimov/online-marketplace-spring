package com.marketplace.controller;

import com.marketplace.service.CartService;
import com.marketplace.service.CurrentUserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final CurrentUserService currentUserService;

    public CartController(CartService cartService, CurrentUserService currentUserService) {
        this.cartService = cartService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public String view(Model model) {
        var u = currentUserService.requireCurrentUser();
        model.addAttribute("cart", cartService.getCart(u).orElse(null));
        return "cart/view";
    }

    @PostMapping("/items")
    public String add(@RequestParam Long productId, @RequestParam(defaultValue = "1") int quantity) {
        cartService.addItem(productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/items/{itemId}/quantity")
    public String update(@PathVariable Long itemId, @RequestParam int quantity) {
        cartService.updateQuantity(itemId, quantity);
        return "redirect:/cart";
    }
}

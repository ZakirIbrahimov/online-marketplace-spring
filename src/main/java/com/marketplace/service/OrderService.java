package com.marketplace.service;

import com.marketplace.domain.Cart;
import com.marketplace.domain.CartItem;
import com.marketplace.domain.CustomerOrder;
import com.marketplace.domain.ListingStatus;
import com.marketplace.domain.OrderLine;
import com.marketplace.domain.OrderStatus;
import com.marketplace.domain.OrderStatusEvent;
import com.marketplace.domain.Payment;
import com.marketplace.domain.PaymentProvider;
import com.marketplace.domain.PaymentStatus;
import com.marketplace.domain.Product;
import com.marketplace.domain.Role;
import com.marketplace.domain.User;
import com.marketplace.repository.CartRepository;
import com.marketplace.repository.CustomerOrderRepository;
import com.marketplace.repository.ProductRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final CartRepository cartRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final ProductRepository productRepository;
    private final CurrentUserService currentUserService;

    public OrderService(
            CartRepository cartRepository,
            CustomerOrderRepository customerOrderRepository,
            ProductRepository productRepository,
            CurrentUserService currentUserService) {
        this.cartRepository = cartRepository;
        this.customerOrderRepository = customerOrderRepository;
        this.productRepository = productRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public CustomerOrder checkout() {
        User shopper = currentUserService.requireCurrentUser();
        if (!shopper.getRoles().contains(Role.SHOPPER)) {
            throw new BusinessException("Shopper role required.");
        }
        Cart cart = cartRepository
                .findWithItemsByUserId(shopper.getId())
                .orElseThrow(() -> new BusinessException("Cart is empty."));
        if (cart.getItems().isEmpty()) {
            throw new BusinessException("Cart is empty.");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : cart.getItems()) {
            Product p = productRepository
                    .findById(ci.getProduct().getId())
                    .orElseThrow(() -> new BusinessException("Product not found."));
            if (p.getListingStatus() != ListingStatus.ACTIVE) {
                throw new BusinessException("Product no longer available: " + p.getTitle());
            }
            if (p.getStock() < ci.getQuantity()) {
                throw new BusinessException("Not enough stock for: " + p.getTitle());
            }
            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }

        CustomerOrder order = new CustomerOrder();
        order.setUser(shopper);
        order.setTotalAmount(total);
        order.setStatus(OrderStatus.PENDING_PAYMENT);

        for (CartItem ci : cart.getItems()) {
            Product p = productRepository
                    .findById(ci.getProduct().getId())
                    .orElseThrow();
            OrderLine line = new OrderLine();
            line.setProduct(p);
            line.setProductTitle(p.getTitle());
            line.setUnitPrice(p.getPrice());
            line.setQuantity(ci.getQuantity());
            line.setMerchant(p.getMerchant());
            order.addLine(line);
        }

        Payment pay = new Payment();
        pay.setAmount(total);
        pay.setProvider(PaymentProvider.MOCK);
        pay.setStatus(PaymentStatus.INITIATED);
        pay.setOrder(order);
        order.setPayment(pay);

        appendEvent(order, OrderStatus.PENDING_PAYMENT, shopper, "Checkout created");

        customerOrderRepository.save(order);

        cart.getItems().clear();
        cartRepository.save(cart);

        return order;
    }

    public void appendEvent(CustomerOrder order, OrderStatus status, User actor, String note) {
        OrderStatusEvent ev = new OrderStatusEvent();
        ev.setStatus(status);
        ev.setUser(actor);
        ev.setNote(note);
        order.addStatusEvent(ev);
    }
}

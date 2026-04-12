package com.marketplace.service;

import com.marketplace.domain.CustomerOrder;
import com.marketplace.domain.OrderStatus;
import com.marketplace.domain.Payment;
import com.marketplace.domain.PaymentStatus;
import com.marketplace.domain.Product;
import com.marketplace.domain.User;
import com.marketplace.repository.CustomerOrderRepository;
import com.marketplace.repository.ProductRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MockPaymentService implements PaymentService {

    private final CustomerOrderRepository customerOrderRepository;
    private final ProductRepository productRepository;
    private final OrderService orderService;

    public MockPaymentService(
            CustomerOrderRepository customerOrderRepository,
            ProductRepository productRepository,
            OrderService orderService) {
        this.customerOrderRepository = customerOrderRepository;
        this.productRepository = productRepository;
        this.orderService = orderService;
    }

    @Override
    @Transactional
    public void completeMockPayment(Long orderId, User payer) {
        CustomerOrder order = customerOrderRepository
                .findWithDetailsById(orderId)
                .orElseThrow(() -> new BusinessException("Order not found."));
        if (!order.getUser().getId().equals(payer.getId())) {
            throw new BusinessException("Not your order.");
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException("Order is not awaiting payment.");
        }
        Payment pay = order.getPayment();
        if (pay == null || pay.getStatus() != PaymentStatus.INITIATED) {
            throw new BusinessException("Payment cannot be completed.");
        }

        for (var line : order.getLines()) {
            Product p = productRepository
                    .findById(line.getProduct().getId())
                    .orElseThrow(() -> new BusinessException("Product missing."));
            if (p.getStock() < line.getQuantity()) {
                throw new BusinessException("Insufficient stock: " + p.getTitle());
            }
            p.setStock(p.getStock() - line.getQuantity());
            productRepository.save(p);
        }

        pay.setStatus(PaymentStatus.SUCCEEDED);
        pay.setReference("MOCK-" + UUID.randomUUID());
        order.setStatus(OrderStatus.PAID);
        orderService.appendEvent(order, OrderStatus.PAID, payer, "Mock payment completed");
        customerOrderRepository.save(order);
    }
}

package com.marketplace.service;

import com.marketplace.domain.CustomerOrder;
import com.marketplace.domain.OrderStatus;
import com.marketplace.domain.Role;
import com.marketplace.domain.User;
import com.marketplace.repository.CustomerOrderRepository;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderFulfillmentService {

    private final CustomerOrderRepository customerOrderRepository;
    private final CurrentUserService currentUserService;
    private final OrderService orderService;

    public OrderFulfillmentService(
            CustomerOrderRepository customerOrderRepository,
            CurrentUserService currentUserService,
            OrderService orderService) {
        this.customerOrderRepository = customerOrderRepository;
        this.currentUserService = currentUserService;
        this.orderService = orderService;
    }

    @Transactional
    public void merchantUpdateStatus(Long orderId, OrderStatus next) {
        User merchant = currentUserService.requireCurrentUser();
        if (!merchant.getRoles().contains(Role.MERCHANT)) {
            throw new BusinessException("Merchant role required.");
        }
        CustomerOrder order = customerOrderRepository
                .findWithDetailsById(orderId)
                .orElseThrow(() -> new BusinessException("Order not found."));
        Long mid = merchant.getId();
        boolean onlyThisMerchant =
                order.getLines().stream().allMatch(l -> Objects.equals(l.getMerchant().getId(), mid));
        if (!onlyThisMerchant) {
            throw new BusinessException("This order includes other sellers; contact support.");
        }

        OrderStatus cur = order.getStatus();
        if (next == OrderStatus.SHIPPED && (cur == OrderStatus.PAID || cur == OrderStatus.PROCESSING)) {
            order.setStatus(OrderStatus.SHIPPED);
            orderService.appendEvent(order, OrderStatus.SHIPPED, merchant, "Marked shipped");
        } else if (next == OrderStatus.DELIVERED && cur == OrderStatus.SHIPPED) {
            order.setStatus(OrderStatus.DELIVERED);
            orderService.appendEvent(order, OrderStatus.DELIVERED, merchant, "Marked delivered");
        } else if (next == OrderStatus.PROCESSING && cur == OrderStatus.PAID) {
            order.setStatus(OrderStatus.PROCESSING);
            orderService.appendEvent(order, OrderStatus.PROCESSING, merchant, "Processing");
        } else {
            throw new BusinessException("Invalid status transition.");
        }
        customerOrderRepository.save(order);
    }
}

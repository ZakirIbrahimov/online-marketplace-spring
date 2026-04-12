package com.marketplace.service;

import com.marketplace.domain.CustomerOrder;
import com.marketplace.domain.Dispute;
import com.marketplace.domain.DisputeStatus;
import com.marketplace.domain.OrderStatus;
import com.marketplace.domain.User;
import com.marketplace.repository.CustomerOrderRepository;
import com.marketplace.repository.DisputeRepository;
import java.util.EnumSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DisputeService {

    private static final EnumSet<OrderStatus> ALLOWED = EnumSet.of(
            OrderStatus.PAID, OrderStatus.PROCESSING, OrderStatus.SHIPPED, OrderStatus.DELIVERED);

    private final DisputeRepository disputeRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final CurrentUserService currentUserService;

    public DisputeService(
            DisputeRepository disputeRepository,
            CustomerOrderRepository customerOrderRepository,
            CurrentUserService currentUserService) {
        this.disputeRepository = disputeRepository;
        this.customerOrderRepository = customerOrderRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public void raise(Long orderId, String description) {
        User u = currentUserService.requireCurrentUser();
        CustomerOrder order = customerOrderRepository
                .findById(orderId)
                .orElseThrow(() -> new BusinessException("Order not found."));
        if (!order.getUser().getId().equals(u.getId())) {
            throw new BusinessException("Not your order.");
        }
        if (!ALLOWED.contains(order.getStatus())) {
            throw new BusinessException("Disputes can be opened after payment.");
        }
        if (description == null || description.isBlank()) {
            throw new BusinessException("Please describe the issue.");
        }
        Dispute d = new Dispute();
        d.setOrder(order);
        d.setRaisedBy(u);
        d.setDescription(description.trim());
        d.setStatus(DisputeStatus.OPEN);
        disputeRepository.save(d);
    }

    @Transactional(readOnly = true)
    public java.util.List<Dispute> all() {
        return disputeRepository.findAllByOrderByIdDesc();
    }

    @Transactional
    public void resolve(Long disputeId, String notes) {
        User admin = currentUserService.requireCurrentUser();
        Dispute d = disputeRepository.findById(disputeId).orElseThrow(() -> new BusinessException("Not found."));
        d.setStatus(DisputeStatus.RESOLVED);
        d.setResolutionNotes(notes != null ? notes.trim() : null);
        d.setResolvedBy(admin);
        disputeRepository.save(d);
    }
}

package com.marketplace.repository;

import com.marketplace.domain.OrderStatusEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusEventRepository extends JpaRepository<OrderStatusEvent, Long> {

    List<OrderStatusEvent> findByOrderIdOrderByCreatedAtAsc(Long orderId);
}

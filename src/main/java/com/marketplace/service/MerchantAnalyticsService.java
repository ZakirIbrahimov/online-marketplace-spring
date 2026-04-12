package com.marketplace.service;

import com.marketplace.domain.OrderStatus;
import com.marketplace.domain.User;
import com.marketplace.repository.CustomerOrderRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MerchantAnalyticsService {

    private static final List<OrderStatus> REVENUE_STATUSES =
            List.of(OrderStatus.PAID, OrderStatus.PROCESSING, OrderStatus.SHIPPED, OrderStatus.DELIVERED);

    private final CustomerOrderRepository customerOrderRepository;

    public MerchantAnalyticsService(CustomerOrderRepository customerOrderRepository) {
        this.customerOrderRepository = customerOrderRepository;
    }

    @Transactional(readOnly = true)
    public BigDecimal earnings(User merchant) {
        BigDecimal b = customerOrderRepository.sumLineRevenueForMerchant(merchant.getId(), REVENUE_STATUSES);
        return b != null ? b : BigDecimal.ZERO;
    }
}

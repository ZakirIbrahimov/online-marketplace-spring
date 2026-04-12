package com.marketplace.service;

import com.marketplace.domain.ListingStatus;
import com.marketplace.domain.MerchantApprovalStatus;
import com.marketplace.domain.OrderStatus;
import com.marketplace.repository.CustomerOrderRepository;
import com.marketplace.repository.MerchantProfileRepository;
import com.marketplace.repository.ProductRepository;
import com.marketplace.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminMetricsService {

    private static final List<OrderStatus> REVENUE_STATUSES =
            List.of(OrderStatus.PAID, OrderStatus.PROCESSING, OrderStatus.SHIPPED, OrderStatus.DELIVERED);

    private final UserRepository userRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final MerchantProfileRepository merchantProfileRepository;
    private final ProductRepository productRepository;

    public AdminMetricsService(
            UserRepository userRepository,
            CustomerOrderRepository customerOrderRepository,
            MerchantProfileRepository merchantProfileRepository,
            ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.customerOrderRepository = customerOrderRepository;
        this.merchantProfileRepository = merchantProfileRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public AdminMetrics snapshot() {
        long users = userRepository.count();
        long orders = customerOrderRepository.count();
        BigDecimal revenue = customerOrderRepository.sumTotalAmountByStatusIn(REVENUE_STATUSES);
        if (revenue == null) {
            revenue = BigDecimal.ZERO;
        }
        long pendingMerchants = merchantProfileRepository.countByApprovalStatus(MerchantApprovalStatus.PENDING);
        long pendingProducts = productRepository.countByListingStatus(ListingStatus.PENDING_APPROVAL);
        return new AdminMetrics(users, orders, revenue, pendingMerchants, pendingProducts);
    }

    public record AdminMetrics(
            long userCount,
            long orderCount,
            BigDecimal revenue,
            long pendingMerchants,
            long pendingProducts) {}
}

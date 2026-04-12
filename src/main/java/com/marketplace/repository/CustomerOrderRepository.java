package com.marketplace.repository;

import com.marketplace.domain.CustomerOrder;
import com.marketplace.domain.OrderStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query(
            "SELECT DISTINCT ol.order FROM OrderLine ol WHERE ol.merchant.id = :merchantId ORDER BY ol.order.createdAt DESC")
    List<CustomerOrder> findDistinctByMerchantId(@Param("merchantId") Long merchantId);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM CustomerOrder o WHERE o.status IN :statuses")
    java.math.BigDecimal sumTotalAmountByStatusIn(@Param("statuses") List<OrderStatus> statuses);

    long countByStatusIn(List<OrderStatus> statuses);

    @Query(
            """
            SELECT COALESCE(SUM(ol.unitPrice * ol.quantity), 0) FROM OrderLine ol
            WHERE ol.merchant.id = :merchantId AND ol.order.status IN :statuses""")
    java.math.BigDecimal sumLineRevenueForMerchant(
            @Param("merchantId") Long merchantId, @Param("statuses") List<OrderStatus> statuses);

    @org.springframework.data.jpa.repository.EntityGraph(
            attributePaths = {"lines", "lines.product", "lines.merchant", "payment", "user"})
    Optional<CustomerOrder> findWithDetailsById(Long id);
}

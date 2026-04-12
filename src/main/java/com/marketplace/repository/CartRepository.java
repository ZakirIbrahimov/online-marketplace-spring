package com.marketplace.repository;

import com.marketplace.domain.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @EntityGraph(attributePaths = {"items", "items.product", "items.product.merchant"})
    Optional<Cart> findWithItemsByUserId(Long userId);

    Optional<Cart> findByUserId(Long userId);
}

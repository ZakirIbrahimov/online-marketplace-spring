package com.marketplace.repository;

import com.marketplace.domain.ListingStatus;
import com.marketplace.domain.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    List<Product> findByMerchantIdOrderByIdDesc(Long merchantId);

    long countByListingStatus(ListingStatus status);
}

package com.marketplace.repository;

import com.marketplace.domain.MerchantApprovalStatus;
import com.marketplace.domain.MerchantProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantProfileRepository extends JpaRepository<MerchantProfile, Long> {

    Optional<MerchantProfile> findByUserId(Long userId);

    List<MerchantProfile> findByApprovalStatusOrderByIdAsc(MerchantApprovalStatus status);

    long countByApprovalStatus(MerchantApprovalStatus status);
}

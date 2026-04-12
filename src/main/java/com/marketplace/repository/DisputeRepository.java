package com.marketplace.repository;

import com.marketplace.domain.Dispute;
import com.marketplace.domain.DisputeStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisputeRepository extends JpaRepository<Dispute, Long> {

    List<Dispute> findByStatusOrderByIdDesc(DisputeStatus status);

    List<Dispute> findAllByOrderByIdDesc();
}

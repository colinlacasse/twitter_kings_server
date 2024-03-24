package com.twittersfs.server.repos;

import com.twittersfs.server.entities.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentEntityRepo extends JpaRepository<PaymentEntity, Long> {
}

package com.utilitybilling.payment.repository;

import com.utilitybilling.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Persistence for payments. */
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByCustomerId(Long customerId);
    boolean existsByBillId(Long billId);
    boolean existsByRecordedById(Long userId);
}

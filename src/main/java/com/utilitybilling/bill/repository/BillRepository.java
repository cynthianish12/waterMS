package com.utilitybilling.bill.repository;

import com.utilitybilling.bill.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Persistence for bills. */
public interface BillRepository extends JpaRepository<Bill, Long> {
    boolean existsByMeterIdAndBillingMonthAndBillingYear(Long meterId, Integer month, Integer year);
    boolean existsByApprovedById(Long userId);
    List<Bill> findByCustomerId(Long customerId);
    Optional<Bill> findByBillReference(String billReference);
}

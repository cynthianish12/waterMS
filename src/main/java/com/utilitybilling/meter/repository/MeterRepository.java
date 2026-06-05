package com.utilitybilling.meter.repository;

import com.utilitybilling.meter.entity.Meter;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persistence for meters. */
public interface MeterRepository extends JpaRepository<Meter, Long> {
    boolean existsByMeterNumber(String meterNumber);
    boolean existsByCustomerId(Long customerId);
}

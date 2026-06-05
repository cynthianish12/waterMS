package com.utilitybilling.tariff.repository;

import com.utilitybilling.common.Status;
import com.utilitybilling.common.UtilityType;
import com.utilitybilling.tariff.entity.Tariff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Persistence for tariff versions. */
public interface TariffRepository extends JpaRepository<Tariff, Long> {
    Optional<Tariff> findTopByUtilityTypeOrderByVersionDesc(UtilityType utilityType);
    Optional<Tariff> findFirstByUtilityTypeAndStatusOrderByVersionDesc(UtilityType utilityType, Status status);
    List<Tariff> findByUtilityTypeAndStatus(UtilityType utilityType, Status status);
    Optional<Tariff> findFirstByUtilityTypeAndStatusAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqual(
            UtilityType utilityType, Status status, LocalDate from, LocalDate to);
    @Query("""
            SELECT t FROM Tariff t
            WHERE t.utilityType = :utilityType
              AND t.status = :status
              AND t.effectiveFrom <= :billingDate
              AND (t.effectiveTo IS NULL OR t.effectiveTo >= :billingDate)
            ORDER BY t.version DESC
            """)
    List<Tariff> findActiveTariffsForBillingDate(
            @Param("utilityType") UtilityType utilityType,
            @Param("status") Status status,
            @Param("billingDate") LocalDate billingDate);
}

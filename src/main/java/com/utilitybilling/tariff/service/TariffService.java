package com.utilitybilling.tariff.service;

import com.utilitybilling.common.Status;
import com.utilitybilling.common.TariffType;
import com.utilitybilling.common.UtilityType;
import com.utilitybilling.exception.BusinessException;
import com.utilitybilling.exception.ResourceNotFoundException;
import com.utilitybilling.tariff.dto.TariffDtos.*;
import com.utilitybilling.tariff.entity.Tariff;
import com.utilitybilling.tariff.entity.TariffTier;
import com.utilitybilling.tariff.repository.TariffRepository;
import com.utilitybilling.user.entity.User;
import com.utilitybilling.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/** Manages versioned tariffs and validates tier ranges. */
@Service
@RequiredArgsConstructor
@Slf4j
public class TariffService {
    private final TariffRepository tariffs;
    private final UserRepository users;

    public TariffResponse create(TariffRequest r, String adminEmail) {
        if (r.effectiveFrom().isBefore(LocalDate.now())) throw new BusinessException("effectiveFrom cannot be in the past");
        if (!r.effectiveTo().isAfter(r.effectiveFrom())) throw new BusinessException("effectiveTo must be after effectiveFrom");
        validateActiveOverlap(r.utilityType(), r.effectiveFrom(), r.effectiveTo());
        validateTiers(r);
        int version = tariffs.findTopByUtilityTypeOrderByVersionDesc(r.utilityType()).map(t -> t.getVersion() + 1).orElse(1);
        User admin = users.findByEmail(adminEmail).orElse(null);
        Tariff tariff = Tariff.builder().utilityType(r.utilityType()).tariffType(r.tariffType()).pricePerUnit(r.pricePerUnit())
                .fixedServiceCharge(r.fixedServiceCharge()).vatPercentage(r.vatPercentage()).penaltyPercentage(r.penaltyPercentage())
                .effectiveFrom(r.effectiveFrom()).effectiveTo(r.effectiveTo()).version(version).status(Status.ACTIVE).createdBy(admin).build();
        if (r.tiers() != null) {
            r.tiers().forEach(t -> tariff.getTiers().add(TariffTier.builder().tariff(tariff)
                    .minConsumption(t.minConsumption()).maxConsumption(t.maxConsumption()).pricePerUnit(t.pricePerUnit()).build()));
        }
        Tariff saved = tariffs.save(tariff);
        return new TariffResponse(saved.getId(), saved.getUtilityType(), saved.getTariffType(), saved.getVersion(), saved.getStatus());
    }

    public Tariff applicable(UtilityType type, LocalDate billingDate) {
        List<Tariff> matches = tariffs.findActiveTariffsForBillingDate(type, Status.ACTIVE, billingDate);
        log.info("Tariff lookup utilityType={}, billingDate={}, matchingTariffs={}", type, billingDate, matches.size());
        if (!matches.isEmpty()) {
            return matches.get(0);
        }

        return tariffs.findFirstByUtilityTypeAndStatusOrderByVersionDesc(type, Status.ACTIVE)
                .map(tariff -> {
                    log.warn("No exact active {} tariff found for billingDate={}. Falling back to tariffId={}, version={}, effectiveFrom={}, effectiveTo={}",
                            type, billingDate, tariff.getId(), tariff.getVersion(), tariff.getEffectiveFrom(), tariff.getEffectiveTo());
                    return tariff;
                })
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active %s tariff found for billing date %s. Please create an ACTIVE tariff where effectiveFrom <= %s and effectiveTo >= %s."
                                .formatted(type, billingDate, billingDate, billingDate)));
    }

    public BigDecimal variableAmount(Tariff tariff, BigDecimal consumption) {
        if (tariff.getTariffType() == TariffType.FLAT) return consumption.multiply(tariff.getPricePerUnit());
        BigDecimal total = BigDecimal.ZERO;
        for (TariffTier tier : tariff.getTiers().stream().sorted(Comparator.comparing(TariffTier::getMinConsumption)).toList()) {
            BigDecimal start = tier.getMinConsumption();
            BigDecimal end = tier.getMaxConsumption();
            if (consumption.compareTo(start) > 0) {
                BigDecimal units = consumption.min(end).subtract(start).max(BigDecimal.ZERO);
                total = total.add(units.multiply(tier.getPricePerUnit()));
            }
        }
        return total;
    }

    public List<TariffResponse> all() {
        return tariffs.findAll().stream().map(t -> new TariffResponse(t.getId(), t.getUtilityType(), t.getTariffType(), t.getVersion(), t.getStatus())).toList();
    }

    private void validateActiveOverlap(UtilityType type, LocalDate from, LocalDate to) {
        for (Tariff tariff : tariffs.findByUtilityTypeAndStatus(type, Status.ACTIVE)) {
            LocalDate existingTo = tariff.getEffectiveTo() == null ? LocalDate.MAX : tariff.getEffectiveTo();
            if (!to.isBefore(tariff.getEffectiveFrom()) && !from.isAfter(existingTo)) {
                throw new BusinessException("Only one active tariff per utility type for the same billing cycle");
            }
        }
    }

    private void validateTiers(TariffRequest r) {
        if (r.tariffType() == TariffType.TIERED && (r.tiers() == null || r.tiers().isEmpty())) {
            throw new BusinessException("Tiered tariff requires tiers");
        }
        if (r.tiers() == null) return;
        List<TariffTierRequest> sorted = r.tiers().stream().sorted(Comparator.comparing(TariffTierRequest::minConsumption)).toList();
        BigDecimal previousMax = null;
        for (TariffTierRequest tier : sorted) {
            if (tier.minConsumption().compareTo(tier.maxConsumption()) >= 0) throw new BusinessException("Tier minConsumption must be less than maxConsumption");
            if (previousMax != null && tier.minConsumption().compareTo(previousMax) < 0) throw new BusinessException("Tier ranges must not overlap");
            previousMax = tier.maxConsumption();
        }
    }
}

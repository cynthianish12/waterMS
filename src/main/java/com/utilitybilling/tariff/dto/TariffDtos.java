package com.utilitybilling.tariff.dto;

import com.utilitybilling.common.Status;
import com.utilitybilling.common.TariffType;
import com.utilitybilling.common.UtilityType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Tariff API DTOs. */
public final class TariffDtos {
    private TariffDtos() {}
    public record TariffTierRequest(
            @Schema(description = "Tier minimum consumption", example = "0")
            @NotNull @PositiveOrZero BigDecimal minConsumption,
            @Schema(description = "Tier maximum consumption", example = "50")
            @NotNull @Positive BigDecimal maxConsumption,
            @Schema(description = "Tier price per unit", example = "400")
            @NotNull @PositiveOrZero BigDecimal pricePerUnit) {}
    public record TariffRequest(
            @Schema(description = "Utility type the tariff applies to", example = "WATER")
            @NotNull UtilityType utilityType,
            @Schema(description = "Tariff calculation type", example = "FLAT")
            @NotNull TariffType tariffType,
            @Schema(description = "Price charged per consumed unit", example = "500")
            @NotNull @PositiveOrZero BigDecimal pricePerUnit,
            @Schema(description = "Fixed service charge", example = "1000")
            @NotNull @PositiveOrZero BigDecimal fixedServiceCharge,
            @Schema(description = "VAT percentage", example = "18")
            @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal vatPercentage,
            @Schema(description = "Late payment penalty percentage", example = "5")
            @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal penaltyPercentage,
            @Schema(description = "Date this tariff starts applying", example = "2026-07-01")
            @NotNull LocalDate effectiveFrom,
            @Schema(description = "Date this tariff stops applying", example = "2026-12-31")
            @NotNull LocalDate effectiveTo,
            @Schema(description = "Tier ranges for tiered tariffs")
            @Valid List<TariffTierRequest> tiers) {}
    public record TariffResponse(Long id, UtilityType utilityType, TariffType tariffType, Integer version, Status status) {}
}

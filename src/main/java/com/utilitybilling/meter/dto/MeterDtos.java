package com.utilitybilling.meter.dto;

import com.utilitybilling.common.Status;
import com.utilitybilling.common.UtilityType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/** Meter API DTOs. */
public final class MeterDtos {
    private MeterDtos() {}
    public record MeterRequest(
            @Schema(description = "Unique meter number", example = "WM-10001")
            @NotBlank String meterNumber,
            @Schema(description = "Meter utility type", example = "WATER")
            @NotNull UtilityType meterType,
            @Schema(description = "Meter installation date", example = "2026-06-05")
            @NotNull @PastOrPresent LocalDate installationDate,
            @Schema(description = "Customer receiving this meter", example = "1")
            @NotNull Long customerId) {}
    public record MeterResponse(Long id, String meterNumber, UtilityType meterType, LocalDate installationDate,
                                Status status, Long customerId) {}
}

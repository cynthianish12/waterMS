package com.utilitybilling.reading.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Meter reading API DTOs. */
public final class MeterReadingDtos {
    private MeterReadingDtos() {}
    @Schema(example = """
            {
              "meterId": 1,
              "previousReading": 150,
              "currentReading": 220,
              "readingDate": "2026-06-05"
            }
            """)
    public record MeterReadingRequest(
            @Schema(description = "Meter being read", example = "1")
            @NotNull Long meterId,
            @Schema(description = "Previous meter reading", example = "150")
            @NotNull @PositiveOrZero BigDecimal previousReading,
            @Schema(description = "Current meter reading", example = "220")
            @NotNull @PositiveOrZero BigDecimal currentReading,
            @Schema(description = "Date reading was captured", example = "2026-06-05")
            @NotNull @PastOrPresent LocalDate readingDate,
            @Schema(description = "Reading month. Optional; defaults to the month in readingDate.", example = "6")
            @Min(1) @Max(12) Integer month,
            @Schema(description = "Reading year. Optional; defaults to the year in readingDate.", example = "2026")
            @Min(2000) Integer year) {
        public int normalizedMonth() {
            return month == null ? readingDate.getMonthValue() : month;
        }

        public int normalizedYear() {
            return year == null ? readingDate.getYear() : year;
        }
    }
    public record MeterReadingResponse(Long id, Long meterId, BigDecimal previousReading, BigDecimal currentReading,
                                       BigDecimal consumption, Integer month, Integer year) {}
}

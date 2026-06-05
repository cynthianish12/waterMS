package com.utilitybilling.bill.dto;

import com.utilitybilling.common.BillStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Bill API DTOs. */
public final class BillDtos {
    private BillDtos() {}
    public record GenerateBillRequest(
            @Schema(description = "Meter reading used to generate the bill", example = "1")
            @NotNull Long meterReadingId,
            @Schema(description = "Bill due date", example = "2026-07-05")
            @NotNull LocalDate dueDate) {}
    public record RejectBillRequest(
            @Schema(description = "Reason for rejecting the bill", example = "Incorrect meter reading")
            @jakarta.validation.constraints.NotBlank(message = "Rejection reason is required")
            String reason) {}
    public record BillResponse(Long id, String billReference, Long customerId, Long meterId, Integer billingMonth,
                               Integer billingYear, BigDecimal totalAmount, BigDecimal amountPaid,
                               BigDecimal outstandingBalance, BillStatus status) {}
}

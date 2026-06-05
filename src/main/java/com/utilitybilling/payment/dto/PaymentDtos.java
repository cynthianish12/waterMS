package com.utilitybilling.payment.dto;

import com.utilitybilling.common.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Payment API DTOs. */
public final class PaymentDtos {
    private PaymentDtos() {}
    @Schema(example = """
            {
              "billReference": "BILL-98EA4C4C",
              "amountPaid": 4000,
              "paymentMethod": "MOMO",
              "paymentDate": "2026-06-05"
            }
            """)
    public record PaymentRequest(
            @Schema(description = "Bill reference being paid", example = "BILL-2026-001")
            @NotBlank String billReference,
            @Schema(description = "Payment amount in FRW", example = "15000")
            @NotNull @Positive BigDecimal amountPaid,
            @Schema(description = "Payment method", example = "MOMO")
            @NotNull PaymentMethod paymentMethod,
            @Schema(description = "Payment date", example = "2026-06-05")
            @NotNull @PastOrPresent LocalDate paymentDate) {}
    public record PaymentResponse(Long id, String paymentReference, String billReference, BigDecimal amountPaid,
                                  PaymentMethod paymentMethod) {}
}

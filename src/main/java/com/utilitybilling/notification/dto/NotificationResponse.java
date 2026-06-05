package com.utilitybilling.notification.dto;

import com.utilitybilling.common.NotificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** Notification response sent to customers. */
public record NotificationResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "Bill Approved") String title,
        @Schema(example = "Dear Nishimwe Cynthia Marie, your June/2026 utility bill for meter WM-10001 has been approved. Total amount: 25,000 FRW. Due date: 2026-07-05.")
        String message,
        @Schema(example = "BILL-2026-001") String billReference,
        @Schema(example = "WM-10001") String meterNumber,
        @Schema(example = "UNREAD") NotificationStatus status,
        @Schema(example = "2026-06-05T12:30:00") LocalDateTime createdAt) {
}

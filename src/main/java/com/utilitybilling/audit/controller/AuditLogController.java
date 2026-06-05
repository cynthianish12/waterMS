package com.utilitybilling.audit.controller;

import com.utilitybilling.audit.entity.AuditLog;
import com.utilitybilling.audit.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/** Admin-only audit log endpoints. */
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Tag(name = "10. Audit Logs")
public class AuditLogController {
    private final AuditLogRepository auditLogs;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "View system audit logs. Allowed role: ROLE_ADMIN.")
    public List<AuditLogResponse> all() {
        return auditLogs.findAll().stream().map(AuditLogResponse::from).toList();
    }

    public record AuditLogResponse(Long id, String actionType, String performedBy, LocalDateTime performedAt,
                                   String oldValue, String newValue) {
        static AuditLogResponse from(AuditLog log) {
            return new AuditLogResponse(log.getId(), log.getActionType(), log.getPerformedBy(),
                    log.getPerformedAt(), log.getOldValue(), log.getNewValue());
        }
    }
}

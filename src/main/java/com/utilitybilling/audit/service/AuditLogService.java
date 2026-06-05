package com.utilitybilling.audit.service;

import com.utilitybilling.audit.entity.AuditLog;
import com.utilitybilling.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Writes audit records for important business actions. */
@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogs;

    public void log(String actionType, String performedBy, String oldValue, String newValue) {
        auditLogs.save(AuditLog.builder()
                .actionType(actionType)
                .performedBy(performedBy)
                .oldValue(oldValue)
                .newValue(newValue)
                .build());
    }
}

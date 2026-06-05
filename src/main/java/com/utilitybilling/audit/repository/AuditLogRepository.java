package com.utilitybilling.audit.repository;

import com.utilitybilling.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persistence for system audit logs. */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}

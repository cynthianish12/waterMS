package com.utilitybilling.audit.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/** Immutable record of an important business or security action. */
@Entity
@Table(name = "audit_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String actionType;
    private String performedBy;
    @CreationTimestamp
    private LocalDateTime performedAt;
    @Column(length = 2000)
    private String oldValue;
    @Column(length = 2000)
    private String newValue;
}

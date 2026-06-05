package com.utilitybilling.notification.entity;

import com.utilitybilling.bill.entity.Bill;
import com.utilitybilling.common.EmailStatus;
import com.utilitybilling.common.NotificationStatus;
import com.utilitybilling.customer.entity.Customer;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/** Customer-facing billing or payment notification. */
@Entity
@Table(name = "notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Customer customer;
    @ManyToOne
    private Bill bill;
    private String title;
    @Column(length = 1000)
    private String message;
    private String notificationType;
    @Enumerated(EnumType.STRING)
    private EmailStatus emailStatus;
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;
    @CreationTimestamp
    private LocalDateTime createdAt;
}

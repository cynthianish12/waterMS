package com.utilitybilling.customer.entity;

import com.utilitybilling.common.Status;
import com.utilitybilling.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/** Customer account that owns meters, bills, payments, and notifications. */
@Entity
@Table(name = "customers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_customer_national_id", columnNames = "nationalId"),
        @UniqueConstraint(name = "uk_customer_email", columnNames = "email"),
        @UniqueConstraint(name = "uk_customer_phone", columnNames = "phoneNumber")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Customer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fullName;
    private String nationalId;
    private String email;
    private String phoneNumber;
    private String address;
    @Enumerated(EnumType.STRING)
    private Status status;
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

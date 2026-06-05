package com.utilitybilling.payment.entity;

import com.utilitybilling.bill.entity.Bill;
import com.utilitybilling.common.PaymentMethod;
import com.utilitybilling.customer.entity.Customer;
import com.utilitybilling.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Payment transaction recorded against a bill. */
@Entity
@Table(name = "payments", uniqueConstraints = @UniqueConstraint(name = "uk_payment_reference", columnNames = "paymentReference"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String paymentReference;
    @ManyToOne(optional = false)
    private Bill bill;
    @ManyToOne(optional = false)
    private Customer customer;
    private BigDecimal amountPaid;
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    private LocalDate paymentDate;
    @ManyToOne
    private User recordedBy;
    @CreationTimestamp
    private LocalDateTime createdAt;
}

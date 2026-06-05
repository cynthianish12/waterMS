package com.utilitybilling.meter.entity;

import com.utilitybilling.common.Status;
import com.utilitybilling.common.UtilityType;
import com.utilitybilling.customer.entity.Customer;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Utility meter installed for an active customer. */
@Entity
@Table(name = "meters", uniqueConstraints = @UniqueConstraint(name = "uk_meter_number", columnNames = "meterNumber"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Meter {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String meterNumber;
    @Enumerated(EnumType.STRING)
    private UtilityType meterType;
    private LocalDate installationDate;
    @Enumerated(EnumType.STRING)
    private Status status;
    @ManyToOne(optional = false)
    private Customer customer;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

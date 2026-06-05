package com.utilitybilling.bill.entity;

import com.utilitybilling.common.BillStatus;
import com.utilitybilling.customer.entity.Customer;
import com.utilitybilling.meter.entity.Meter;
import com.utilitybilling.reading.entity.MeterReading;
import com.utilitybilling.tariff.entity.Tariff;
import com.utilitybilling.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Bill generated from a validated monthly meter reading. */
@Entity
@Table(name = "bills", uniqueConstraints = {
        @UniqueConstraint(name = "uk_bill_reference", columnNames = "billReference"),
        @UniqueConstraint(name = "uk_bill_meter_month_year", columnNames = {"meter_id", "billingMonth", "billingYear"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Bill {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String billReference;
    @ManyToOne(optional = false)
    private Customer customer;
    @ManyToOne(optional = false)
    private Meter meter;
    @OneToOne(optional = false)
    private MeterReading meterReading;
    @ManyToOne(optional = false)
    private Tariff tariff;
    private Integer billingMonth;
    private Integer billingYear;
    private BigDecimal consumption;
    private BigDecimal tariffAmount;
    private BigDecimal fixedCharge;
    private BigDecimal vatAmount;
    private BigDecimal penaltyAmount;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal outstandingBalance;
    @Enumerated(EnumType.STRING)
    private BillStatus status;
    private LocalDate dueDate;
    @ManyToOne
    private User approvedBy;
    private LocalDateTime approvedAt;
    @Column(length = 1000)
    private String rejectionReason;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

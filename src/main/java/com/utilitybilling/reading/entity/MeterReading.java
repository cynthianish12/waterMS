package com.utilitybilling.reading.entity;

import com.utilitybilling.meter.entity.Meter;
import com.utilitybilling.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Monthly meter reading captured by an operator. */
@Entity
@Table(name = "meter_readings", uniqueConstraints =
@UniqueConstraint(name = "uk_reading_meter_month_year", columnNames = {"meter_id", "reading_month", "reading_year"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MeterReading {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Meter meter;
    private BigDecimal previousReading;
    private BigDecimal currentReading;
    private BigDecimal consumption;
    private LocalDate readingDate;
    @Column(name = "reading_month")
    private Integer month;
    @Column(name = "reading_year")
    private Integer year;
    @ManyToOne
    private User capturedBy;
    @CreationTimestamp
    private LocalDateTime createdAt;
}

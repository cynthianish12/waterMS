package com.utilitybilling.tariff.entity;

import com.utilitybilling.common.Status;
import com.utilitybilling.common.TariffType;
import com.utilitybilling.common.UtilityType;
import com.utilitybilling.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Versioned utility price configuration used when bills are generated. */
@Entity
@Table(name = "tariffs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Tariff {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private UtilityType utilityType;
    @Enumerated(EnumType.STRING)
    private TariffType tariffType;
    private BigDecimal pricePerUnit;
    private BigDecimal fixedServiceCharge;
    private BigDecimal vatPercentage;
    private BigDecimal penaltyPercentage;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Integer version;
    @Enumerated(EnumType.STRING)
    private Status status;
    @ManyToOne
    private User createdBy;
    @OneToMany(mappedBy = "tariff", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TariffTier> tiers = new ArrayList<>();
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

package com.utilitybilling.tariff.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/** Consumption range price for tiered tariffs. */
@Entity
@Table(name = "tariff_tiers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TariffTier {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Tariff tariff;
    private BigDecimal minConsumption;
    private BigDecimal maxConsumption;
    private BigDecimal pricePerUnit;
}

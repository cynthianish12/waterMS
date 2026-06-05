package com.utilitybilling.meter.service;

import com.utilitybilling.common.Status;
import com.utilitybilling.customer.entity.Customer;
import com.utilitybilling.customer.service.CustomerService;
import com.utilitybilling.exception.BusinessException;
import com.utilitybilling.exception.DuplicateResourceException;
import com.utilitybilling.exception.ResourceNotFoundException;
import com.utilitybilling.meter.dto.MeterDtos.*;
import com.utilitybilling.meter.entity.Meter;
import com.utilitybilling.meter.repository.MeterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** Manages meters and installation rules. */
@Service
@RequiredArgsConstructor
public class MeterService {
    private final MeterRepository meters;
    private final CustomerService customers;

    public MeterResponse create(MeterRequest r) {
        if (meters.existsByMeterNumber(r.meterNumber())) throw new DuplicateResourceException("Meter number already exists");
        Customer customer = customers.get(r.customerId());
        if (customer.getStatus() != Status.ACTIVE) throw new BusinessException("Meter must belong to an active customer");
        if (customer.getCreatedAt() != null && r.installationDate().isBefore(customer.getCreatedAt().toLocalDate())) {
            throw new BusinessException("Installation date cannot be before customer creation date");
        }
        return toResponse(meters.save(Meter.builder().meterNumber(r.meterNumber()).meterType(r.meterType())
                .installationDate(r.installationDate()).status(Status.ACTIVE).customer(customer).build()));
    }

    public Meter get(Long id) {
        return meters.findById(id).orElseThrow(() -> new ResourceNotFoundException("Meter not found"));
    }

    public List<MeterResponse> all() {
        return meters.findAll().stream().map(this::toResponse).toList();
    }

    private MeterResponse toResponse(Meter m) {
        return new MeterResponse(m.getId(), m.getMeterNumber(), m.getMeterType(), m.getInstallationDate(),
                m.getStatus(), m.getCustomer().getId());
    }
}

package com.utilitybilling.customer.service;

import com.utilitybilling.common.Status;
import com.utilitybilling.customer.dto.CustomerDtos.*;
import com.utilitybilling.customer.entity.Customer;
import com.utilitybilling.customer.repository.CustomerRepository;
import com.utilitybilling.exception.DuplicateResourceException;
import com.utilitybilling.exception.ResourceNotFoundException;
import com.utilitybilling.meter.repository.MeterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** Manages customer registration, listing, and deactivation. */
@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customers;
    private final MeterRepository meters;

    public CustomerResponse create(CustomerRequest r) {
        if (customers.existsByNationalId(r.nationalId())) throw new DuplicateResourceException("National ID already exists");
        if (customers.existsByEmail(r.email())) throw new DuplicateResourceException("Customer email already exists");
        if (customers.existsByPhoneNumber(r.phoneNumber())) throw new DuplicateResourceException("Customer phone already exists");
        return toResponse(customers.save(Customer.builder().fullName(r.fullName()).nationalId(r.nationalId())
                .email(r.email()).phoneNumber(r.phoneNumber()).address(r.address()).status(Status.ACTIVE).build()));
    }

    public List<CustomerResponse> all() {
        return customers.findAll().stream().map(this::toResponse).toList();
    }

    public Customer get(Long id) {
        return customers.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    public void deactivate(Long id) {
        Customer c = get(id);
        c.setStatus(Status.INACTIVE);
        customers.save(c);
    }

    public void activate(Long id) {
        Customer c = get(id);
        c.setStatus(Status.ACTIVE);
        customers.save(c);
    }

    public void delete(Long id) {
        if (meters.existsByCustomerId(id)) throw new DuplicateResourceException("Customer has related records; deactivate instead");
        customers.delete(get(id));
    }

    public CustomerResponse toResponse(Customer c) {
        return new CustomerResponse(c.getId(), c.getFullName(), c.getNationalId(), c.getEmail(),
                c.getPhoneNumber(), c.getAddress(), c.getStatus(), c.getCreatedAt());
    }
}

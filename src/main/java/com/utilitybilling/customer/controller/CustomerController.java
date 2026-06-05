package com.utilitybilling.customer.controller;

import com.utilitybilling.bill.dto.BillDtos.BillResponse;
import com.utilitybilling.bill.service.BillService;
import com.utilitybilling.customer.dto.CustomerDtos.*;
import com.utilitybilling.customer.entity.Customer;
import com.utilitybilling.customer.repository.CustomerRepository;
import com.utilitybilling.customer.service.CustomerService;
import com.utilitybilling.exception.ResourceNotFoundException;
import com.utilitybilling.notification.dto.NotificationResponse;
import com.utilitybilling.notification.service.NotificationService;
import com.utilitybilling.payment.dto.PaymentDtos.PaymentResponse;
import com.utilitybilling.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/** Customer administration and customer self-service endpoints. */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "03. Customer Management")
public class CustomerController {
    private final CustomerService service;
    private final CustomerRepository customers;
    private final BillService bills;
    private final PaymentService payments;
    private final NotificationService notifications;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Hidden
    @Operation(summary = "Create customer. Allowed role: ROLE_ADMIN.")
    public CustomerResponse create(@Valid @RequestBody CustomerRequest request) {
        return service.create(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @Operation(summary = "List customers. Allowed roles: ROLE_ADMIN, ROLE_FINANCE.")
    public List<CustomerResponse> all() {
        return service.all();
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate customer instead of deleting related records. Allowed role: ROLE_ADMIN.")
    public void deactivate(@PathVariable Long id) {
        service.deactivate(id);
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate customer. Allowed role: ROLE_ADMIN.")
    public void activate(@PathVariable Long id) {
        service.activate(id);
    }

    @GetMapping("/my-bills")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "View own bills. Allowed role: ROLE_CUSTOMER.", tags = "07. Bill Management")
    public List<BillResponse> myBills(Principal principal) {
        return bills.byCustomer(currentCustomer(principal).getId());
    }

    @GetMapping("/my-payments")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "View own payment history. Allowed role: ROLE_CUSTOMER.", tags = "08. Payment Management")
    public List<PaymentResponse> myPayments(Principal principal) {
        return payments.byCustomer(currentCustomer(principal).getId());
    }

    @GetMapping("/my-notifications")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "View own notifications. Allowed role: ROLE_CUSTOMER.", tags = "09. Notifications")
    public List<NotificationResponse> myNotifications(Principal principal) {
        return notifications.byCustomer(currentCustomer(principal).getId());
    }

    private Customer currentCustomer(Principal principal) {
        return customers.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("No customer profile linked to this user email"));
    }
}

package com.utilitybilling.payment.controller;

import com.utilitybilling.payment.dto.PaymentDtos.*;
import com.utilitybilling.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/** Payment recording endpoint. */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "08. Payment Management")
public class PaymentController {
    private final PaymentService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('FINANCE','CUSTOMER')")
    @Operation(summary = "Record payment and update bill balance. Allowed roles: ROLE_FINANCE, ROLE_CUSTOMER. Customers can only pay their own bills.")
    public PaymentResponse record(@Valid @RequestBody PaymentRequest request, Principal principal) {
        return service.record(request, principal.getName());
    }
}

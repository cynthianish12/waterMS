package com.utilitybilling.meter.controller;

import com.utilitybilling.meter.dto.MeterDtos.*;
import com.utilitybilling.meter.service.MeterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Meter management endpoints. */
@RestController
@RequestMapping("/api/meters")
@RequiredArgsConstructor
@Tag(name = "04. Meter Management")
public class MeterController {
    private final MeterService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create meter for active customer. Allowed role: ROLE_ADMIN.")
    public MeterResponse create(@Valid @RequestBody MeterRequest request) {
        return service.create(request);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','FINANCE')")
    @Operation(summary = "List meters. Allowed roles: ROLE_ADMIN, ROLE_OPERATOR, ROLE_FINANCE.")
    public List<MeterResponse> all() {
        return service.all();
    }
}

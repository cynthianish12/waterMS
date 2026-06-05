package com.utilitybilling.reading.controller;

import com.utilitybilling.reading.dto.MeterReadingDtos.*;
import com.utilitybilling.reading.service.MeterReadingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/** Meter reading capture endpoint. */
@RestController
@RequestMapping("/api/meter-readings")
@RequiredArgsConstructor
@Tag(name = "05. Meter Reading Management")
public class MeterReadingController {
    private final MeterReadingService service;

    @PostMapping
    @PreAuthorize("hasRole('OPERATOR')")
    @Operation(summary = "Capture meter reading. Allowed role: ROLE_OPERATOR.")
    public MeterReadingResponse capture(@Valid @RequestBody MeterReadingRequest request, Principal principal) {
        return service.capture(request, principal.getName());
    }
}

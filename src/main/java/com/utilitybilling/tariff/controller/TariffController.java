package com.utilitybilling.tariff.controller;

import com.utilitybilling.tariff.dto.TariffDtos.*;
import com.utilitybilling.tariff.service.TariffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/** Tariff configuration endpoints. */
@RestController
@RequestMapping("/api/tariffs")
@RequiredArgsConstructor
@Tag(name = "06. Tariff Management")
public class TariffController {
    private final TariffService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Configure versioned tariff. Allowed role: ROLE_ADMIN.")
    public TariffResponse create(@Valid @RequestBody TariffRequest request, Principal principal) {
        return service.create(request, principal.getName());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @Operation(summary = "List tariffs. Allowed roles: ROLE_ADMIN, ROLE_FINANCE.")
    public List<TariffResponse> all() {
        return service.all();
    }
}

package com.utilitybilling.bill.controller;

import com.utilitybilling.bill.dto.BillDtos.*;
import com.utilitybilling.bill.service.BillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/** Bill generation and approval endpoints. */
@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
@Tag(name = "07. Bill Management")
public class BillController {
    private final BillService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @Operation(summary = "Generate bill from valid meter reading. Allowed roles: ROLE_ADMIN, ROLE_FINANCE.")
    public BillResponse generate(@Valid @RequestBody GenerateBillRequest request) {
        return service.generate(request);
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve pending bill and notify customer. Allowed role: ROLE_ADMIN.")
    public BillResponse approve(@PathVariable Long id, Principal principal) {
        return service.approve(id, principal.getName());
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject pending bill and notify customer. Allowed role: ROLE_ADMIN.")
    public BillResponse reject(@PathVariable Long id, @Valid @RequestBody RejectBillRequest request, Principal principal) {
        return service.reject(id, request, principal.getName());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @Operation(summary = "List all bills. Allowed roles: ROLE_ADMIN, ROLE_FINANCE.")
    public List<BillResponse> all() {
        return service.all();
    }
}

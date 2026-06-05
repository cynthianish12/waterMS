package com.utilitybilling.user.controller;

import com.utilitybilling.common.Status;
import com.utilitybilling.common.ApiResponse;
import com.utilitybilling.user.dto.AdminUserDtos.*;
import com.utilitybilling.user.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/** Admin-only user management endpoints. */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "02. Admin User Management")
public class UserController {
    private final AdminUserService service;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "1. View Users. Allowed role: ROLE_ADMIN.")
    public List<UserView> all() {
        return service.all();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "2. View User By ID. Allowed role: ROLE_ADMIN.")
    public UserView byId(@PathVariable Long id) {
        return service.byId(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "3. Create Staff User. Allowed role: ROLE_ADMIN. Roles: ROLE_OPERATOR, ROLE_FINANCE, ROLE_ADMIN.")
    public UserView createStaff(@Valid @RequestBody CreateStaffUserRequest request) {
        return service.createStaff(request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "4. Update User Status Only. Allowed role: ROLE_ADMIN. Statuses: ACTIVE, INACTIVE, LOCKED.")
    public UserView status(@PathVariable Long id, @Valid @RequestBody UpdateUserStatusRequest request) {
        return service.updateStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "5. Delete User. Allowed role: ROLE_ADMIN. Soft-deletes users with linked records.")
    public ApiResponse delete(@PathVariable Long id, Principal principal) {
        return new ApiResponse(service.delete(id, principal.getName()));
    }
}

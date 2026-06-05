package com.utilitybilling.user.dto;

import com.utilitybilling.common.Role;
import com.utilitybilling.common.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/** DTOs used by admin-only user management endpoints. */
public final class AdminUserDtos {
    private AdminUserDtos() {}

    public record CreateStaffUserRequest(
            @Schema(description = "Staff full name", example = "System Operator")
            @NotBlank(message = "Full name is required")
            @Pattern(regexp = "^[A-Za-z ]+$", message = "Full name must contain letters and spaces only")
            String fullName,

            @Schema(description = "Staff email address", example = "operator@gmail.com")
            @NotBlank(message = "Email is required")
            @Email(message = "Invalid email format")
            String email,

            @Schema(description = "Rwandan staff phone number", example = "0782222222")
            @NotBlank(message = "Phone number is required")
            @Pattern(regexp = "^(07[2389])[0-9]{7}$", message = "Invalid Rwanda phone number")
            String phoneNumber,

            @Schema(description = "Initial password", example = "Operator@123")
            @NotBlank(message = "Password is required")
            @Size(min = 6, message = "Password must be at least 6 characters")
            String password,

            @Schema(description = "Staff role. Customer accounts must use public signup.", example = "ROLE_OPERATOR")
            @NotNull(message = "Role is required")
            Role role) {}

    public record UpdateUserStatusRequest(
            @Schema(description = "New user status", example = "ACTIVE")
            @NotNull(message = "Status is required")
            Status status) {}

    public record UserView(Long id, String fullName, String email, String phoneNumber, Status status, Role role, boolean verified) {}
}

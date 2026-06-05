package com.utilitybilling.customer.dto;

import com.utilitybilling.common.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

/** Customer API DTOs. */
public final class CustomerDtos {
    private CustomerDtos() {}
    public record CustomerRequest(
            @Schema(description = "Customer full name", example = "Nishimwe Cynthia Marie")
            @NotBlank @Pattern(regexp = "^[A-Za-z ]+$", message = "Full name must contain letters and spaces only") String fullName,
            @Schema(description = "Rwandan national ID number", example = "1199988776655441")
            @NotBlank @Pattern(regexp = "^[0-9]{16}$", message = "National ID must contain exactly 16 digits") String nationalId,
            @Schema(description = "Customer email address", example = "cynthia.customer@example.com")
            @NotBlank @Email @Pattern(regexp = "^[^A-Z]+$", message = "Email must be lowercase only") String email,
            @Schema(description = "Rwandan phone number", example = "0781234567")
            @NotBlank @Pattern(regexp = "^(07[2389])[0-9]{7}$", message = "Invalid Rwanda phone number") String phoneNumber,
            @Schema(description = "Customer service address", example = "Kigali, Rwanda")
            @NotBlank String address) {}
    public record CustomerResponse(Long id, String fullName, String nationalId, String email, String phoneNumber,
                                   String address, Status status, LocalDateTime createdAt) {}
}

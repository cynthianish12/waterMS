package com.utilitybilling.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/** Authentication request and response DTOs. */
public final class AuthDtos {
    private AuthDtos() {}

    @Schema(name = "CustomerSignupRequest", description = "Public customer signup request. Role and status are assigned automatically.")
    public record SignupRequest(
            @Schema(description = "Customer full name", example = "Nishimwe Cynthia Marie")
            @NotBlank(message = "Full name is required")
            @Pattern(regexp = "^[A-Za-z ]+$", message = "Full name must contain letters and spaces only")
            String fullName,

            @Schema(description = "Rwandan national ID number", example = "1199988776655441")
            @NotBlank(message = "National ID required")
            @Pattern(regexp = "^[0-9]{16}$", message = "National ID must contain exactly 16 digits")
            String nationalId,

            @Schema(description = "Customer email address", example = "cynthia.customer@example.com")
            @NotBlank(message = "Email required")
            @Email(message = "Invalid email format")
            @Pattern(regexp = "^[^A-Z]+$", message = "Email must be lowercase only")
            String email,

            @Schema(description = "Rwandan phone number", example = "0781234567")
            @NotBlank(message = "Phone required")
            @Pattern(regexp = "^(07[2389])[0-9]{7}$", message = "Invalid Rwanda phone number")
            String phoneNumber,

            @Schema(description = "Customer service address", example = "Kigali, Rwanda")
            @NotBlank(message = "Address required")
            String address,

            @Schema(description = "Customer password", example = "Customer@123")
            @NotBlank(message = "Password required")
            @Size(min = 6, message = "Password must be at least 6 characters")
            String password) {}

    public record LoginRequest(
            @Schema(description = "Registered email address", example = "cynthia.customer@example.com")
            @NotBlank @Email String email,
            @Schema(description = "Account password", example = "Customer@123")
            @NotBlank String password) {}
    public record VerifyOtpRequest(
            @Schema(example = "cynthia.customer@example.com") @NotBlank @Email String email,
            @Schema(description = "OTP code sent by email", example = "123456") @NotBlank String otpCode) {}
    public record ForgotPasswordRequest(
            @Schema(example = "cynthia.customer@example.com") @NotBlank @Email String email) {}
    public record ResetPasswordRequest(
            @Schema(example = "cynthia.customer@example.com") @NotBlank @Email String email,
            @Schema(example = "123456") @NotBlank String otpCode,
            @Schema(example = "Customer@456") @NotBlank @Size(min = 6) String newPassword) {}
    public record ChangePasswordRequest(
            @Schema(example = "Customer@123") @NotBlank String oldPassword,
            @Schema(example = "Customer@456") @NotBlank String newPassword) {}
    public record RefreshTokenRequest(@Schema(description = "JWT refresh token") @NotBlank String refreshToken) {}
    public record AuthResponse(String accessToken, String refreshToken, String tokenType, String role) {}
}

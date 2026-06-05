package com.utilitybilling.auth.controller;

import com.utilitybilling.auth.dto.AuthDtos.*;
import com.utilitybilling.auth.service.AuthService;
import com.utilitybilling.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/** Public authentication and authenticated password endpoints. */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "01. Authentication")
public class AuthController {
    private final AuthService auth;

    @PostMapping("/signup")
    @Operation(summary = "Customer signup. Public endpoint. Creates ROLE_CUSTOMER user and customer profile, then sends SMTP OTP.")
    public ApiResponse signup(@Valid @RequestBody SignupRequest request) {
        auth.signup(request);
        return new ApiResponse("Signup successful. Verify OTP before login.");
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify registration OTP. Public endpoint.")
    public ApiResponse verify(@Valid @RequestBody VerifyOtpRequest request) {
        auth.verifyOtp(request);
        return new ApiResponse("Account verified successfully");
    }

    @PostMapping("/login")
    @Operation(summary = "Login verified active user and receive JWT access and refresh tokens.")
    public AuthResponse login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
                    @ExampleObject(name = "Customer Login", value = "{\"email\":\"cynthia.customer@example.com\",\"password\":\"Customer@123\"}"),
                    @ExampleObject(name = "Operator Login", value = "{\"email\":\"operator@gmail.com\",\"password\":\"Operator@123\"}"),
                    @ExampleObject(name = "Finance Login", value = "{\"email\":\"finance@gmail.com\",\"password\":\"finance@123\"}")
            }))
            @Valid @org.springframework.web.bind.annotation.RequestBody LoginRequest request) {
        return auth.login(request);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT access token with a valid refresh token.")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return auth.refresh(request);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset OTP. Public endpoint.")
    public ApiResponse forgot(@Valid @RequestBody ForgotPasswordRequest request) {
        auth.forgotPassword(request);
        return new ApiResponse("Password reset OTP sent");
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using OTP. Public endpoint.")
    public ApiResponse reset(@Valid @RequestBody ResetPasswordRequest request) {
        auth.resetPassword(request);
        return new ApiResponse("Password reset successful");
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change password. Allowed roles: ROLE_ADMIN, ROLE_OPERATOR, ROLE_FINANCE, ROLE_CUSTOMER.")
    public ApiResponse change(@Valid @RequestBody ChangePasswordRequest request, Principal principal) {
        auth.changePassword(principal.getName(), request);
        return new ApiResponse("Password changed successfully");
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Logout by clearing refresh token. Allowed roles: ROLE_ADMIN, ROLE_OPERATOR, ROLE_FINANCE, ROLE_CUSTOMER.")
    public ApiResponse logout(Principal principal) {
        auth.logout(principal.getName());
        return new ApiResponse("Logged out successfully");
    }
}

package com.utilitybilling.auth.service;

import com.utilitybilling.auth.dto.AuthDtos.*;
import com.utilitybilling.common.Role;
import com.utilitybilling.common.Status;
import com.utilitybilling.common.ValidationSupport;
import com.utilitybilling.customer.entity.Customer;
import com.utilitybilling.customer.repository.CustomerRepository;
import com.utilitybilling.exception.BusinessException;
import com.utilitybilling.exception.DuplicateResourceException;
import com.utilitybilling.security.JwtService;
import com.utilitybilling.user.entity.User;
import com.utilitybilling.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/** Handles signup, OTP verification, login, refresh, logout, and password recovery. */
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository users;
    private final CustomerRepository customers;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final EmailService emailService;
    private final AuthenticationManager authManager;
    private final SecureRandom random = new SecureRandom();
    @Value("${app.otp.minutes}")
    private long otpMinutes;

    @Transactional
    public void signup(SignupRequest request) {
        if (users.existsByEmail(request.email())) throw new DuplicateResourceException("Email already exists");
        if (users.existsByPhoneNumber(request.phoneNumber())) throw new DuplicateResourceException("Phone number already exists");
        if (customers.existsByNationalId(request.nationalId())) throw new DuplicateResourceException("National ID already exists");
        if (customers.existsByEmail(request.email())) throw new DuplicateResourceException("Customer email already exists");
        if (customers.existsByPhoneNumber(request.phoneNumber())) throw new DuplicateResourceException("Customer phone already exists");
        ValidationSupport.passwordDoesNotContainIdentity(request.password(), request.email(), request.fullName(), request.phoneNumber());
        User user = User.builder().fullName(request.fullName()).email(request.email()).phoneNumber(request.phoneNumber())
                .password(encoder.encode(request.password())).role(Role.ROLE_CUSTOMER).status(Status.ACTIVE)
                .verified(false).build();
        setOtp(user);
        User savedUser = users.save(user);
        customers.save(Customer.builder()
                .fullName(request.fullName())
                .nationalId(request.nationalId())
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .address(request.address())
                .status(Status.ACTIVE)
                .user(savedUser)
                .build());
        emailService.sendOtp(user.getEmail(), user.getOtpCode());
    }

    public void verifyOtp(VerifyOtpRequest request) {
        User user = byEmail(request.email());
        verifyCode(user, request.otpCode());
        user.setVerified(true);
        user.setOtpCode(null);
        user.setOtpExpiryTime(null);
        users.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = byEmail(request.email());
        if (!user.isVerified()) throw new BusinessException("Account must be verified before login");
        if (user.getStatus() == Status.INACTIVE) throw new BusinessException("Inactive users cannot login");
        if (user.getStatus() == Status.LOCKED) throw new BusinessException("Locked users cannot login");
        authManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        String access = jwt.accessToken(user);
        String refresh = jwt.refreshToken(user);
        user.setRefreshToken(refresh);
        users.save(user);
        return new AuthResponse(access, refresh, "Bearer", user.getRole().name());
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        String email = jwt.subject(request.refreshToken());
        User user = byEmail(email);
        if (!request.refreshToken().equals(user.getRefreshToken()) || !jwt.valid(request.refreshToken(), email)) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        return new AuthResponse(jwt.accessToken(user), request.refreshToken(), "Bearer", user.getRole().name());
    }

    public void logout(String email) {
        User user = byEmail(email);
        user.setRefreshToken(null);
        users.save(user);
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        User user = byEmail(request.email());
        setOtp(user);
        users.save(user);
        emailService.sendOtp(user.getEmail(), user.getOtpCode());
    }

    public void resetPassword(ResetPasswordRequest request) {
        User user = byEmail(request.email());
        verifyCode(user, request.otpCode());
        ValidationSupport.passwordDoesNotContainIdentity(request.newPassword(), user.getEmail(), user.getFullName(), user.getPhoneNumber());
        user.setPassword(encoder.encode(request.newPassword()));
        user.setOtpCode(null);
        user.setOtpExpiryTime(null);
        users.save(user);
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        User user = byEmail(email);
        if (!encoder.matches(request.oldPassword(), user.getPassword())) throw new BadCredentialsException("Invalid old password");
        ValidationSupport.passwordDoesNotContainIdentity(request.newPassword(), user.getEmail(), user.getFullName(), user.getPhoneNumber());
        user.setPassword(encoder.encode(request.newPassword()));
        users.save(user);
    }

    private User byEmail(String email) {
        return users.findByEmail(email).orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
    }

    private void setOtp(User user) {
        user.setOtpCode(String.valueOf(100000 + random.nextInt(900000)));
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(otpMinutes));
    }

    private void verifyCode(User user, String code) {
        if (user.getOtpExpiryTime() == null || user.getOtpExpiryTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Expired OTP");
        }
        if (!code.equals(user.getOtpCode())) throw new BusinessException("Invalid OTP");
    }
}

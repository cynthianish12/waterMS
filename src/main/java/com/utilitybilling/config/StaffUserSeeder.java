package com.utilitybilling.config;

import com.utilitybilling.common.Role;
import com.utilitybilling.common.Status;
import com.utilitybilling.user.entity.User;
import com.utilitybilling.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Seeds dummy staff users used for Swagger/manual testing. */
@Configuration
@RequiredArgsConstructor
public class StaffUserSeeder {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedStaffUsers() {
        return args -> {
            seed("System Operator", "operator@gmail.com", "0782222222", "Operator@123", Role.ROLE_OPERATOR);
            seed("Finance User", "finance@gmail.com", "0783333333", "finance@123", Role.ROLE_FINANCE);
        };
    }

    private void seed(String fullName, String email, String phone, String rawPassword, Role role) {
        if (users.existsByEmail(email)) return;
        if (users.existsByPhoneNumber(phone)) return;
        users.save(User.builder()
                .fullName(fullName)
                .email(email)
                .phoneNumber(phone)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .status(Status.ACTIVE)
                .verified(true)
                .build());
    }
}

package com.utilitybilling.user.repository;

import com.utilitybilling.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Persistence for system users. */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    long countByRole(com.utilitybilling.common.Role role);
}

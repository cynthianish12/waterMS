package com.utilitybilling.user.service;

import com.utilitybilling.bill.repository.BillRepository;
import com.utilitybilling.common.Role;
import com.utilitybilling.common.Status;
import com.utilitybilling.customer.repository.CustomerRepository;
import com.utilitybilling.exception.BusinessException;
import com.utilitybilling.exception.DuplicateResourceException;
import com.utilitybilling.exception.ResourceNotFoundException;
import com.utilitybilling.payment.repository.PaymentRepository;
import com.utilitybilling.reading.repository.MeterReadingRepository;
import com.utilitybilling.user.dto.AdminUserDtos.*;
import com.utilitybilling.user.entity.User;
import com.utilitybilling.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Admin-only user management business rules. */
@Service
@RequiredArgsConstructor
public class AdminUserService {
    private final UserRepository users;
    private final CustomerRepository customers;
    private final BillRepository bills;
    private final MeterReadingRepository readings;
    private final PaymentRepository payments;
    private final PasswordEncoder passwordEncoder;

    public List<UserView> all() {
        return users.findAll().stream().map(this::toView).toList();
    }

    public UserView byId(Long id) {
        return toView(get(id));
    }

    public UserView createStaff(CreateStaffUserRequest request) {
        if (request.role() == Role.ROLE_CUSTOMER) {
            throw new BusinessException("Use public signup to create customer accounts");
        }
        if (users.existsByEmail(request.email())) throw new DuplicateResourceException("Email already exists");
        if (users.existsByPhoneNumber(request.phoneNumber())) throw new DuplicateResourceException("Phone number already exists");
        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .status(Status.ACTIVE)
                .verified(true)
                .build();
        return toView(users.save(user));
    }

    public UserView updateStatus(Long id, Status status) {
        User user = get(id);
        user.setStatus(status);
        return toView(users.save(user));
    }

    @Transactional
    public String delete(Long id, String currentAdminEmail) {
        User user = get(id);
        if (user.getEmail().equalsIgnoreCase(currentAdminEmail)) {
            throw new BusinessException("Admin cannot delete themselves");
        }
        if (user.getRole() == Role.ROLE_ADMIN && users.countByRole(Role.ROLE_ADMIN) <= 1) {
            throw new BusinessException("Cannot delete the last remaining admin");
        }
        if (hasLinkedRecords(user.getId())) {
            user.setStatus(Status.INACTIVE);
            users.save(user);
            return "User has linked records and was deactivated instead of deleted";
        }
        users.delete(user);
        return "User deleted successfully";
    }

    private boolean hasLinkedRecords(Long userId) {
        return customers.existsByUserId(userId)
                || users.findById(userId).map(user -> customers.existsByEmail(user.getEmail())).orElse(false)
                || readings.existsByCapturedById(userId)
                || payments.existsByRecordedById(userId)
                || bills.existsByApprovedById(userId);
    }

    private User get(Long id) {
        return users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UserView toView(User u) {
        return new UserView(u.getId(), u.getFullName(), u.getEmail(), u.getPhoneNumber(),
                u.getStatus(), u.getRole(), u.isVerified());
    }
}

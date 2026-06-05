package com.utilitybilling.notification.controller;

import com.utilitybilling.customer.entity.Customer;
import com.utilitybilling.customer.repository.CustomerRepository;
import com.utilitybilling.exception.ResourceNotFoundException;
import com.utilitybilling.notification.dto.NotificationResponse;
import com.utilitybilling.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

/** Notification endpoints for customers and administrators. */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "09. Notifications")
public class NotificationController {
    private final NotificationService notifications;
    private final CustomerRepository customers;

    @GetMapping("/my-notifications")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "View own notifications, including approved bill notifications. Allowed role: ROLE_CUSTOMER.")
    public List<NotificationResponse> mine(Principal principal) {
        return notifications.byCustomer(currentCustomer(principal).getId());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "View all customer notifications. Allowed role: ROLE_ADMIN.")
    public List<NotificationResponse> all() {
        return notifications.all();
    }

    private Customer currentCustomer(Principal principal) {
        return customers.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("No customer profile linked to this user email"));
    }
}

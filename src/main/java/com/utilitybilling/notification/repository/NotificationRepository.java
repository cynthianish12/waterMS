package com.utilitybilling.notification.repository;

import com.utilitybilling.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Persistence for notifications. */
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByCustomerId(Long customerId);
    boolean existsByBillIdAndNotificationType(Long billId, String notificationType);
    List<Notification> findAllByOrderByCreatedAtDesc();
}

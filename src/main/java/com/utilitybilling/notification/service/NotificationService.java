package com.utilitybilling.notification.service;

import com.utilitybilling.auth.service.EmailService;
import com.utilitybilling.bill.entity.Bill;
import com.utilitybilling.common.EmailStatus;
import com.utilitybilling.common.NotificationStatus;
import com.utilitybilling.common.Status;
import com.utilitybilling.customer.entity.Customer;
import com.utilitybilling.notification.dto.NotificationResponse;
import com.utilitybilling.notification.entity.Notification;
import com.utilitybilling.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Month;
import java.util.List;
import java.util.Locale;

/** Creates and exposes customer notifications. */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository notifications;
    private final EmailService emailService;

    public void billGenerated(Bill bill) {
        String message = "Dear %s,\nYour %s/%s utility bill of %s FRW has been successfully processed."
                .formatted(bill.getCustomer().getFullName(), bill.getBillingMonth(), bill.getBillingYear(), bill.getTotalAmount());
        create(bill.getCustomer(), bill, "Bill Generated", message, "BILL_GENERATED", EmailStatus.NOT_REQUIRED);
    }

    public EmailStatus billApproved(Bill bill) {
        if (notifications.existsByBillIdAndNotificationType(bill.getId(), "BILL_APPROVED")) {
            return EmailStatus.NOT_REQUIRED;
        }
        String amount = formatAmount(bill.getTotalAmount());
        String monthYear = Month.of(bill.getBillingMonth()).name().charAt(0)
                + Month.of(bill.getBillingMonth()).name().substring(1).toLowerCase(Locale.ROOT)
                + "/" + bill.getBillingYear();
        String message = "Dear %s, your %s utility bill for meter %s has been approved. Total amount: %s FRW. Due date: %s."
                .formatted(bill.getCustomer().getFullName(), monthYear, bill.getMeter().getMeterNumber(), amount, bill.getDueDate());
        String emailBody = """
                Dear %s,

                Your %s utility bill for meter %s has been approved.

                Bill Reference: %s
                Total Amount: %s FRW
                Due Date: %s
                Status: APPROVED

                Please make your payment before the due date.

                Thank you,
                Utility Billing System
                """.formatted(bill.getCustomer().getFullName(), monthYear, bill.getMeter().getMeterNumber(),
                bill.getBillReference(), amount, bill.getDueDate());
        boolean sent = emailService.sendBillApproved(bill.getCustomer().getEmail(), emailBody);
        EmailStatus emailStatus = sent ? EmailStatus.SENT : EmailStatus.FAILED;
        create(bill.getCustomer(), bill, "Bill Approved", message, "BILL_APPROVED", emailStatus);
        return emailStatus;
    }

    public EmailStatus billRejected(Bill bill, String reason) {
        if (notifications.existsByBillIdAndNotificationType(bill.getId(), "BILL_REJECTED")) {
            return EmailStatus.NOT_REQUIRED;
        }
        String message = """
                Dear %s,

                Your utility bill for meter %s has been rejected.

                Reason:
                %s

                Please contact support or wait for correction.
                """.formatted(bill.getCustomer().getFullName(), bill.getMeter().getMeterNumber(), reason);
        String emailBody = """
                Dear %s,

                Your utility bill for meter %s has been rejected.

                Bill Reference: %s
                Reason: %s
                Status: REJECTED

                Please contact support or wait for correction.

                Thank you,
                Utility Billing System
                """.formatted(bill.getCustomer().getFullName(), bill.getMeter().getMeterNumber(),
                bill.getBillReference(), reason);
        boolean sent = emailService.sendBillRejected(bill.getCustomer().getEmail(), emailBody);
        EmailStatus emailStatus = sent ? EmailStatus.SENT : EmailStatus.FAILED;
        create(bill.getCustomer(), bill, "Bill Rejected", message, "BILL_REJECTED", emailStatus);
        return emailStatus;
    }

    public void paymentReceived(Bill bill, BigDecimal amount) {
        String customerEmail = bill.getCustomer().getEmail();
        String message;
        String emailBody;
        boolean emailSent;
        if (bill.getOutstandingBalance().compareTo(BigDecimal.ZERO) == 0) {
            message = "Dear %s,\nYour bill %s is fully paid."
                    .formatted(bill.getCustomer().getFullName(), bill.getBillReference());
            emailBody = """
                    Dear %s,

                    Your payment for bill %s has been received successfully.

                    Total Bill Amount: %s FRW
                    Amount Paid: %s FRW
                    Outstanding Balance: 0 FRW
                    Bill Status: PAID

                    Your utility bill has been fully paid.

                    Thank you,
                    Utility Billing System
                    """.formatted(bill.getCustomer().getFullName(), bill.getBillReference(),
                    formatAmount(bill.getTotalAmount()), formatAmount(bill.getAmountPaid()));
            log.info("Calling payment completed email. customerEmail={}, billReference={}, paymentAmount={}, outstandingBalance={}",
                    customerEmail, bill.getBillReference(), amount, bill.getOutstandingBalance());
            emailSent = emailService.sendPaymentCompletedEmail(customerEmail, emailBody);
        } else {
            message = "Dear %s,\nYour payment of %s FRW for bill %s has been received successfully. Remaining balance: %s FRW."
                    .formatted(bill.getCustomer().getFullName(), amount, bill.getBillReference(), bill.getOutstandingBalance());
            emailBody = """
                    Dear %s,

                    Your partial payment for bill %s has been received.

                    Total Bill Amount: %s FRW
                    Amount Paid: %s FRW
                    Total Paid So Far: %s FRW
                    Remaining Balance: %s FRW
                    Bill Status: PARTIALLY_PAID

                    Please complete the remaining balance before the due date.

                    Thank you,
                    Utility Billing System
                    """.formatted(bill.getCustomer().getFullName(), bill.getBillReference(),
                    formatAmount(bill.getTotalAmount()), formatAmount(amount), formatAmount(bill.getAmountPaid()),
                    formatAmount(bill.getOutstandingBalance()));
            log.info("Calling partial payment email. customerEmail={}, billReference={}, paymentAmount={}, outstandingBalance={}",
                    customerEmail, bill.getBillReference(), amount, bill.getOutstandingBalance());
            emailSent = emailService.sendPartialPaymentEmail(customerEmail, emailBody);
        }
        EmailStatus emailStatus = emailSent ? EmailStatus.SENT : EmailStatus.FAILED;
        log.info("Payment email result. customerEmail={}, billReference={}, paymentAmount={}, outstandingBalance={}, emailStatus={}",
                customerEmail, bill.getBillReference(), amount, bill.getOutstandingBalance(), emailStatus);
        create(bill.getCustomer(), bill, "Payment Received", message, "PAYMENT_SUCCESS", emailStatus);
    }

    public List<NotificationResponse> byCustomer(Long customerId) {
        return notifications.findByCustomerId(customerId).stream().map(this::toResponse).toList();
    }

    public List<NotificationResponse> all() {
        return notifications.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    private void create(Customer customer, Bill bill, String title, String message, String type, EmailStatus emailStatus) {
        if (customer.getStatus() != Status.ACTIVE) {
            return;
        }
        notifications.save(Notification.builder()
                .customer(customer).bill(bill).title(title).message(message).notificationType(type)
                .emailStatus(emailStatus).status(NotificationStatus.UNREAD).build());
    }

    private NotificationResponse toResponse(Notification n) {
        Bill bill = n.getBill();
        return new NotificationResponse(n.getId(), n.getTitle(), n.getMessage(),
                bill == null ? null : bill.getBillReference(),
                bill == null ? null : bill.getMeter().getMeterNumber(),
                n.getStatus(), n.getCreatedAt());
    }

    private String formatAmount(BigDecimal amount) {
        return NumberFormat.getNumberInstance(Locale.US).format(amount);
    }
}

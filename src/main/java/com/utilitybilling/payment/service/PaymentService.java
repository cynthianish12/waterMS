package com.utilitybilling.payment.service;

import com.utilitybilling.bill.entity.Bill;
import com.utilitybilling.bill.repository.BillRepository;
import com.utilitybilling.common.BillStatus;
import com.utilitybilling.common.Role;
import com.utilitybilling.common.Status;
import com.utilitybilling.customer.entity.Customer;
import com.utilitybilling.customer.repository.CustomerRepository;
import com.utilitybilling.exception.BusinessException;
import com.utilitybilling.exception.ResourceNotFoundException;
import com.utilitybilling.notification.service.NotificationService;
import com.utilitybilling.payment.dto.PaymentDtos.*;
import com.utilitybilling.payment.entity.Payment;
import com.utilitybilling.payment.repository.PaymentRepository;
import com.utilitybilling.user.entity.User;
import com.utilitybilling.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Records payments and updates bill balances atomically. */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentRepository payments;
    private final BillRepository bills;
    private final UserRepository users;
    private final CustomerRepository customers;
    private final NotificationService notifications;

    @Transactional
    public PaymentResponse record(PaymentRequest r, String currentUserEmail) {
        validatePaymentRequest(r);
        Bill bill = bills.findByBillReference(r.billReference()).orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
        User currentUser = users.findByEmail(currentUserEmail).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        enforcePaymentOwnership(bill, currentUser);
        if (bill.getStatus() == BillStatus.REJECTED) throw new BusinessException("Rejected bills cannot be paid.");
        if (bill.getStatus() == BillStatus.PAID) throw new BusinessException("This bill is already fully paid.");
        if (bill.getStatus() != BillStatus.APPROVED && bill.getStatus() != BillStatus.PARTIALLY_PAID) {
            throw new BusinessException("Bill must be approved before payment.");
        }
        if (bill.getCustomer().getStatus() != Status.ACTIVE) throw new BusinessException("Cannot pay an inactive customer's bill");
        if (bill.getOutstandingBalance() == null || bill.getOutstandingBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("This bill has no outstanding balance.");
        }
        if (r.amountPaid().compareTo(bill.getOutstandingBalance()) > 0) throw new BusinessException("Payment amount cannot exceed outstanding balance.");
        if (bill.getCreatedAt() != null && r.paymentDate().isBefore(bill.getCreatedAt().toLocalDate())) throw new BusinessException("Payment date cannot be before bill creation date");
        Payment payment = payments.save(Payment.builder().paymentReference("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .bill(bill).customer(bill.getCustomer()).amountPaid(r.amountPaid()).paymentMethod(r.paymentMethod())
                .paymentDate(r.paymentDate()).recordedBy(currentUser).build());
        bill.setAmountPaid(bill.getAmountPaid().add(r.amountPaid()));
        bill.setOutstandingBalance(bill.getOutstandingBalance().subtract(r.amountPaid()));
        bill.setStatus(bill.getOutstandingBalance().compareTo(BigDecimal.ZERO) == 0 ? BillStatus.PAID : BillStatus.PARTIALLY_PAID);
        bills.save(bill);
        log.info("Payment recorded. customerEmail={}, billReference={}, paymentAmount={}, outstandingBalance={}, billStatus={}",
                bill.getCustomer().getEmail(), bill.getBillReference(), r.amountPaid(), bill.getOutstandingBalance(), bill.getStatus());
        log.info("Calling payment notification and email flow. billReference={}", bill.getBillReference());
        notifications.paymentReceived(bill, r.amountPaid());
        return new PaymentResponse(payment.getId(), payment.getPaymentReference(), bill.getBillReference(), payment.getAmountPaid(), payment.getPaymentMethod());
    }

    private void validatePaymentRequest(PaymentRequest r) {
        if (r.billReference() == null || r.billReference().isBlank()) throw new BusinessException("Bill reference is required.");
        if (r.amountPaid() == null) throw new BusinessException("amountPaid is required.");
        if (r.amountPaid().compareTo(BigDecimal.ZERO) <= 0) throw new BusinessException("amountPaid must be greater than 0.");
        if (r.paymentMethod() == null) throw new BusinessException("paymentMethod must be valid: CASH, MOMO, BANK, CARD.");
        if (r.paymentDate() == null) throw new BusinessException("paymentDate is required.");
        if (r.paymentDate().isAfter(LocalDate.now())) throw new BusinessException("paymentDate cannot be in the future.");
    }

    private void enforcePaymentOwnership(Bill bill, User currentUser) {
        if (currentUser.getRole() == Role.ROLE_FINANCE) {
            return;
        }
        if (currentUser.getRole() != Role.ROLE_CUSTOMER) {
            throw new AccessDeniedException("Only customers and finance users can record payments");
        }
        Customer customer = customers.findByEmail(currentUser.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No customer profile linked to this user email"));
        if (!bill.getCustomer().getId().equals(customer.getId())) {
            throw new AccessDeniedException("You are not allowed to pay a bill that does not belong to you.");
        }
    }

    public List<PaymentResponse> byCustomer(Long customerId) {
        return payments.findByCustomerId(customerId).stream()
                .map(p -> new PaymentResponse(p.getId(), p.getPaymentReference(), p.getBill().getBillReference(), p.getAmountPaid(), p.getPaymentMethod()))
                .toList();
    }
}

package com.utilitybilling.bill.service;

import com.utilitybilling.audit.service.AuditLogService;
import com.utilitybilling.bill.dto.BillDtos.*;
import com.utilitybilling.bill.entity.Bill;
import com.utilitybilling.bill.repository.BillRepository;
import com.utilitybilling.common.BillStatus;
import com.utilitybilling.common.EmailStatus;
import com.utilitybilling.common.Status;
import com.utilitybilling.exception.BusinessException;
import com.utilitybilling.exception.ResourceNotFoundException;
import com.utilitybilling.notification.service.NotificationService;
import com.utilitybilling.reading.entity.MeterReading;
import com.utilitybilling.reading.service.MeterReadingService;
import com.utilitybilling.tariff.entity.Tariff;
import com.utilitybilling.tariff.service.TariffService;
import com.utilitybilling.user.entity.User;
import com.utilitybilling.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Generates, approves, and exposes bills. */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillService {
    private final BillRepository bills;
    private final MeterReadingService readings;
    private final TariffService tariffs;
    private final NotificationService notifications;
    private final UserRepository users;
    private final AuditLogService auditLogs;

    public BillResponse generate(GenerateBillRequest r) {
        MeterReading reading = readings.get(r.meterReadingId());
        if (reading.getMeter().getStatus() != Status.ACTIVE) throw new BusinessException("Inactive meters cannot receive bills");
        if (reading.getMeter().getCustomer().getStatus() != Status.ACTIVE) throw new BusinessException("Inactive customers cannot receive bills");
        if (bills.existsByMeterIdAndBillingMonthAndBillingYear(reading.getMeter().getId(), reading.getMonth(), reading.getYear())) {
            throw new BusinessException("One bill per meter per billing month/year is allowed");
        }
        if (!r.dueDate().isAfter(LocalDate.now())) throw new BusinessException("Due date must be after bill generation date");
        LocalDate billingDate = reading.getReadingDate();
        log.info("Generating bill for readingId={}, meterId={}, meterType={}, readingDate={}, billingMonth={}, billingYear={}",
                reading.getId(), reading.getMeter().getId(), reading.getMeter().getMeterType(),
                reading.getReadingDate(), reading.getMonth(), reading.getYear());
        log.info("Searching tariff using utilityType={}, billingDate={}", reading.getMeter().getMeterType(), billingDate);
        Tariff tariff = tariffs.applicable(reading.getMeter().getMeterType(), billingDate);
        BigDecimal tariffAmount = tariffs.variableAmount(tariff, reading.getConsumption());
        BigDecimal fixed = tariff.getFixedServiceCharge();
        BigDecimal vat = tariffAmount.add(fixed).multiply(tariff.getVatPercentage()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal penalty = BigDecimal.ZERO;
        BigDecimal total = tariffAmount.add(fixed).add(vat).add(penalty);
        Bill bill = bills.save(Bill.builder().billReference("BILL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .customer(reading.getMeter().getCustomer()).meter(reading.getMeter()).meterReading(reading).tariff(tariff)
                .billingMonth(reading.getMonth()).billingYear(reading.getYear()).consumption(reading.getConsumption())
                .tariffAmount(tariffAmount).fixedCharge(fixed).vatAmount(vat).penaltyAmount(penalty).totalAmount(total)
                .amountPaid(BigDecimal.ZERO).outstandingBalance(total).status(BillStatus.PENDING).dueDate(r.dueDate()).build());
        notifications.billGenerated(bill);
        return toResponse(bill);
    }

    public BillResponse approve(Long id, String approverEmail) {
        Bill bill = get(id);
        if (bill.getStatus() == BillStatus.APPROVED) throw new BusinessException("Bill has already been approved");
        if (bill.getStatus() == BillStatus.REJECTED) throw new BusinessException("Rejected bill cannot be approved without resubmission");
        if (bill.getStatus() != BillStatus.PENDING) throw new BusinessException("Only pending bills can be approved");
        User approver = users.findByEmail(approverEmail).orElse(null);
        bill.setStatus(BillStatus.APPROVED);
        bill.setApprovedBy(approver);
        bill.setApprovedAt(LocalDateTime.now());
        Bill approved = bills.save(bill);
        EmailStatus emailStatus = notifications.billApproved(approved);
        auditLogs.log("BILL_APPROVAL_NOTIFICATION_SENT", approverEmail, null,
                "Bill approval notification and email %s to customer for bill %s."
                        .formatted(emailStatus, approved.getBillReference()));
        return toResponse(approved);
    }

    public BillResponse reject(Long id, RejectBillRequest request, String adminEmail) {
        Bill bill = get(id);
        if (bill.getStatus() == BillStatus.APPROVED) throw new BusinessException("Approved bill cannot be rejected");
        if (bill.getStatus() == BillStatus.REJECTED) throw new BusinessException("Bill has already been rejected");
        if (bill.getStatus() != BillStatus.PENDING) throw new BusinessException("Only pending bills can be rejected");
        bill.setStatus(BillStatus.REJECTED);
        bill.setRejectionReason(request.reason());
        Bill rejected = bills.save(bill);
        EmailStatus emailStatus = notifications.billRejected(rejected, request.reason());
        auditLogs.log("BILL_REJECTION_NOTIFICATION_SENT", adminEmail, null,
                "Bill rejection notification and email %s to customer for bill %s."
                        .formatted(emailStatus, rejected.getBillReference()));
        return toResponse(rejected);
    }

    public Bill get(Long id) {
        return bills.findById(id).orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
    }

    public List<BillResponse> all() {
        return bills.findAll().stream().map(this::toResponse).toList();
    }

    public List<BillResponse> byCustomer(Long customerId) {
        return bills.findByCustomerId(customerId).stream().map(this::toResponse).toList();
    }

    public BillResponse toResponse(Bill b) {
        return new BillResponse(b.getId(), b.getBillReference(), b.getCustomer().getId(), b.getMeter().getId(),
                b.getBillingMonth(), b.getBillingYear(), b.getTotalAmount(), b.getAmountPaid(), b.getOutstandingBalance(), b.getStatus());
    }
}

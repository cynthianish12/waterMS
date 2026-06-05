package com.utilitybilling.reading.service;

import com.utilitybilling.common.Status;
import com.utilitybilling.exception.BusinessException;
import com.utilitybilling.exception.DuplicateResourceException;
import com.utilitybilling.exception.ResourceNotFoundException;
import com.utilitybilling.meter.entity.Meter;
import com.utilitybilling.meter.service.MeterService;
import com.utilitybilling.reading.dto.MeterReadingDtos.*;
import com.utilitybilling.reading.entity.MeterReading;
import com.utilitybilling.reading.repository.MeterReadingRepository;
import com.utilitybilling.user.entity.User;
import com.utilitybilling.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Locale;

/** Validates and stores monthly meter readings. */
@Service
@RequiredArgsConstructor
public class MeterReadingService {
    private final MeterReadingRepository readings;
    private final MeterService meters;
    private final UserRepository users;

    public MeterReadingResponse capture(MeterReadingRequest r, String operatorEmail) {
        validateReadingRequest(r);
        int readingMonth = r.normalizedMonth();
        int readingYear = r.normalizedYear();
        Meter meter = meters.get(r.meterId());
        if (meter.getStatus() != Status.ACTIVE) throw new BusinessException("Meter must be ACTIVE before a reading can be captured.");
        if (meter.getCustomer().getStatus() != Status.ACTIVE) throw new BusinessException("Inactive customers cannot receive readings");
        if (r.currentReading().compareTo(r.previousReading()) <= 0) throw new BusinessException("Current reading must be greater than previous reading");
        BigDecimal consumption = r.currentReading().subtract(r.previousReading());
        if (consumption.compareTo(BigDecimal.ZERO) <= 0) throw new BusinessException("Consumption must be greater than 0.");
        if (r.readingDate().isBefore(meter.getInstallationDate())) throw new BusinessException("Reading date cannot be before meter installation date");
        if (readings.existsByMeterIdAndMonthAndYear(meter.getId(), readingMonth, readingYear)) {
            throw new DuplicateResourceException("This meter already has a reading for %s %d."
                    .formatted(formatMonth(readingMonth), readingYear));
        }
        readings.findLatestBeforeMonth(meter.getId(), readingYear, readingMonth).ifPresent(last -> {
            if (last.getCurrentReading().compareTo(r.previousReading()) != 0) {
                throw new BusinessException("Previous reading must match the last recorded current reading for this meter.");
            }
        });
        User operator = users.findByEmail(operatorEmail).orElse(null);
        MeterReading reading = readings.save(MeterReading.builder().meter(meter).previousReading(r.previousReading())
                .currentReading(r.currentReading()).consumption(consumption)
                .readingDate(r.readingDate()).month(readingMonth).year(readingYear).capturedBy(operator).build());
        return toResponse(reading);
    }

    private void validateReadingRequest(MeterReadingRequest r) {
        if (r.meterId() == null) throw new BusinessException("meterId is required.");
        if (r.previousReading() == null) throw new BusinessException("previousReading is required.");
        if (r.currentReading() == null) throw new BusinessException("currentReading is required.");
        if (r.readingDate() == null) throw new BusinessException("readingDate is required.");
        if (r.readingDate().isAfter(LocalDate.now())) throw new BusinessException("readingDate cannot be in the future.");
        if (r.month() != null && r.month() != r.readingDate().getMonthValue()) {
            throw new BusinessException("reading_month must match the month in readingDate.");
        }
        if (r.year() != null && r.year() != r.readingDate().getYear()) {
            throw new BusinessException("reading_year must match the year in readingDate.");
        }
    }

    private String formatMonth(int month) {
        String name = Month.of(month).name();
        return name.charAt(0) + name.substring(1).toLowerCase(Locale.ROOT);
    }

    public MeterReading get(Long id) {
        return readings.findById(id).orElseThrow(() -> new ResourceNotFoundException("Meter reading not found"));
    }

    public MeterReadingResponse toResponse(MeterReading r) {
        return new MeterReadingResponse(r.getId(), r.getMeter().getId(), r.getPreviousReading(),
                r.getCurrentReading(), r.getConsumption(), r.getMonth(), r.getYear());
    }
}

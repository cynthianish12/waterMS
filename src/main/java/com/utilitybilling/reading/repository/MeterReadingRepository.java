package com.utilitybilling.reading.repository;

import com.utilitybilling.reading.entity.MeterReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/** Persistence for meter readings. */
public interface MeterReadingRepository extends JpaRepository<MeterReading, Long> {
    boolean existsByMeterIdAndMonthAndYear(Long meterId, Integer month, Integer year);
    boolean existsByCapturedById(Long userId);
    Optional<MeterReading> findTopByMeterIdOrderByYearDescMonthDesc(Long meterId);
    @Query(value = """
            select * from meter_readings r
            where r.meter_id = :meterId
              and (r.reading_year < :year or (r.reading_year = :year and r.reading_month < :month))
            order by r.reading_year desc, r.reading_month desc
            limit 1
            """, nativeQuery = true)
    Optional<MeterReading> findLatestBeforeMonth(@Param("meterId") Long meterId,
                                                 @Param("year") Integer year,
                                                 @Param("month") Integer month);
}

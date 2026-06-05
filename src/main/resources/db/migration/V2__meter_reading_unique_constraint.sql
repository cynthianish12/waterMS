-- Enforce one reading per meter for each reading month/year.
-- This migration is guarded because the project can create tables through JPA before enabling Flyway.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = current_schema()
          AND table_name = 'meter_readings'
    ) AND NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_reading_meter_month_year'
    ) THEN
        ALTER TABLE meter_readings
            ADD CONSTRAINT uk_reading_meter_month_year
            UNIQUE (meter_id, reading_month, reading_year);
    END IF;
END $$;

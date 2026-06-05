-- PostgreSQL database routines for notification side effects.
-- Enable with FLYWAY_ENABLED=true and JPA_DDL_AUTO=validate/update after tables exist.

CREATE OR REPLACE FUNCTION notify_bill_generated()
RETURNS TRIGGER AS $$
DECLARE
    customer_name TEXT;
BEGIN
    SELECT full_name INTO customer_name FROM customers WHERE id = NEW.customer_id;
    INSERT INTO notifications(customer_id, bill_id, message, notification_type, status, created_at)
    VALUES (
        NEW.customer_id,
        NEW.id,
        'Dear ' || customer_name || E',\nYour ' || NEW.billing_month || '/' || NEW.billing_year ||
        ' utility bill of ' || NEW.total_amount || ' FRW has been successfully processed.',
        'BILL_GENERATED',
        'UNREAD',
        CURRENT_TIMESTAMP
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_notify_bill_generated ON bills;
CREATE TRIGGER trg_notify_bill_generated
AFTER INSERT ON bills
FOR EACH ROW
EXECUTE FUNCTION notify_bill_generated();

CREATE OR REPLACE FUNCTION notify_full_payment()
RETURNS TRIGGER AS $$
DECLARE
    customer_name TEXT;
    paid_now NUMERIC;
BEGIN
    IF NEW.status = 'PAID' AND OLD.status IS DISTINCT FROM NEW.status THEN
        SELECT full_name INTO customer_name FROM customers WHERE id = NEW.customer_id;
        paid_now := NEW.amount_paid - COALESCE(OLD.amount_paid, 0);
        INSERT INTO notifications(customer_id, bill_id, message, notification_type, status, created_at)
        VALUES (
            NEW.customer_id,
            NEW.id,
            'Dear ' || customer_name || E',\nYour payment of ' || paid_now ||
            ' FRW for bill ' || NEW.bill_reference || ' has been received successfully.',
            'PAYMENT_SUCCESS',
            'UNREAD',
            CURRENT_TIMESTAMP
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_notify_full_payment ON bills;
CREATE TRIGGER trg_notify_full_payment
AFTER UPDATE OF status, amount_paid ON bills
FOR EACH ROW
EXECUTE FUNCTION notify_full_payment();

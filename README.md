# Utility Billing System

Spring Boot backend for WASAC/REG utility billing. It manages users, customers, meters, meter readings, tariffs, bills, payments, and notifications with JWT security, role-based authorization, validation, Swagger UI, SMTP OTP, JPA persistence, and database routines.

## Run

```powershell
.\mvnw.cmd spring-boot:run
```

Swagger UI: `http://localhost:8080/swagger-ui.html`

The default profile uses an in-memory H2 database for fast testing. For PostgreSQL:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/utility_billing"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="postgres"
$env:DB_DRIVER="org.postgresql.Driver"
$env:JPA_DDL_AUTO="update"
$env:FLYWAY_ENABLED="true"
.\mvnw.cmd spring-boot:run
```

SMTP OTP is configured through `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, and `MAIL_PASSWORD`. In local development, failed email delivery is ignored so the OTP remains stored in the database for testing.

## Roles

- `ROLE_ADMIN`: manage users, customers, meters, tariffs, bills, and approvals.
- `ROLE_OPERATOR`: capture meter readings only.
- `ROLE_FINANCE`: approve bills and record payments.
- `ROLE_CUSTOMER`: view own bills, payment history, and notifications.

All non-authentication endpoints require a JWT bearer token. Swagger endpoint summaries state the allowed roles.

## ERD

```text
User (role)                         Tariff 1 ---- * TariffTier
  |
  | created/approved/captured
  v
Customer 1 ---- * Meter 1 ---- * MeterReading 1 ---- 1 Bill * ---- 1 Tariff(version)
   |                                  |                 |
   |                                  |                 * ---- * Payment
   |                                  |
   * ---- * Notification <------------+
```

Relationship notes:

- A user has one role and participates as creator, capturer, approver, or recorder.
- A customer may own many meters.
- A meter has many readings, with one reading per meter/month/year.
- A valid reading generates one bill.
- A bill stores the exact tariff version used for that billing cycle, so future tariffs do not change existing bills.
- A bill accepts many payments and updates `amountPaid`, `outstandingBalance`, and status automatically.
- Notifications are created for generated bills and successful payments.

## Flow

```text
Register user
  -> send email OTP
  -> verify OTP
  -> login
  -> generate JWT
  -> operator captures meter reading
  -> system validates reading
  -> admin/finance generates bill
  -> admin/finance approves bill
  -> customer receives bill notification
  -> finance records payment
  -> system updates outstanding balance
  -> bill becomes PAID when balance is zero
  -> customer receives payment notification
```

## Main Endpoints

- `POST /api/auth/signup`
- `POST /api/auth/verify-otp`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`
- `POST /api/customers` - `ROLE_ADMIN`
- `POST /api/meters` - `ROLE_ADMIN`
- `POST /api/meter-readings` - `ROLE_OPERATOR`
- `POST /api/tariffs` - `ROLE_ADMIN`
- `POST /api/bills` - `ROLE_ADMIN`, `ROLE_FINANCE`
- `PATCH /api/bills/{id}/approve` - `ROLE_ADMIN`, `ROLE_FINANCE`
- `POST /api/payments` - `ROLE_FINANCE`
- `GET /api/customers/my-bills` - `ROLE_CUSTOMER`
- `GET /api/customers/my-payments` - `ROLE_CUSTOMER`
- `GET /api/customers/my-notifications` - `ROLE_CUSTOMER`

## Sample Test Flow

1. Register admin, operator, finance, and customer users with lowercase emails and strong passwords.
2. Verify each OTP from the database or SMTP inbox.
3. Login and paste the access token into Swagger's bearer auth dialog.
4. As admin, create a customer using the same email as the customer user.
5. As admin, create a meter for the active customer.
6. As admin, configure a future or current tariff for the meter utility type.
7. As operator, capture a valid reading.
8. As admin or finance, generate a bill from the reading and approve it.
9. As finance, make a partial payment and confirm status becomes `PARTIALLY_PAID`.
10. As finance, pay the remaining balance and confirm status becomes `PAID`.
11. As customer, call own bills, payments, and notifications.
12. Try restricted endpoints with the wrong role and confirm `403 FORBIDDEN`.

## Example JSON

Signup:

```json
{
  "fullName": "Alice Admin",
  "email": "admin@example.com",
  "phoneNumber": "0780000001",
  "password": "Admin@12345",
  "role": "ROLE_ADMIN"
}
```

Tariff:

```json
{
  "utilityType": "WATER",
  "tariffType": "FLAT",
  "pricePerUnit": 120,
  "fixedServiceCharge": 500,
  "vatPercentage": 18,
  "penaltyPercentage": 5,
  "effectiveFrom": "2026-06-05",
  "effectiveTo": "2026-12-31",
  "tiers": []
}
```

Meter reading:

```json
{
  "meterId": 1,
  "previousReading": 0,
  "currentReading": 45,
  "readingDate": "2026-06-05",
  "month": 6,
  "year": 2026
}
```

Payment:

```json
{
  "billId": 1,
  "amountPaid": 1000,
  "paymentMethod": "MOBILE_MONEY",
  "paymentDate": "2026-06-05"
}
```

## Validation and Errors

The API rejects uppercase emails, invalid email formats, duplicate emails/phones/national IDs/meters, invalid Rwandan phone numbers, weak passwords, inactive customers/meters, negative readings, duplicate monthly readings, invalid tariff ranges, overpayments, future payment dates, and unauthorized role access.

Errors use this shape:

```json
{
  "timestamp": "2026-06-05T08:00:00",
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Current reading must be greater than previous reading",
  "path": "/api/meter-readings"
}
```

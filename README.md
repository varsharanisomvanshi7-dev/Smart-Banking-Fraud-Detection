# Smart Banking & Fraud Anomaly Detection System

A secure online banking backend built with **Java 21 + Spring Boot**, featuring
JWT authentication, ACID-compliant fund transfers, rule-based real-time fraud
detection, and PDF statement generation — matching the project spec provided.

## Tech Stack
- Java 21, Spring Boot 3.3
- Spring Security + JWT (jjwt 0.12)
- Spring Data JPA + PostgreSQL
- iText PDF (statement generation)
- Springdoc OpenAPI / Swagger UI
- Maven

## Project Structure
```
src/main/java/com/bank/frauddetection/
├── SmartBankingApplication.java     # entry point
├── config/                          # SecurityConfig, JwtAuthFilter
├── security/                        # JwtUtil, UserPrincipal, UserDetailsService
├── entity/                          # User, Account, Transaction, LoginHistory,
│                                       FraudAlert, AuditLog, RoleName
├── repository/                      # Spring Data JPA repositories
├── dto/                             # request/response payloads
├── service/                         # business logic
│   ├── AuthService.java             # register/login
│   ├── AccountService.java          # account lifecycle
│   ├── TransactionService.java      # deposit/withdraw/transfer (ACID)
│   ├── FraudDetectionService.java   # rule engine
│   ├── PdfStatementService.java     # iText statement PDF
│   ├── NotificationService.java     # alert dispatch (stub)
│   └── AuditService.java            # audit trail
├── controller/                      # REST controllers
└── exception/                       # global exception handling
```

## Setup

1. **Create the database**
   ```sql
   CREATE DATABASE smart_banking_db;
   ```

2. **Configure credentials** in `src/main/resources/application.properties`
   (or override via environment variables) — update `spring.datasource.username`,
   `spring.datasource.password`, and `jwt.secret` for your environment.

3. **Build & run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
   The API starts at `http://localhost:8080`.
   Swagger UI: `http://localhost:8080/swagger-ui.html`

4. **Promote a user to admin** (first admin must be set manually):
   ```sql
   UPDATE users SET role = 'ROLE_ADMIN' WHERE email = 'admin@example.com';
   ```

## Fraud Detection Rules Implemented
| Rule | Trigger | Severity |
|---|---|---|
| High Amount Transfer | amount > ₹1,00,000 (configurable) | HIGH |
| Frequent Transactions | > 5 transfers in 10 minutes | MEDIUM |
| Sudden Balance Withdrawal | withdrawal/transfer ≥ 80% of balance | HIGH |
| Multiple Login Locations | logins from >1 city within 1 hour | HIGH |
| Unknown Device Login | login from a device not seen before | MEDIUM |
| Multiple Failed Logins | ≥ 3 consecutive failed attempts → account auto-locked | HIGH |

Thresholds are configurable in `application.properties` under `fraud.rule.*`.

## Key API Endpoints

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/auth/register` | Register a new customer | Public |
| POST | `/api/auth/login` | Login, returns JWT | Public |
| POST | `/api/accounts` | Open a new account | Customer |
| GET | `/api/accounts` | List my accounts | Customer |
| GET | `/api/accounts/{accNo}/balance` | Check balance | Customer |
| POST | `/api/transactions/deposit` | Deposit funds | Customer |
| POST | `/api/transactions/withdraw` | Withdraw funds | Customer |
| POST | `/api/transactions/transfer` | Transfer funds | Customer |
| GET | `/api/transactions/history/{accNo}` | Transaction history | Customer |
| GET | `/api/transactions/statement/{accNo}` | Download PDF statement | Customer |
| GET | `/api/fraud-alerts/me` | My fraud alerts | Customer |
| GET | `/api/admin/customers` | List all customers | Admin |
| POST | `/api/admin/accounts/{accNo}/freeze` | Freeze an account | Admin |
| POST | `/api/admin/accounts/{accNo}/unfreeze` | Reactivate an account | Admin |
| GET | `/api/admin/fraud-alerts` | All fraud alerts | Admin |
| GET | `/api/admin/dashboard` | Summary stats | Admin |

All authenticated requests require header: `Authorization: Bearer <token>`

## Sample Flow
1. `POST /api/auth/register` → get JWT
2. `POST /api/accounts` → open account, note the `accountNumber`
3. `POST /api/transactions/deposit` → add funds
4. `POST /api/transactions/transfer` → send funds to another account
   (crossing ₹1,00,000 or transferring 5+ times in 10 minutes auto-raises a
   fraud alert, visible at `/api/fraud-alerts/me` and to admins)
5. `GET /api/transactions/statement/{accountNumber}` → download PDF

## Notes
- Passwords are hashed with BCrypt; never stored in plaintext.
- All money-movement operations run inside `@Transactional` methods — any
  failure (insufficient balance, frozen account) rolls back the entire
  operation, so no partial debit/credit is ever persisted.
- `NotificationService` currently logs alerts; swap in `JavaMailSender` or an
  SMS gateway for real email/SMS delivery.
- `spring.jpa.hibernate.ddl-auto=update` auto-creates tables on first run for
  convenience — use Flyway/Liquibase migrations for a production rollout.

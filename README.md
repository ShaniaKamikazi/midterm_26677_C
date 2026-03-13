# FinanceTrackerPlatform

A Spring Boot backend for personal finance management. The platform helps users manage accounts, categories, transactions, budgets, goals, recurring activity, notifications, reports, and profile settings in one place.

## What This Project Is

FinanceTrackerPlatform is a REST API built with Java and Spring Boot that supports:

- User registration, authentication, and profile settings
- Account and category management
- Transaction tracking with tags
- Budget planning and spending insights
- Financial goals and recurring transactions
- Notifications and report generation
- Location hierarchy support for user village mapping

## Tech Stack

- Java 17+
- Spring Boot 3.x
- Spring Web, Spring Security, Spring Data JPA, Validation
- PostgreSQL
- Maven

## How It Works

The application is structured in a standard layered architecture:

- Controllers: expose REST endpoints
- Services: business logic
- Repositories: data access with JPA
- Models/Entities: relational data model mapped with Hibernate

Data is stored in PostgreSQL. Hibernate manages schema updates (`ddl-auto=update`) and `data.sql` can seed default data.

## Setup Instructions

### 1. Prerequisites

- JDK 17 or newer
- Maven 3.9+
- PostgreSQL 14+

### 2. Configure Environment Variables

You can use the defaults from `application.properties`, but environment variables are recommended:

- `DB_URL` (example: `jdbc:postgresql://localhost:5432/finance_tracker_db`)
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `OTP_EMAIL_VERIFICATION` (true/false)
- `OTP_CODE_LENGTH`
- `OTP_EXPIRY_MINUTES`

### 3. Run the Application

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

### 4. Build and Test

```bash
./mvnw clean test
./mvnw clean package
```

## Database ERD (Mermaid)

Main ERD (optimized for readability):

```mermaid
erDiagram
    USER {
        UUID user_id PK
        UUID village_id FK
        string user_name UK
        string user_email UK
        enum account_type
        enum role_type
    }

    USER_SETTINGS {
        UUID user_id PK, FK
        string theme
        string language
        boolean two_factor_enabled
    }

    LOCATION {
        UUID location_id PK
        UUID parent_id FK
        string name
        enum location_type
    }

    ACCOUNT {
        UUID account_id PK
        UUID user_id FK
        string account_name
        decimal account_balance
    }

    CATEGORY {
        UUID category_id PK
        UUID user_id FK
        UUID parent_category_id FK
        string category_name
        enum category_type
    }

    TRANSACTION {
        UUID transaction_id PK
        UUID user_id FK
        UUID account_id FK
        UUID category_id FK
        decimal amount
        enum transaction_type
    }

    TAG {
        UUID tag_id PK
        UUID user_id FK
        string name
    }

    TRANSACTION_TAG {
        UUID transaction_id PK, FK
        UUID tag_id PK, FK
    }

    BUDGET {
        UUID budget_id PK
        UUID user_id FK
        UUID category_id FK
        decimal limit_amount
        date start_date
        date end_date
    }

    REPORT {
        UUID report_id PK
        UUID user_id FK
        date start_date
        date end_date
    }

    FINANCIAL_GOAL {
        UUID goal_id PK
        UUID user_id FK
        UUID account_id FK
        decimal target_amount
        decimal current_amount
    }

    RECURRING_TRANSACTION {
        UUID recurring_transaction_id PK
        UUID user_id FK
        UUID account_id FK
        UUID category_id FK
        decimal amount
        enum frequency
    }

    NOTIFICATION {
        UUID notification_id PK
        UUID user_id FK
        string title
        enum type
        boolean is_read
    }

    %% One-to-one
    USER ||--|| USER_SETTINGS : has_settings

    %% One-to-many / many-to-one
    LOCATION ||--o{ USER : village_of
    USER ||--o{ ACCOUNT : owns
    USER ||--o{ CATEGORY : defines
    USER ||--o{ TRANSACTION : records
    USER ||--o{ BUDGET : plans
    USER ||--o{ REPORT : generates
    USER ||--o{ TAG : creates
    USER ||--o{ FINANCIAL_GOAL : tracks
    USER ||--o{ RECURRING_TRANSACTION : schedules
    USER ||--o{ NOTIFICATION : receives

    ACCOUNT ||--o{ TRANSACTION : contains
    ACCOUNT ||--o{ FINANCIAL_GOAL : links
    ACCOUNT ||--o{ RECURRING_TRANSACTION : funds

    CATEGORY ||--o{ TRANSACTION : classifies
    CATEGORY ||--o{ BUDGET : scopes
    CATEGORY ||--o{ RECURRING_TRANSACTION : classifies

    %% Many-to-many via join table
    TRANSACTION ||--o{ TRANSACTION_TAG : mapped_by
    TAG ||--o{ TRANSACTION_TAG : mapped_by
```

Hierarchy ERD (self-referencing only):

```mermaid
erDiagram
    LOCATION {
        UUID location_id PK
        UUID parent_id FK
        string name
        enum location_type
    }

    CATEGORY {
        UUID category_id PK
        UUID parent_category_id FK
        string category_name
        enum category_type
    }

    LOCATION ||--o{ LOCATION : parent_of
    CATEGORY ||--o{ CATEGORY : parent_of
```

### Relationship Legend

- One-to-one: `USER` to `USER_SETTINGS`
- One-to-many: `USER` to `ACCOUNT` (and similar)
- Many-to-one: inverse of one-to-many, e.g., each `ACCOUNT` belongs to one `USER`
- Many-to-many: `TRANSACTION` to `TAG` via `TRANSACTION_TAG`
- Self-referencing: shown in the dedicated Hierarchy ERD (`LOCATION` to `LOCATION`, `CATEGORY` to `CATEGORY`) to avoid clutter

## Included ERD Image Exports

- `FinanceTracker_ERD.svg`
- `FinanceTracker_ERD.png`

## License

This project is for educational and development use.

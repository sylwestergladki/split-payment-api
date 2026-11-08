# Split Payment API

A REST API for sharing expenses within groups, tracking member balances, and recording transfers between members.

Built with **Java 21**, **Spring Boot 4**, and **Domain-Driven Design (DDD)** principles.

> **Project status:** Backend MVP. The API supports the core expense-sharing workflow. A web frontend is planned.

## Features

- **Identity and authentication:** Keycloak-based authentication using JWT; application users are associated with Keycloak subjects.
- **Groups:** Create groups, list a user's groups, view group details, and add members.
- **Expenses:** Record and retrieve shared expenses with equal splitting among selected participants.
- **Balances:** Calculate each group member's net balance from expenses and recorded transfers.
- **Settlement suggestions:** Suggest transfers to settle outstanding group balances.
- **Transfers (settlements):** Record transfers between group members and view their history.
- **Access control:** Group-specific operations check that the authenticated user belongs to the group.

### How balances work

A **positive** balance means the member should receive money; a **negative** balance means the member owes money.

For example, if Anna pays **100.00** for herself and John, their initial balances are:

| Member | Balance |
| --- | ---: |
| Anna | +50.00 |
| John | -50.00 |

If John records a transfer of **30.00** to Anna, their balances become **+20.00** and **-20.00**, respectively.

Transfers are intentionally **flexible**: the API does not require an existing debt, enforce a suggested direction, or cap transfers at the current balance. A recorded transfer is a **user-declared transaction**, not independently verified bank payment.

## Tech stack

| Area | Technology |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 4 |
| Security | Spring Security, OAuth 2.0 Resource Server, Keycloak, JWT |
| Persistence | Spring Data JPA / Hibernate |
| Database | PostgreSQL |
| Migrations | Flyway |
| Build | Gradle |
| Testing | JUnit 5, Mockito, Spring MVC tests, Testcontainers |
| API testing | Bruno |

## Architecture

The application is organized by business capability, with DDD-inspired layers inside each module:

```text
src/main/java/pl/sylwestergladki/split_payment_api/
├── identity/
│   ├── api/
│   ├── application/
│   ├── domain/
│   └── infrastructure/
├── groups/
│   ├── api/
│   ├── application/
│   ├── domain/
│   └── infrastructure/
└── expenses/
    ├── api/
    ├── application/
    ├── domain/
    └── infrastructure/
```

- **API:** REST controllers and request/response DTOs.
- **Application:** Use cases orchestrating business operations and authorization checks.
- **Domain:** Business models, value objects, repository interfaces, and calculation services.
- **Infrastructure:** JPA entities, Spring Data repositories, and persistence adapters.

The expense module includes `GroupBalanceCalculator` and `SettlementSuggestionCalculator` as domain services. Persistence is accessed through repository interfaces defined in the domain layer.

## API overview

All paths below are relative to the application base URL. Protected endpoints require a valid Bearer JWT.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/groups/{groupId}/balances` | Get member balances |
| `GET` | `/api/groups/{groupId}/settlements/suggestions` | Get suggested transfers |
| `POST` | `/api/groups/{groupId}/settlements` | Record a transfer |
| `GET` | `/api/groups/{groupId}/settlements` | Get transfer history |
| `POST` | `/api/groups/{groupId}/members` | Add a group member |

The application also provides endpoints for user registration, group creation/listing/details, and expense creation/listing. Refer to the controllers and Bruno collection for their exact routes and payloads.

### Record a transfer

```http
POST /api/groups/{groupId}/settlements
Authorization: Bearer <access-token>
Content-Type: application/json
```

```json
{
  "toUserId": "d9871e0d-2ac5-42e1-9d09-52b5ba9135a2",
  "amount": 30.00
}
```

The authenticated user is the sender. A successful request returns **201 Created** and the recorded transfer, including its ID and timestamp.

### Get transfer history

```http
GET /api/groups/{groupId}/settlements
Authorization: Bearer <access-token>
```

Returns an array of transfers, ordered newest first. Each item contains `id`, `groupId`, `fromUserId`, `toUserId`, `amount`, and `createdAt`.

## Getting started

### Prerequisites

- JDK **21**
- Docker, or an accessible **PostgreSQL** instance
- A running **Keycloak** instance configured to issue JWTs accepted by the API

### 1. Clone the repository

```bash
git clone <repository-url>
cd <repository-directory>
```

### 2. Configure infrastructure

Configure the application's PostgreSQL connection and Spring Security JWT issuer settings using the project's Spring configuration or environment variables. The Keycloak realm/client configuration must match the backend's security configuration.

> **Before publishing:** Add concrete local setup instructions, required environment variable names, and any Docker Compose commands used by this repository. Do not commit database passwords, client secrets, or access tokens.

### 3. Run the application

On macOS/Linux:

```bash
./gradlew bootRun
```

On Windows:

```powershell
.\gradlew.bat bootRun
```

Flyway migrations initialize and update the database schema as configured by the application.

### 4. Try the API

Use Bruno or another HTTP client with a valid Keycloak access token. The repository contains Bruno requests for group membership, balances, settlement suggestions, transfer recording, and transfer history.

For the full workflow: authenticate, register or resolve the application user, create a group, add members, record an expense, inspect balances and suggestions, and record a transfer.

## Testing

Run the complete test suite:

```bash
./gradlew test
```

The test suite includes:

- **Unit tests** for domain rules, balance calculation, settlement suggestions, and application use cases.
- **Web layer tests** for REST endpoints and authentication-related behavior.
- **Persistence integration tests** using Testcontainers with PostgreSQL.

Docker must be available when running Testcontainers-based tests.

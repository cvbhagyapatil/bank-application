# banking-transaction-processor

A Spring Boot service for processing banking transactions. It supports account management, deposits, withdrawals, transfers, validation against invalid amounts, and an in-memory per-account transaction ledger.

## Project Overview

- Language: Java 21
- Framework: Spring Boot 3
- Build: Maven
- Runtime: Spring Boot application with Actuator enabled

This service exposes HTTP endpoints to create accounts, deposit, withdraw, transfer funds, and query balances and transaction histories. It uses dependency injection, centralized error handling, and logging to support production-style observability.

## Architecture

- Model/Domain: `com.bank.model`
- Repository: `com.bank.repository`
- Service: `com.bank.service`
- Controller/API: `com.bank.controller`
- DTOs: `com.bank.dto`

The current implementation stores accounts in-memory and uses per-account locks plus atomic updates to preserve consistency.

## Build, Test, and Run

Prerequisites: Java 21, Maven 3.8+

Build:

```bash
cd c:\Workspace\bank-application\bank-application
mvn clean package
```

Run:

```bash
mvn spring-boot:run
```

Or run the packaged jar:

```bash
java -jar target/banking-service-1.0.0.jar
```

The service starts on `http://localhost:8080` by default.

Run tests:

```bash
mvn test
```

## Actuator Endpoints

With Actuator enabled, use:

- `http://localhost:8080/actuator/health`
- `http://localhost:8080/actuator/info`
- `http://localhost:8080/actuator`

## API Endpoints

Base URL: `http://localhost:8080/api`

### Create account

POST `/api/accounts`

Request body (optional):

```json
{ "initialBalance": 100.00 }
```

Response:

```json
{ "accountId": "<uuid>" }
```

If the body is omitted, the account is created with a zero balance.

### Get balance

GET `/api/accounts/{accountId}/balance`

Response:

```json
{ "balance": 100.00 }
```

### Get transactions

GET `/api/accounts/{accountId}/transactions`

Response: list of transaction records.

### Deposit

POST `/api/accounts/{accountId}/deposit`

Request body:

```json
{ "amount": 25.50, "description": "paycheck" }
```

### Withdraw

POST `/api/accounts/{accountId}/withdraw`

Request body:

```json
{ "amount": 10.00, "description": "atm" }
```

### Transfer

POST `/api/accounts/transfer`

Request body:

```json
{ "from": "<uuid-from>", "to": "<uuid-to>", "amount": 5.00, "description": "rent" }
```

## Error Handling

The service returns structured JSON errors for invalid requests and server failures:

- `400 Bad Request` for invalid input
- `409 Conflict` for business-state violations like insufficient funds
- `500 Internal Server Error` for unexpected errors

## Notes

- This project uses in-memory storage for accounts and transactions.
- For production, replace `AccountRepository` with a persistence-backed implementation.
- Logging and Actuator are enabled for better observability.

## Docker

Build and run with Docker Compose:

```bash
docker compose up --build
```

Or build and run manually:

```bash
docker build -t banking-service .
docker run -p 8080:8080 banking-service
```

The service is available at `http://localhost:8080/api`.

Notes:
- Docker uses a multi-stage build: Maven build image then a JRE runtime image.
- Ensure Docker Engine is installed and running on your machine.
=======
# banking-transaction-processor

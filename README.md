# 🔐 User Management System API

> **RBAC + Kafka + Audit Logging + JWT** — Spring Boot 3.5.11 · MySQL 8 · Apache Kafka · OAS 3.1

A production-ready User Management System with Role-Based Access Control, stateless JWT authentication, Kafka event streaming, audit logging, Flyway migrations, and full Docker support.

---

## 📋 Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [API Reference](#api-reference)
- [Design Decisions](#design-decisions)
- [Getting Started — Local Setup](#getting-started--local-setup)
- [Getting Started — Docker Setup](#getting-started--docker-setup)
- [Environment Variables](#environment-variables)
- [Database Schema & Migrations](#database-schema--migrations)
- [Default Admin Credentials](#default-admin-credentials)
- [Kafka Events](#kafka-events)
- [Swagger UI](#swagger-ui)
- [Running Tests](#running-tests)

---

## ✅ Features

- User Registration with BCrypt password hashing and email uniqueness validation
- JWT-based stateless login returning a signed Bearer token
- View current authenticated user profile (`/api/users/me`) — cached with Caffeine
- Role creation and assignment — Admin only, protected with `@PreAuthorize`
- Admin statistics endpoint with total users and recent login records
- **Kafka event publishing** on Registration and Login (async, non-blocking)
- **Kafka consumer** in the same app logs all received events
- Flyway database migrations (V1 schema + V2 admin seed)
- Global exception handling via `@ControllerAdvice` with structured JSON errors
- Bean Validation (JSR-380) on all request inputs
- Manual DTO mapping (no MapStruct)
- Audit logging (DB table) for every login and registration
- Swagger / OpenAPI 3.1 documentation at `/swagger-ui/index.html`
- Lombok for boilerplate reduction
- Multi-stage Dockerfile + full `docker-compose.yml`
- Unit tests + integration tests (Testcontainers for MySQL + EmbeddedKafka)

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.5.11 |
| Language | Java 21 |
| Security | Spring Security + JWT (jjwt 0.12.7) + BCrypt |
| Database | MySQL 8.3 + Spring Data JPA + Hibernate |
| Migrations | Flyway |
| Messaging | Apache Kafka (Confluent 7.6.0) + Zookeeper |
| Validation | Spring Validation (JSR-380) |
| DTO Mapping | Manual Java mappers |
| API Docs | Springdoc OpenAPI (Swagger UI) — OAS 3.1 |
| Boilerplate | Lombok |
| Caching | Spring Cache + Caffeine |
| Testing | JUnit 5 + Mockito + Testcontainers + EmbeddedKafka |
| Build | Maven (multi-stage Docker build) |
| Runtime | eclipse-temurin:21-jdk |

---

## 🏗 Architecture

```
HTTP Request
     │
     ▼
[ JwtAuthenticationFilter ]   ← Validates Bearer token, sets SecurityContext
     │
     ▼
[ Controller ]                ← @Valid input, delegates to service
     │
     ▼
[ Service ]                   ← Business logic, caching, Kafka publish, audit log
     │
     ▼
[ Repository ]                ← Spring Data JPA → MySQL

Kafka flow (async, fire-and-forget):
Service → EventPublisherService → KafkaTemplate → user-events topic
                                                        ↓
                                               UserEventConsumer (logs event)
```

---

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/management/
│   │   ├── config/
│   │   │   ├── KafkaProducerConfig.java     ← Reads bootstrap from application.properties
│   │   │   ├── KafkaConsumerConfig.java     ← Full consumer factory with JSON deserializer
│   │   │   ├── SecurityConfig.java
│   │   │   ├── JwtService.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── OpenApiConfig.java
│   │   ├── consumer/
│   │   │   └── UserEventConsumer.java       ← @KafkaListener logs all events
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── UserController.java
│   │   │   ├── RoleController.java
│   │   │   └── AdminController.java
│   │   ├── service/imp/
│   │   │   ├── AuthServiceImpl.java
│   │   │   ├── UserServiceImpl.java
│   │   │   ├── AdminServiceImpl.java
│   │   │   ├── RoleServiceImpl.java
│   │   │   ├── EventPublisherServiceImpl.java
│   │   │   └── AuditServiceImpl.java
│   │   ├── dto/
│   │   │   ├── event/UserEvent.java
│   │   │   ├── request/
│   │   │   └── response/
│   │   ├── entity/, mapper/, repository/, exception/
│   └── resources/
│       ├── application.properties           ← Main config with ${ENV:default} patterns
│       ├── application-local.properties     ← Local dev overrides (localhost Kafka/MySQL)
│       └── db/migration/
│           ├── V1__init_schema.sql          ← Creates all tables
│           └── V2__insert_admin_role.sql    ← Seeds admin user + roles
└── test/
    ├── java/com/management/
    │   ├── AuthServiceImplTest.java
    │   ├── UserServiceTest.java
    │   ├── RoleServiceTest.java
    │   └── AdminServiceTest.java
    └── resources/application-test.properties
```

---

## 🔌 API Reference

Full interactive docs at `http://localhost:8080/swagger-ui/index.html`

### `POST /api/users/register` — Register new user

```json
// Request
{ "username": "john_doe", "email": "user@example.com", "password": "SecurePass1!" }

// 201 Created
{ "id": 1, "username": "john_doe", "email": "user@example.com", "message": "User registered successfully. Please login." }

// 409 Conflict  →  Email already registered
// 400 Bad Request  →  Validation failed
```

### `POST /api/users/login` — Login and get JWT

```json
// Request
{ "email": "user@example.com", "password": "SecurePass1!" }

// 200 OK
{ "token": "eyJhbGciOiJIUzI1NiJ9..." }

// 401 Unauthorized  →  Invalid email or password
```

Use the token on all secured endpoints: `Authorization: Bearer <token>`

### `GET /api/users/me` — Current user profile 🔒

```json
// 200 OK
{ "id": 1, "username": "john_doe", "email": "user@example.com", "roles": ["ROLE_USER"] }

// 401  →  Missing or invalid token
```

### `POST /api/users/{userId}/roles?roleName=ROLE_ADMIN` — Assign role 🔒 ADMIN

```
// 200 OK   →  Role assigned
// 404      →  User or role not found
// 409      →  Role already assigned to this user
```

### `POST /api/roles?name=ROLE_MODERATOR` — Create role 🔒 ADMIN

```
// 201 Created  →  Role created
// 409 Conflict →  Role already exists
```

### `GET /api/admin/stats` — System statistics 🔒 ADMIN

```json
// 200 OK
{
  "totalUsers": 5,
  "recentLogins": [
    { "userId": 1, "email": "admin@system.com", "lastLogin": "2026-02-21T10:30:00" }
  ]
}
// 403 Forbidden  →  Not ADMIN
```

---

## 💡 Design Decisions

**Kafka configuration via environment** — `KafkaProducerConfig` and `KafkaConsumerConfig` both read `spring.kafka.bootstrap-servers` from `application.properties`, which resolves to `${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}`. This means the same binary works for local dev (`localhost:9092`) and Docker (`kafka:9092`) without any code change.

**LocalDateTime in Kafka** — `UserEvent` uses `LocalDateTime`. Jackson requires the `JavaTimeModule` to serialize this correctly. Both `KafkaProducerConfig` and `KafkaConsumerConfig` register this module explicitly, and `ADD_TYPE_INFO_HEADERS` is disabled so the consumer doesn't need to resolve a fully-qualified class name from the message header.

**Flyway migrations** — Schema is managed by Flyway, not `ddl-auto=create`. V1 creates all tables. V2 seeds the default admin user and ROLE_ADMIN/ROLE_USER. DDL auto is set to `validate` in Docker so Hibernate just checks the schema on startup.

**Manual DTO mapping** — `UserMapper` is a plain `@Component` with explicit mapping methods. No MapStruct — keeps compilation simple and mapping fully transparent.

**Caffeine cache** — `/api/users/me` is cached per email key. The cache evicts when a role is assigned to that user. Caffeine is the fastest in-memory JVM cache; can be swapped for Redis by changing `spring.cache.type=redis` and adding the Redis starter.

**Audit log** — Every login and registration writes a record to the `audit_logs` table via `AuditService`. This is separate from the Kafka event so audit records survive even if the broker is temporarily down.

**Multi-stage Docker build** — Stage 1 (`maven:3.9.9-eclipse-temurin-21`) downloads dependencies offline and produces the fat JAR. Stage 2 (`eclipse-temurin:21-jdk`) copies only the JAR — final image is lean with no Maven toolchain.

**Health-check ordering** — `docker-compose.yml` uses `condition: service_healthy` for both MySQL and Kafka before starting the app. MySQL health check uses `mysqladmin ping`. Kafka health check uses `kafka-broker-api-versions`. This prevents the app from crashing on startup due to an unready broker.

---

## 🚀 Getting Started — Local Setup

### Prerequisites

- Java 21+
- Maven 3.9+
- MySQL 8 running locally
- Kafka + Zookeeper (or use Docker just for infra — see below)

### Step 1 — Start only MySQL + Kafka via Docker (easiest)

```bash
docker-compose up -d mysql zookeeper kafka
```

Wait ~30 seconds for Kafka to be ready, then:

```bash
docker-compose ps
# mysql-db, zookeeper, kafka should all be "healthy" or "running"
```

### Step 2 — Run the Spring Boot app

```bash
cd user-management-system

# Option A: with local profile (recommended — no env vars needed)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Option B: with explicit env vars
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export MYSQL_HOST=localhost
export MYSQL_DATABASE=userdb
export MYSQL_USERNAME=root
export MYSQL_PASSWORD=root
export JWT_SECRET=dev-secret-key-must-be-at-least-32-chars-long!!
export DDL_AUTO=validate
./mvnw spring-boot:run
```

App starts at `http://localhost:8080`  
Swagger UI: `http://localhost:8080/swagger-ui/index.html`

---

## 🐳 Getting Started — Docker Setup

### Step 1 — Clone

```bash
git clone https://github.com/your-username/user-management-system.git
cd user-management-system
```

### Step 2 — Build and start everything

```bash
docker-compose up --build
```

**What this starts:**

| Container | Image | Port |
|---|---|---|
| `user-management-app` | Built from `Dockerfile` | `8080` |
| `mysql-db` | `mysql:8.3.0` | `3306` |
| `zookeeper` | `confluentinc/cp-zookeeper:7.6.0` | `2181` |
| `kafka` | `confluentinc/cp-kafka:7.6.0` | `9092` (internal) · `9093` (host) |

> The app waits for MySQL and Kafka health checks to pass before starting. First boot takes ~60 seconds.

### Step 3 — Verify

```bash
docker-compose ps          # all containers should be healthy
curl http://localhost:8080/swagger-ui/index.html
```

### Stop / Clean up

```bash
docker-compose down           # stop containers
docker-compose down -v        # stop + wipe database volume
```
```
watch it live from a new terminal:
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic user-events --from-beginning

**** Clean up old containers **** dont forget to clean up **********
docker-compose down
docker rm mysql-db zookeeper kafka user-management-app 2>nul
```
---

## ⚙️ Environment Variables

| Variable | Default (local) | Docker value | Description |
|---|---|---|---|
| `SERVER_PORT` | `8080` | `8080` | App port |
| `JWT_SECRET` | `dev-secret-...` | Set in compose | HMAC-SHA256 key (min 32 chars) |
| `JWT_EXPIRATION` | `3600000` | `3600000` | Token TTL in ms (1 hour) |
| `MYSQL_HOST` | `localhost` | `mysql` | MySQL hostname |
| `MYSQL_PORT` | `3306` | `3306` | MySQL port |
| `MYSQL_DATABASE` | `userdb` | `userdb` | Database name |
| `MYSQL_USERNAME` | `root` | `root` | MySQL user |
| `MYSQL_PASSWORD` | `root` | `root` | MySQL password |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | `kafka:9092` | Kafka broker address |
| `DDL_AUTO` | `validate` | `validate` | Hibernate DDL strategy |

Copy `.env.example` → `.env` for local overrides.

---

## 🗄 Database Schema & Migrations

Managed by **Flyway**. Migrations run automatically on startup.

| Migration | File | Description |
|---|---|---|
| V1 | `V1__init_schema.sql` | Creates `users`, `roles`, `user_roles`, `audit_logs` tables |
| V2 | `V2__insert_admin_role.sql` | Inserts `ROLE_ADMIN`, `ROLE_USER`, and default admin user |

### Schema Overview

```sql
-- users, roles, user_roles (many-to-many), audit_logs
-- See src/main/resources/db/migration/ for full DDL
```

---

## 🔑 Default Admin Credentials

Seeded by `V2__insert_admin_role.sql`:

| Field | Value |
|---|---|
| Email | `admin@system.com` |
| Password | `Admin@12345` |
| Role | `ROLE_ADMIN` |

Login immediately after startup:
```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@system.com","password":"Admin@12345"}'
```

---

## 📨 Kafka Events

All events are published to the **`user-events`** topic and consumed by `UserEventConsumer` in the same application (logged at INFO level).

### Event payload (`UserEvent`)

```json
{
  "userId": 1,
  "email": "user@example.com",
  "eventType": "REGISTERED",
  "timestamp": "2026-02-21T10:00:00"
}
```

`eventType` is either `REGISTERED` or `LOGIN`.

### Monitor events from terminal

```bash
# Inside Docker
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic user-events \
  --from-beginning

# From host machine (uses external listener on port 9093)
kafka-console-consumer.sh \
  --bootstrap-server localhost:9093 \
  --topic user-events \
  --from-beginning
```

---

## 📖 Swagger UI

| URL | Description |
|---|---|
| `http://localhost:8080/swagger-ui/index.html` | Interactive Swagger UI |
| `http://localhost:8080/v3/api-docs` | Raw OpenAPI 3.1 JSON |

### Authenticate in Swagger UI

1. Call `POST /api/users/login` → copy `token` value
2. Click **Authorize 🔒** (top right)
3. Enter: `Bearer <your-token>` → click **Authorize**
4. All 🔒 endpoints now send your token automatically

---

## 🧪 Running Tests

```bash
# All tests (unit + integration via Testcontainers)
./mvnw test

# Unit tests only (no Docker needed)
./mvnw test -Dtest="AuthServiceImplTest,UserServiceTest,RoleServiceTest,AdminServiceTest"
```

Integration tests use:
- **Testcontainers** — spins up a real MySQL 8 container automatically
- **EmbeddedKafka** — in-process Kafka broker, no external broker needed

---


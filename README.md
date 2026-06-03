# E-Commerce Microservices

A stakeholder-facing working prototype of an e-commerce platform built on a microservices architecture.

## Architecture

```
Client (Next.js)
    │
    ▼
API Gateway (port 8080)   ← JWT validation happens here
    │
    ├── Auth Service        (port 8081)
    ├── Product Service     (port 8084)
    ├── Order Service       (port 8082)
    ├── Notification Service(port 8083)
    └── Merchant Service    (port 8085)
```

## Services

| Service | Port | Description |
|---|---|---|
| API Gateway | 8080 | JWT validation, routing, Swagger aggregation |
| Auth Service | 8081 | Register, login, JWT issuance |
| Order Service | 8082 | Place orders, order status |
| Notification Service | 8083 | Order confirmation emails |
| Product Service | 8084 | Product catalog, stock management |
| Merchant Service | 8085 | Sales dashboard, stock replenishment |

## Infrastructure

| Component | Port | Purpose |
|---|---|---|
| PostgreSQL (auth) | 5432 | Auth service database |
| PostgreSQL (order) | 5433 | Order service database |
| PostgreSQL (product) | 5434 | Product service database |
| PostgreSQL (merchant) | 5435 | Merchant service database |
| PostgreSQL (notification) | 5436 | Notification service database |
| Redis | 6379 | Product catalog cache |
| RabbitMQ | 5672 / 15672 | Async event messaging |
| Mailhog | 1025 / 8025 | Local email testing |

## Quick Start

```bash
# Start all infrastructure and services
docker-compose up --build

# Start only infrastructure (for local development)
docker-compose up postgres-auth postgres-order postgres-product postgres-merchant postgres-notification redis rabbitmq mailhog
```

## URLs

| Service | URL |
|---|---|
| Swagger UI (all services) | http://localhost:8080/swagger-ui.html |
| RabbitMQ Management | http://localhost:15672 (guest / guest) |
| Mailhog Web UI | http://localhost:8025 |

## Authentication

> ⚠️ **MVP only — rotate before production**

JWT is signed with a hardcoded HMAC secret:

```
ecommerce-jwt-secret-key-2026
```

This value is set in `api-gateway/src/main/resources/application.yml` and `auth-service/src/main/resources/application.yml`.

**Using Swagger UI with JWT:**
1. Call `POST /api/auth/login` → copy the token from the response
2. Click the **Authorize** button at the top of Swagger UI
3. Enter `Bearer <your-token>` → all protected endpoints are now accessible

## Async Order Flow

Orders are processed asynchronously:

```
POST /api/orders → 202 Accepted {orderId, status: "PENDING"}
    ↓
Product Service checks stock (via RabbitMQ)
    ├── Stock OK  → order.confirmed → email sent + status = CONFIRMED
    └── No stock  → order.failed   → status = FAILED

Poll GET /api/orders/{id} until status != PENDING
```

## Tech Stack

- **Frontend**: Next.js (App Router)
- **Backend**: Spring Boot 3.x
- **Gateway**: Spring Cloud Gateway
- **Auth**: JWT (HMAC HS256, `jjwt` library)
- **Messaging**: RabbitMQ
- **Cache**: Redis (product catalog only)
- **Database**: PostgreSQL (one per service)
- **Email**: Mailhog (local SMTP mock)
- **API Docs**: SpringDoc OpenAPI (Swagger UI)
- **Containers**: Docker Compose

## Branching Strategy

Each branch is a self-contained, runnable slice of the system:

| Branch | What you can test |
|---|---|
| `feature/auth` | Register, login, JWT, 401 enforcement |
| `feature/product` | Product catalog, HTML descriptions, stock |
| `feature/order-flow` | Place order → Pending → Confirmed/Failed |
| `feature/notification` | Order confirmed → email in Mailhog |
| `feature/merchant` | Sales view, stock replenishment |
| `feature/frontend` | Full customer flow in browser |

Each branch has its own scoped `docker-compose.yml` that only starts the containers needed for that slice.

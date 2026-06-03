# Architecture Diagram

## System Overview

```mermaid
graph TB
    subgraph Client
        FE["Next.js Frontend<br/><i>:3000</i>"]
    end

    subgraph Gateway
        GW["API Gateway<br/><i>Spring Cloud Gateway :8080</i>"]
        JWT{{"JWT Filter<br/><i>HMAC-SHA256</i>"}}
    end

    subgraph Services
        AUTH["Auth Service<br/><i>:8081</i>"]
        PROD["Product Service<br/><i>:8084</i>"]
        ORD["Order Service<br/><i>:8082</i>"]
        NOTIF["Notification Service<br/><i>:8083</i>"]
        MERCH["Merchant Service<br/><i>:8085</i>"]
    end

    subgraph Databases
        PG_AUTH[("PostgreSQL<br/>authdb<br/><i>:5432</i>")]
        PG_PROD[("PostgreSQL<br/>productdb<br/><i>:5434</i>")]
        PG_ORD[("PostgreSQL<br/>orderdb<br/><i>:5433</i>")]
        PG_NOTIF[("PostgreSQL<br/>notificationdb<br/><i>:5436</i>")]
        PG_MERCH[("PostgreSQL<br/>merchantdb<br/><i>:5435</i>")]
    end

    subgraph Cache
        REDIS[("Redis<br/><i>:6379</i>")]
    end

    subgraph Messaging
        RMQ["RabbitMQ<br/><i>:5672 / :15672</i>"]
    end

    subgraph Email
        MH["Mailhog<br/><i>SMTP :1025 / UI :8025</i>"]
    end

    FE -->|"REST/JSON"| GW
    GW --> JWT
    JWT -->|"Public paths pass through"| AUTH
    JWT -->|"Validates token"| PROD
    JWT -->|"Validates token"| ORD
    JWT -->|"Validates token"| MERCH

    AUTH --> PG_AUTH
    PROD --> PG_PROD
    PROD --> REDIS
    ORD --> PG_ORD
    NOTIF --> PG_NOTIF
    MERCH --> PG_MERCH

    ORD -->|"order.placed"| RMQ
    RMQ -->|"order.placed"| PROD
    PROD -->|"order.confirmed /<br/>order.failed"| RMQ
    RMQ -->|"order.confirmed /<br/>order.failed"| ORD
    RMQ -->|"order.confirmed"| NOTIF
    NOTIF -->|"SMTP"| MH

    MERCH -->|"REST (WebClient)"| PROD
    MERCH -->|"REST (WebClient)"| ORD

    style FE fill:#1e1e2e,stroke:#cdd6f4,color:#cdd6f4
    style GW fill:#1e1e2e,stroke:#89b4fa,color:#89b4fa
    style JWT fill:#313244,stroke:#f9e2af,color:#f9e2af
    style AUTH fill:#1e1e2e,stroke:#a6e3a1,color:#a6e3a1
    style PROD fill:#1e1e2e,stroke:#a6e3a1,color:#a6e3a1
    style ORD fill:#1e1e2e,stroke:#a6e3a1,color:#a6e3a1
    style NOTIF fill:#1e1e2e,stroke:#a6e3a1,color:#a6e3a1
    style MERCH fill:#1e1e2e,stroke:#a6e3a1,color:#a6e3a1
    style PG_AUTH fill:#1e1e2e,stroke:#89b4fa,color:#89b4fa
    style PG_PROD fill:#1e1e2e,stroke:#89b4fa,color:#89b4fa
    style PG_ORD fill:#1e1e2e,stroke:#89b4fa,color:#89b4fa
    style PG_NOTIF fill:#1e1e2e,stroke:#89b4fa,color:#89b4fa
    style PG_MERCH fill:#1e1e2e,stroke:#89b4fa,color:#89b4fa
    style REDIS fill:#1e1e2e,stroke:#f38ba8,color:#f38ba8
    style RMQ fill:#1e1e2e,stroke:#fab387,color:#fab387
    style MH fill:#1e1e2e,stroke:#cba6f7,color:#cba6f7
```

## Async Order Saga

```mermaid
sequenceDiagram
    participant C as Client
    participant GW as API Gateway
    participant OS as Order Service
    participant RMQ as RabbitMQ
    participant PS as Product Service
    participant NS as Notification Service
    participant MH as Mailhog

    C->>GW: POST /api/orders
    GW->>GW: Validate JWT
    GW->>OS: Forward request + X-User-Id
    OS->>OS: Save order (PENDING)
    OS-->>C: 202 Accepted {orderId, status: PENDING}
    OS->>RMQ: Publish order.placed

    RMQ->>PS: Consume order.placed

    alt Stock Available
        PS->>PS: UPDATE stock_count -= qty<br/>WHERE stock_count >= qty
        PS->>RMQ: Publish order.confirmed
        RMQ->>OS: Consume order.confirmed
        OS->>OS: Update status = CONFIRMED
        RMQ->>NS: Consume order.confirmed
        NS->>MH: Send confirmation email
    else Insufficient Stock
        PS->>RMQ: Publish order.failed
        RMQ->>OS: Consume order.failed
        OS->>OS: Update status = FAILED
    end

    C->>GW: GET /api/orders/{id} (poll)
    GW->>OS: Forward request
    OS-->>C: {status: CONFIRMED | FAILED}
```

## RabbitMQ Topology

```mermaid
graph LR
    subgraph Exchange
        EX["ecommerce.events<br/><i>topic exchange</i>"]
    end

    subgraph Queues
        Q1["product.order.placed"]
        Q2["order.status.confirmed"]
        Q3["order.status.failed"]
        Q4["notification.order.confirmed"]
    end

    subgraph Consumers
        PS["Product Service"]
        OS1["Order Service"]
        OS2["Order Service"]
        NS["Notification Service"]
    end

    EX -->|"order.placed"| Q1
    EX -->|"order.confirmed"| Q2
    EX -->|"order.confirmed"| Q4
    EX -->|"order.failed"| Q3

    Q1 --> PS
    Q2 --> OS1
    Q3 --> OS2
    Q4 --> NS

    style EX fill:#1e1e2e,stroke:#fab387,color:#fab387
    style Q1 fill:#1e1e2e,stroke:#89b4fa,color:#89b4fa
    style Q2 fill:#1e1e2e,stroke:#a6e3a1,color:#a6e3a1
    style Q3 fill:#1e1e2e,stroke:#f38ba8,color:#f38ba8
    style Q4 fill:#1e1e2e,stroke:#cba6f7,color:#cba6f7
```

## Service Communication Matrix

```
                    ┌──────────┐
                    │  Client  │
                    │ (Next.js)│
                    └────┬─────┘
                         │ REST/JSON
                         ▼
               ┌─────────────────────┐
               │    API Gateway      │
               │  JWT Validation     │
               │  Route Forwarding   │
               │  Swagger Aggregation│
               └──┬──┬──┬──┬──┬─────┘
                  │  │  │  │  │
         ┌────────┘  │  │  │  └────────┐
         ▼           ▼  │  ▼           ▼
    ┌─────────┐  ┌──────┤  ┌────────┐  ┌──────────┐
    │  Auth   │  │Product│  │ Order  │  │ Merchant │
    │ Service │  │Service│  │Service │  │ Service  │
    └────┬────┘  └──┬─┬─┘  └──┬──┬──┘  └──┬───┬───┘
         │          │ │        │  │         │   │
         ▼          ▼ ▼        ▼  │         │   │
    ┌────────┐  ┌─────┐┌─────┐┌──┴──┐      │   │
    │postgres│  │pg   ││Redis││pg   │      │   │
    │ auth   │  │prod ││     ││order│      │   │
    └────────┘  └─────┘└─────┘└─────┘      │   │
                   ▲                        │   │
                   │  ┌─────────────────────┘   │
                   │  │ REST (WebClient)         │
                   │  ▼                          │
                   │  Product Service ◄──────────┘
                   │                    REST (WebClient)
                   │
              ┌────┴────┐
              │RabbitMQ  │──── order.placed ────► Product Service
              │          │◄─── order.confirmed ── Product Service
              │          │──── order.confirmed ──► Notification Service
              │          │──── order.confirmed ──► Order Service
              │          │──── order.failed ─────► Order Service
              └──────────┘
                                    │
                          ┌─────────┘
                          ▼
                    ┌───────────┐
                    │  Mailhog  │
                    │ SMTP Mock │
                    └───────────┘
```

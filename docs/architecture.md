# KitchenPulse architecture

## Why this shape

Interviewers have seen six-service e-commerce + Eureka + Keycloak. This repo is a **kitchen order spine**: user service (auth + RBAC), three business services, one edge, polyglot persistence, and Kafka only where the work is async.

```
Angular (signals)
        |
        v
   gateway :8088   JWT + CORS + RBAC + routes. No Eureka.
        |
        +-- /api/auth/**, /api/users/** -> user-service :8084 (Postgres: users + roles)
        +-- /api/orders/**    -> order-service :8081   (Postgres: orders + outbox)
        +-- /api/inventory/** -> inventory-service :8082  (Mongo: menu + stock)
        +-- /api/kitchen/**   -> notify-service :8083     (Mongo: tickets)
                    |
                    |  sync Feign + Resilience4j
                    v
              inventory /internal/reservations
                    |
                    |  after commit: outbox -> Redpanda
                    v
              notify-service consumer  (retry + DLQ)
```

## Why Postgres and Mongo both exist

| Store | Owner | Reason |
|-------|--------|--------|
| PostgreSQL | user-service | Users, roles (`STAFF`, `KITCHEN`, `ADMIN`), JWT issuance |
| PostgreSQL | order-service | ACID place-order, unique `idempotency_key`, transactional outbox |
| MongoDB | inventory-service | Menu item documents + stock counters |
| MongoDB | notify-service | Kitchen tickets (append-heavy, no joins) |

Gateway has **no database**. Kafka/Redpanda is **not** a source of truth; it is the async kitchen ticket pipe.

## What we cut

Eureka, Config Server, Keycloak, Kubernetes, Grafana. Discovery is Compose DNS (`order-service`, `inventory-service`, …). Auth is JWT at the gateway (same secret on services for defense in depth). Observability is Spring Actuator health + metrics.

# KitchenPulse

**A focused, event-driven kitchen order platform** — place orders, reserve stock synchronously, publish domain events through a **transactional outbox**, and surface **kitchen tickets** asynchronously. Built as a portfolio-grade microservices sample: small enough to run on a laptop, rich enough to discuss in system design interviews.

> Not another six-service e-commerce clone. KitchenPulse is a **kitchen order spine** with deliberate trade-offs: polyglot persistence, JWT at the edge, Kafka only where work is async, and no Eureka / Config Server / Keycloak overhead.

---

## Highlights

| Capability | What it demonstrates |
|------------|----------------------|
| **Idempotent ordering** | `Idempotency-Key` header; duplicate requests return the same order without double stock deduction |
| **Sync + async boundary** | Feign + Resilience4j to inventory, then outbox → Redpanda/Kafka for kitchen notifications |
| **Polyglot persistence** | PostgreSQL (orders, users, outbox) + MongoDB (menu, stock, tickets) — each store owned by one service |
| **Operational realism** | Retry + DLQ on the consumer; circuit breaker when inventory is unavailable |
| **Modern frontend** | Angular 19 standalone components, signals, lazy routes, and `resource()` for data loading |

---

## Architecture

```
Angular (POS + Kitchen board)
            │
            ▼
     API Gateway :8088          JWT validation, CORS, routing
            │
            ├── /api/auth/**, /api/orders/**  →  order-service :8081   (PostgreSQL)
            ├── /api/inventory/**             →  inventory-service :8082 (MongoDB)
            └── /api/kitchen/**               →  notify-service :8083    (MongoDB)
                         │
                         │  sync: POST /internal/reservations (Feign)
                         ▼
                 inventory-service
                         │
                         │  after DB commit: outbox publisher
                         ▼
                    Redpanda/Kafka  →  notify-service (retry + DLQ)  →  kitchen tickets
```

Deeper design notes: [docs/architecture.md](docs/architecture.md).  
**Learning microservices from this repo:** [docs/microservices-guide.md](docs/microservices-guide.md).  
**API docs (Swagger):** [http://localhost:8088/swagger-ui.html](http://localhost:8088/swagger-ui.html) — see [docs/swagger.md](docs/swagger.md).

---

## Tech stack

| Layer | Technologies |
|-------|----------------|
| **Backend** | Java 17, Spring Boot 3.5, Spring Cloud Gateway, OpenFeign, Resilience4j, Spring Security (JWT), Flyway |
| **Messaging** | Apache Kafka API via **Redpanda** (dev-friendly broker) |
| **Data** | PostgreSQL 16, MongoDB 7 |
| **Frontend** | Angular 19, standalone components, signals |
| **Runtime** | Docker Compose for infrastructure (and optional full stack profile) |

---

## Prerequisites

Install the following before you run the project locally:

- **JDK 17+**
- **Node.js 20+** and npm (for the Angular app)
- **Docker Desktop** (or Docker Engine + Compose) for Postgres, MongoDB, and Redpanda

Optional: **IntelliJ IDEA** or VS Code with Java extensions — recommended for running microservices from the IDE.

---

## Quick start (recommended for development)

This path runs databases and Kafka in Docker and starts Java services from your IDE (fastest feedback loop).

### 1. Clone the repository

```bash
git clone https://github.com/02Raj/kitchenpulse.git
cd kitchenpulse
```

### 2. Start infrastructure

```bash
docker compose up -d postgres mongo redpanda
```

Wait until containers are healthy (`docker compose ps`).

### 3. Start backend services (in this order)

Open four run configurations (or terminals) and start each Spring Boot application:

1. `inventory-service` → port **8082**
2. `order-service` → port **8081**
3. `notify-service` → port **8083**
4. `gateway` → port **8088**

From each service directory you can also build and run:

```bash
# Linux / macOS
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

### 4. Start the web app

```bash
cd web
npm install
npm start
```

### 5. Open the application

| Resource | URL |
|----------|-----|
| **Web UI** | http://localhost:4200 |
| **API gateway** | http://localhost:8088 |
| **Gateway health** | http://localhost:8088/actuator/health |

**Demo account** (seeded on first order-service startup):

- Email: `chef@kitchenpulse.dev`
- Password: `chef12345`

You can also register a new user from the **Register** screen.

---

## How to use the application

After logging in, you will see two main areas in the header: **POS** and **Kitchen board**.

### POS — place an order

1. Go to **POS** (`/orders`).
2. The menu loads from inventory (demo items: Classic burger, Fries, Vanilla shake).
3. Click **Order 1** on any item.
4. The UI sends `POST /api/orders` with a fresh `Idempotency-Key` (UUID) per click.
5. On success, stock counts refresh and you are prompted to check the kitchen board.

**What happens behind the scenes:** order-service persists the order in PostgreSQL, calls inventory to reserve stock synchronously, writes an outbox event, and a background publisher emits to Kafka. notify-service consumes the event and creates a kitchen ticket in MongoDB.

### Kitchen board — see tickets

1. Go to **Kitchen board** (`/kitchen`).
2. Tickets appear after notify-service processes `kitchen.orders.created`.
3. The page auto-refreshes every few seconds; use **Refresh** for an immediate reload.

Each ticket shows order id, station (e.g. `GRILL`, `FRY`, `BAR`), status, and line items.

### Logout

Use **Logout** in the header to clear the JWT and return to the login page.

---

## Demo scenarios (great for interviews or README walkthroughs)

Try these flows to show how the system behaves under real constraints:

1. **Happy path** — Log in → order a burger → confirm ticket on kitchen board → note decreased stock on POS.
2. **Idempotency** — Replay the same `POST /api/orders` with the **same** `Idempotency-Key` (e.g. via curl or REST client). You should get the same order id and no extra inventory reservation.
3. **Resilience** — Stop `inventory-service`, place a new order. Expect a failed order with a clear error; with Resilience4j, the order path degrades predictably (e.g. **503** / circuit breaker behavior).
4. **Async path** — With all services up, place an order and watch the ticket land on the kitchen board after the Kafka consumer runs.

---

## Run everything with Docker (full profile)

To run all microservices in containers (after building JARs):

```bash
cd gateway && ./gradlew bootJar && cd ..
cd order-service && ./gradlew bootJar && cd ..
cd inventory-service && ./gradlew bootJar && cd ..
cd notify-service && ./gradlew bootJar && cd ..

docker compose --profile full up --build
```

The Angular app is still started locally (`cd web && npm start`) unless you add your own container image for the UI.

---

## Repository layout

| Path | Responsibility |
|------|----------------|
| `gateway/` | Spring Cloud Gateway — JWT, CORS, route to backend services |
| `order-service/` | Registration, login, JWT issuance, orders, transactional outbox |
| `inventory-service/` | Menu catalog, stock, internal reservation API |
| `notify-service/` | Kafka consumer, retries, DLQ, kitchen ticket API |
| `web/` | Angular POS and kitchen board |
| `docker-compose.yml` | Postgres, MongoDB, Redpanda; optional `full` profile for all apps |
| `docs/architecture.md` | Rationale for service boundaries and data stores |

---

## API overview (via gateway)

All authenticated routes expect `Authorization: Bearer <token>` except auth endpoints.

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/auth/register` | Create account |
| `POST` | `/api/auth/login` | Obtain JWT |
| `POST` | `/api/orders` | Place order (header: `Idempotency-Key`) |
| `GET` | `/api/orders` | List orders |
| `GET` | `/api/inventory/items` | Menu and stock |
| `GET` | `/api/kitchen/tickets` | Kitchen tickets |

Internal service-to-service reservation: `POST /internal/reservations` on inventory-service (not exposed through the gateway for clients).

---

## Configuration

| Variable | Purpose | Default (local) |
|----------|---------|-----------------|
| `JWT_SECRET` | Shared secret for gateway and services | Dev-only value in Compose — **change in production** |
| `CORS_ORIGINS` | Allowed browser origins | `http://localhost:4200` |
| `DATABASE_URL` | order-service JDBC URL | `jdbc:postgresql://localhost:5433/kitchenpulse` |
| `MONGODB_URI` | inventory / notify Mongo connection | See `application.yml` per service |
| `KAFKA_BOOTSTRAP_SERVERS` | Broker address | `localhost:19092` (host) / `redpanda:9092` (Compose network) |

Host port mapping (to avoid clashes with other local databases):

- PostgreSQL **5433** → container 5432  
- MongoDB **27018** → container 27017  
- Kafka **19092** → Redpanda external listener  

---

## Observability

Spring Boot Actuator is enabled on services:

- Gateway: `http://localhost:8088/actuator/health`
- Order service metrics: `http://localhost:8081/actuator/metrics`

Use these endpoints to verify the stack during demos or CI smoke checks.

---

## What we intentionally left out

To keep the story clear and the repo maintainable:

- No Eureka or Spring Cloud Config  
- No Keycloak (JWT issued by order-service, validated at gateway and services)  
- No Kubernetes or Grafana in this repo  

Discovery uses Docker Compose DNS (`order-service`, `inventory-service`, etc.).

---

## Author & showcase

Built by [Raj](https://github.com/02Raj) as a **public microservices portfolio project**. If this repo helps you or you run it in a talk or post, a star on GitHub or a mention on [LinkedIn](https://www.linkedin.com) / X is appreciated — not required.

**Suggested one-liner for social posts:**

> KitchenPulse: place order → reserve stock (sync) → transactional outbox → Kafka → kitchen tickets. Java 17, Spring Boot 3.5, Angular 19, Postgres + Mongo, no microservices boilerplate bloat.

---

## License

This project is provided as-is for learning and portfolio use. Add an explicit `LICENSE` file if you open-source under MIT or Apache 2.0.

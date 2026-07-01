# Rental Vehicle Management System

A production-style, full-stack rental vehicle management platform for a company operating
**4 branches/locations**, with an AI assistant (LangChain4j + LangGraph4j) for natural-language
vehicle search, customer lookup, and rental policy Q&A (RAG).

- **Backend**: Java 21, Spring Boot 3, Maven, PostgreSQL, Redis, Flyway, JWT auth, Swagger/OpenAPI
- **AI**: LangChain4j (RAG, tool-calling) + LangGraph4j (7-node workflow graph)
- **Frontend**: React 18, Vite, TypeScript, Tailwind CSS, TanStack Query, React Hook Form + Zod
- **Testing**: JUnit 5 + Mockito (backend), Vitest + React Testing Library (frontend)
- **Deployment**: Docker Compose (local), AWS EC2 / Render / Railway / Fly.io guide (cloud)

> **Note on this build**: this project was generated in an environment without a local JDK,
> Maven, Node, or Docker installed, so the Java/TypeScript code could not be compiled or the
> containers built end-to-end here. Everything is written to be correct and runnable per the
> versions pinned in `pom.xml` / `package.json`, but run `mvn -q -DskipTests package` and
> `npm run build` yourself as a first sanity check before deploying. The one area with real
> version-sensitivity risk is `RentalAssistantGraphBuilder` (see the AI section below) since
> `langgraph4j` is a small, fast-moving library — everything else is standard Spring/React.

---

## 1. Project Structure

```
rental-vehicle-management/
├── docker-compose.yml
├── .env.example
├── backend/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/rvms/backend/
│       ├── controller/     # REST controllers (Auth, Branch, Vehicle, Customer, Booking, Payment, Maintenance, Report, AiChat)rvice/        # Business logic (incl. RentalPricingService, BookingService, ReportService...)
│       ├── rep
│       ├── seository/     # Spring Data JPA repositories
│       ├── entity/         # JPA entities + enums
│       ├── dto/            # Request/response records, grouped by module
│       ├── mapper/         # Entity -> DTO mappers
│       ├── cache/          # InMemoryLruCache + CustomerSearchCacheService (Redis + fallback)
│       ├── security/       # JWT filter/service, SecurityUser, UserDetailsService
│       ├── config/         # SecurityConfig, RedisConfig, OpenApiConfig, DataSeeder, *Properties
│       ├── exception/      # GlobalExceptionHandler + domain exceptions
│       └── ai/
│           ├── config/     # AiModelConfig (chat model, embedding model, vector store), MockChatLanguageModel
│           ├── rag/        # PolicyIngestionService, PolicyQaAssistant (LangChain4j AiServices)
│           ├── tools/      # VehicleSearchTool, CustomerLookupTool (@Tool methods -> real services)
│           └── graph/      # RentalAssistantState, IntentType, IntentRouter, nodes/, RentalAssistantGraphBuilder
│   └── src/main/resources/
│       ├── db/migration/   # Flyway: V1__init_schema.sql, V2__seed_data.sql
│       ├── policies/       # Sample rental policy .txt documents ingested for RAG
│       └── application*.yml
│   └── src/test/java/...   # JUnit 5 + Mockito tests
├── frontend/
│   ├── package.json
│   ├── Dockerfile / nginx.conf
│   └── src/
│       ├── api/            # axios client + endpoint functions
│       ├── context/         # AuthContext (JWT storage, role helpers)
│       ├── components/      # Layout, ProtectedRoute, common/ (LoadingSpinner, ErrorAlert)
│       ├── pages/            # Login, Dashboard, Branches, Vehicles, Customers, Bookings, Maintenance, Reports, AiAssistant
│       └── types/            # Shared TS types mirroring backend DTOs
└── README.md
```

### Architecture Diagram (text)

```
┌─────────────────────┐        HTTPS/JSON        ┌───────────────────────────────────────┐
│   React SPA (Vite)  │ ───────────────────────▶ │        Spring Boot REST API           │
│  Tailwind + RTQ      │ ◀─────────────────────── │  Controller -> Service -> Repository   │
└─────────────────────┘        JWT bearer          │                                        │
                                                    │  ┌──────────────────────────────────┐  │
                                                    │  │ AI module (ai/)                  │  │
                                                    │  │  LangGraph4j 7-node workflow:     │  │
                                                    │  │  classifyIntent                   │  │
                                                    │  │   -> extractRentalSearchCriteria  │  │
                                                    │  │   -> searchAvailableVehicles ──┐  │  │
                                                    │  │   -> searchCustomerWithLRU ─────┤  │  │
                                                    │  │   -> answerPolicyQuestion (RAG)─┤  │  │
                                                    │  │   -> recommendVehicle           │  │  │
                                                    │  │   -> finalResponse ◀────────────┘  │  │
                                                    │  └──────────────────────────────────┘  │
                                                    └───────────────┬───────────────────────┘
                                                                    │
                                    ┌───────────────────────────────┼───────────────────────────┐
                                    ▼                               ▼                           ▼
                             ┌─────────────┐               ┌───────────────┐          ┌───────────────────┐
                             │ PostgreSQL  │               │     Redis      │          │ In-memory vector   │
                             │ (Flyway)    │               │ LRU customer   │          │ store (policy RAG) │
                             │             │               │ search cache   │          │ + local embeddings │
                             └─────────────┘               └───────────────┘          └────────────────────┘
```

---

## 2. Core Modules

| Module | Highlights |
|---|---|
| Auth | JWT login/register, roles `ADMIN`, `BRANCH_MANAGER`, `STAFF`, `CUSTOMER` |
| Locations | CRUD for the 4 branches (name, address, phone, manager, hours, active flag) |
| Vehicles | CRUD, branch assignment, status machine `AVAILABLE → RESERVED/RENTED → MAINTENANCE/RETIRED` |
| Customers | CRUD + **LRU-cached** search by name/email/phone/license |
| Bookings | Search availability, create reservation, convert to rental, return + late fee, cancel |
| Payments | Mock gateway: `PENDING → PAID/FAILED`, refund (`PAID → REFUNDED`) |
| Maintenance | Schedule (vehicle → `MAINTENANCE`), complete (vehicle → `AVAILABLE`) |
| Reports | Company-wide + per-branch: fleet counts, active rentals, upcoming returns, revenue |
| AI Assistant | LangChain4j + LangGraph4j: vehicle search, customer lookup, policy RAG, recommendations |

---

## 3. LRU Customer Search Cache

`cache/InMemoryLruCache.java` is a generic, thread-safe LRU built on `LinkedHashMap` in
access-order mode (`removeEldestEntry` evicts the least-recently-used key once the configured
max size is exceeded) — this is the required in-memory fallback.

`cache/CustomerSearchCacheService.java` prefers Redis when available: it keeps a
`ZSET` (`customer:search:lru:index`) as the recency index (score = last-access epoch millis) plus
one string entry per cached term, and evicts the lowest-score members once the set exceeds
`app.cache.lru.max-size`. If Redis throws (down/unreachable), it transparently falls back to the
in-memory cache so search still works.

Configuration (`application.yml` / env vars):

```yaml
app:
  cache:
    lru:
      enabled: true
      max-size: ${LRU_CACHE_SIZE:50}
      ttl-seconds: ${LRU_CACHE_TTL_SECONDS:600}
      use-redis: ${LRU_USE_REDIS:true}
```

Eviction is proven in `backend/src/test/java/com/rvms/backend/cache/InMemoryLruCacheTest.java`
(direct LRU behavior) and `CustomerSearchCacheServiceTest.java` (eviction through the service,
with Redis disabled so the in-memory path is exercised).

---

## 4. AI Assistant (LangChain4j + LangGraph4j)

### Workflow

`ai/graph/RentalAssistantGraphBuilder` builds and compiles a `StateGraph<RentalAssistantState>`
with exactly the 7 requested nodes:

1. **classifyIntent** – deterministic keyword routing into `VEHICLE_SEARCH`, `BOOKING_HELP`,
   `BRANCH_AVAILABILITY`, `CUSTOMER_LOOKUP`, `POLICY_QUESTION`, `GENERAL` (kept rule-based, not
   LLM-based, so routing is exactly reproducible in unit tests — see `IntentRouterTest`,
   `ClassifyIntentNodeTest`).
2. **extractRentalSearchCriteria** – regex/keyword parsing of vehicle type, `location N` → branch
   id, date range (`"July 5" .. "July 10"`), and max daily price from free text.
3. **searchCustomerWithLRU** – calls `CustomerLookupTool`, which is backed by the same
   `CustomerSearchCacheService` LRU cache used by the REST API.
4. **searchAvailableVehicles** – calls `VehicleSearchTool` → `VehicleService.search(...)` →
   real DB query. The model never invents vehicles or prices.
5. **answerPolicyQuestion** – RAG: retrieves relevant chunks from the ingested policy documents
   and answers strictly from that context (see below).
6. **recommendVehicle** – deterministically picks the cheapest real match and formats a
   recommendation string from actual fields only.
7. **finalResponse** – assembles the reply from whatever the above nodes produced.

Routing after `classifyIntent` is a conditional edge (`IntentRouter.route(...)`), extracted into
its own class specifically so it's unit-testable without booting the graph.

### RAG (policy Q&A)

- Sample policy docs live in `backend/src/main/resources/policies/*.txt` (cancellation, late
  return, insurance/damage, age & license requirements, fuel & mileage).
- `PolicyIngestionService` (an `ApplicationRunner`) splits and embeds them at startup into an
  **in-memory vector store** (`InMemoryEmbeddingStore<TextSegment>`), using a local ONNX
  embedding model (`AllMiniLmL6V2EmbeddingModel` — no API key needed for embeddings).
- `PolicyQaAssistant` is a LangChain4j `AiServices` interface wired to a `ContentRetriever` over
  that store; its system prompt explicitly forbids answering outside the retrieved context and
  forbids inventing prices/availability/bookings.

### Preventing hallucination

- Vehicle/price/availability data always comes from `VehicleSearchTool` → `VehicleService` → DB.
- Customer data always comes from `CustomerLookupTool` → `CustomerSearchCacheService` → DB.
- The policy assistant is instructed to say "I don't know" rather than guess when the answer
  isn't in the retrieved policy text.
- No node ever asks the LLM to produce a price, vehicle, or booking confirmation directly —
  those are always formatted in Java from real service responses.

### Running without an API key

By default `AI_MOCK_MODE=true`, so `AiModelConfig` wires a deterministic `MockChatLanguageModel`
instead of a real OpenAI model — the whole app, including RAG retrieval and tool-calling nodes,
runs offline. Set `AI_MOCK_MODE=false` and `OPENAI_API_KEY=<your key>` to use a real model
(`gpt-4o-mini` by default, configurable via `OPENAI_MODEL`).

### Example

```
POST /api/ai/chat
{ "message": "Find me an SUV in location 2 from July 5 to July 10 under $80/day" }

-> classifyIntent: VEHICLE_SEARCH
-> extractRentalSearchCriteria: { vehicleType: SUV, branchId: 2, startDate: 2027-07-05, endDate: 2027-07-10, maxDailyRate: 80 }
-> searchAvailableVehicles: [ real Vehicle rows from Airport Terminal branch, SUV type, <= $80/day ]
-> recommendVehicle: cheapest match
-> finalResponse: "Here's what's available: ... Recommended: Toyota RAV4 (SUV, AUTOMATIC) at Airport Terminal - plate APT-2002, $80.00/day."
```

> **Branch numbering**: `location N` / `branch N` in a chat message is treated as branch id `N`,
> which lines up with this project's seed order (1=Downtown Central, 2=Airport Terminal,
> 3=Uptown, 4=Suburban Mall) on a fresh database.

---

## 5. Setup Instructions

### One-command local run (recommended)

```bash
git clone <this-repo> && cd rental-vehicle-management
cp .env.example .env    # adjust secrets/keys if desired
docker compose up --build
```

- Frontend: http://localhost:5173
- Backend API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui.html
- Postgres: localhost:5432, Redis: localhost:6379

Demo accounts (seeded by `DataSeeder` on first boot, password `Password123!` for all):

| Email | Role |
|---|---|
| admin@rvms.com | ADMIN |
| manager.downtown@rvms.com | BRANCH_MANAGER (Downtown Central) |
| manager.airport@rvms.com | BRANCH_MANAGER (Airport Terminal) |
| manager.uptown@rvms.com | BRANCH_MANAGER (Uptown) |
| manager.suburban@rvms.com | BRANCH_MANAGER (Suburban Mall) |
| staff.downtown@rvms.com | STAFF |
| staff.airport@rvms.com | STAFF |
| customer.demo@rvms.com | CUSTOMER |

### Running backend/frontend separately (dev mode)

Backend needs Postgres + Redis reachable (either via `docker compose up postgres redis`, or your
own local instances):

```bash
cd backend
mvn spring-boot:run
```

Frontend:

```bash
cd frontend
npm install
cp .env.example .env   # VITE_API_BASE_URL=http://localhost:8080/api
npm run dev
```

---

## 6. Sample API Requests

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@rvms.com","password":"Password123!"}'

# Search available vehicles
curl "http://localhost:8080/api/vehicles/search?branchId=2&type=SUV&startDate=2027-07-05&endDate=2027-07-10&maxDailyRate=80" \
  -H "Authorization: Bearer <TOKEN>"

# Create a reservation
curl -X POST http://localhost:8080/api/reservations \
  -H "Authorization: Bearer <TOKEN>" -H "Content-Type: application/json" \
  -d '{"customerId":1,"vehicleId":2,"startDate":"2027-07-05","endDate":"2027-07-10"}'

# Return a vehicle (late fee auto-calculated if applicable)
curl -X POST http://localhost:8080/api/rentals/5/return \
  -H "Authorization: Bearer <TOKEN>" -H "Content-Type: application/json" \
  -d '{"actualReturnDate":"2027-07-12"}'

# Ask the AI assistant
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Authorization: Bearer <TOKEN>" -H "Content-Type: application/json" \
  -d '{"message":"What is the late return fee policy?"}'
```

Full interactive API docs: **http://localhost:8080/swagger-ui.html**

---

## 7. Testing

Backend:

```bash
cd backend
mvn test
```

Covers: `RentalPricingServiceTest` (booking price/late-fee calculation),
`InMemoryLruCacheTest` + `CustomerSearchCacheServiceTest` (LRU eviction proof),
`BookingServiceTest`, `VehicleServiceTest`, `AuthServiceTest`, `PaymentServiceTest`,
`IntentRouterTest` + `ClassifyIntentNodeTest` + `ExtractRentalSearchCriteriaNodeTest`
(AI workflow routing).

Frontend:

```bash
cd frontend
npm test
```

Covers `ErrorAlert`, `LoginPage` (validation, success, server-error paths), and
`AiAssistantPage` (renders suggestions, sends a message, shows intent/tools used).

---

## 8. Deployment Guide (AWS EC2)

1. Provision an EC2 instance (Ubuntu 22.04, t3.medium or larger recommended), open inbound ports
   22, 80/443 (and 8080 if exposing the API directly).
2. Install Docker + Compose plugin:
   ```bash
   sudo apt update && sudo apt install -y docker.io docker-compose-plugin
   sudo usermod -aG docker $USER && newgrp docker
   ```
3. Copy the repo to the instance (`git clone` or `scp`), then:
   ```bash
   cd rental-vehicle-management
   cp .env.example .env
   # edit .env: set a strong JWT_SECRET, real DB_PASSWORD, OPENAI_API_KEY if using a real LLM,
   # and CORS_ALLOWED_ORIGINS to your public frontend URL
   docker compose up -d --build
   ```
4. Put nginx or an ALB in front for TLS termination, proxying 443 → frontend container's port 80
   (which itself proxies `/api/` to the backend container).
5. For persistence across restarts, the Postgres data volume (`rvms_postgres_data`) is already
   defined in `docker-compose.yml`; back it up regularly (`docker exec ... pg_dump`).

### Alternative: Render / Railway / Fly.io

All three support "deploy from Dockerfile" per service:
- Create one service from `backend/Dockerfile` (set env vars from `.env.example`; attach a
  managed Postgres and Redis add-on and point `DB_URL`/`REDIS_HOST` at them).
- Create a second service from `frontend/Dockerfile`, setting build arg
  `VITE_API_BASE_URL` to the backend service's public URL + `/api` (or keep the nginx proxy
  approach and point `proxy_pass` in `nginx.conf` at the backend's public hostname instead of the
  Docker-network name `backend`).
- Railway/Render/Fly.io all auto-provision HTTPS for you, so you can skip the manual nginx/ALB
  TLS step from the EC2 instructions above.

---

## 9. Environment Variables

See `.env.example` (root, for docker-compose) and `frontend/.env.example` (for local `npm run dev`).
Key ones: `DB_*`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`, `LRU_*`, `AI_MOCK_MODE`, `OPENAI_API_KEY`.
No secrets are hardcoded in source — everything defaults to safe local-dev values via
`${VAR:default}` placeholders and is overridable via environment/`.env`.

---

## 10. Known Simplifications (given project scope)

- `CUSTOMER` role is not yet linked 1:1 to a `Customer` record — a real deployment would add that
  FK so customers only see their own bookings.
- `RentalAssistantGraphBuilder` is written against `org.bsc.langgraph4j:langgraph4j-core` 1.2.x;
  since this library's public API has moved between versions, if `mvn compile` reports a
  signature mismatch, that class is the only place in the codebase touching the langgraph4j API.
- Branch deletion is blocked while vehicles are still assigned (encourages deactivating instead).

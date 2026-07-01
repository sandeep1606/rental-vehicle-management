# Rental Vehicle Management System

A full-stack rental vehicle management platform built for a company operating **4 rental branches**. The application manages vehicle rentals, reservations, customers, payments, maintenance, and reporting, while also providing an AI-powered assistant for natural language queries using **LangChain4j** and **LangGraph4j**.

---

## Features

- JWT Authentication & Role-Based Access Control
- Multi-Branch Management (4 Locations)
- Vehicle Inventory & Availability Management
- Customer Management with LRU Search Cache
- Reservations & Rental Processing
- Payment & Refund Management
- Vehicle Maintenance Scheduling
- Fleet & Revenue Reporting
- AI Assistant (Vehicle Search, Customer Lookup & Rental Policy Q&A)

---

# Tech Stack

## Backend

- Java 21
- Spring Boot 3
- Maven
- PostgreSQL
- Redis
- Flyway
- Spring Security
- JWT Authentication
- Swagger / OpenAPI

## Frontend

- React 18
- Vite
- TypeScript
- Tailwind CSS
- TanStack Query
- React Hook Form
- Zod

## AI

- LangChain4j
- LangGraph4j
- Retrieval-Augmented Generation (RAG)
- Tool Calling

## Testing

### Backend

- JUnit 5
- Mockito

### Frontend

- Vitest
- React Testing Library

---

# Architecture

```
React (Vite)
      │
      ▼
Spring Boot REST API
      │
 ┌────┼───────────────┐
 ▼    ▼               ▼
PostgreSQL        Redis
                     │
                     ▼
          LangChain4j + LangGraph4j
                     │
          RAG + Tool Calling + AI Workflow
```

---

# Project Structure

```
rental-vehicle-management/
│
├── backend/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   ├── mapper/
│   ├── security/
│   ├── cache/
│   ├── config/
│   ├── exception/
│   └── ai/
│       ├── config/
│       ├── graph/
│       ├── rag/
│       └── tools/
│
├── frontend/
│   ├── api/
│   ├── components/
│   ├── context/
│   ├── pages/
│   └── types/
│
├── docker-compose.yml
└── README.md
```

---

# AI Assistant

The AI assistant is built using **LangChain4j** and **LangGraph4j**.

### Workflow

1. Classify User Intent
2. Extract Rental Search Criteria
3. Search Customers
4. Search Available Vehicles
5. Answer Rental Policy Questions (RAG)
6. Recommend the Best Vehicle
7. Generate Final Response

### Supported Queries

- Find available vehicles
- Search customers
- Rental policy questions
- Vehicle recommendations
- General rental assistance

---

# LRU Customer Cache

Customer searches are optimized using a two-layer caching strategy.

- **Primary Cache:** Redis
- **Fallback Cache:** Thread-safe in-memory LRU cache

The application automatically falls back to the in-memory cache if Redis is unavailable.

```yaml
app:
  cache:
    lru:
      enabled: true
      max-size: 50
      ttl-seconds: 600
      use-redis: true
```

---

# RAG (Retrieval-Augmented Generation)

Rental policies are stored under:

```
backend/src/main/resources/policies/
```

At startup the application:

- Loads policy documents
- Splits documents into chunks
- Generates embeddings locally
- Stores embeddings in an in-memory vector store
- Retrieves relevant context before generating responses

The assistant only answers policy questions using retrieved documents, reducing hallucinations.

---

# Running the Project

## Using Docker

```bash
git clone <repository-url>
cd rental-vehicle-management

cp .env.example .env

docker compose up --build
```

### Services

| Service | URL |
|----------|-----|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui.html |

---

# Development

## Backend

```bash
cd backend
mvn spring-boot:run
```

## Frontend

```bash
cd frontend

npm install

npm run dev
```

---

# Demo Accounts

Password for all demo users:

```
Password123!
```

| Email | Role |
|--------|------|
| admin@rvms.com | ADMIN |
| manager.downtown@rvms.com | BRANCH_MANAGER |
| manager.airport@rvms.com | BRANCH_MANAGER |
| manager.uptown@rvms.com | BRANCH_MANAGER |
| manager.suburban@rvms.com | BRANCH_MANAGER |
| staff.downtown@rvms.com | STAFF |
| staff.airport@rvms.com | STAFF |
| customer.demo@rvms.com | CUSTOMER |

---

# API Examples

### Login

```http
POST /api/auth/login
```

### Search Vehicles

```http
GET /api/vehicles/search
```

### Create Reservation

```http
POST /api/reservations
```

### Return Vehicle

```http
POST /api/rentals/{id}/return
```

### AI Assistant

```http
POST /api/ai/chat
```

---

# Testing

## Backend

```bash
cd backend

mvn test
```

## Frontend

```bash
cd frontend

npm test
```

Tests cover:

- Authentication
- Booking Workflow
- Vehicle Management
- Payment Processing
- Pricing Service
- Customer LRU Cache
- AI Workflow
- Frontend Components

---

# Deployment

The application can be deployed using:

- Docker Compose
- AWS EC2
- Render
- Railway
- Fly.io

For production deployment:

- Configure a secure `JWT_SECRET`
- Configure PostgreSQL & Redis
- Set `OPENAI_API_KEY` (optional)
- Configure `CORS_ALLOWED_ORIGINS`
- Deploy behind Nginx or a Load Balancer

---

# Environment Variables

Important configuration includes:

- `DB_*`
- `JWT_SECRET`
- `CORS_ALLOWED_ORIGINS`
- `LRU_*`
- `AI_MOCK_MODE`
- `OPENAI_API_KEY`

Configuration is managed through environment variables and `.env` files.

---

# Notes

- Customer accounts are currently not linked directly to customer records.
- `RentalAssistantGraphBuilder` may require updates if using a newer LangGraph4j version.
- Branches cannot be deleted while vehicles remain assigned.

---

## License

This is intended for educational and portfolio purposes.

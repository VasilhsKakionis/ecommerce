# eCommerce Microservices Platform

## Project Description

This project implements a modular **eCommerce platform** based on microservices architecture. The platform consists of independent services for handling orders, events, notifications, and analytics, along with an API Gateway for unified access. Services communicate asynchronously via **Kafka** and store data in **PostgreSQL** or **MongoDB** depending on the use case.

Key features:

* Order creation and management
* Event sourcing and persistence
* Notification handling (email/SMS) with retry logic
* Analytics and reporting
* Unified API access via Spring Cloud Gateway
* Dockerized deployment for local and production environments
* Integration and unit tests using **Testcontainers**

---

## Architecture

* **Order Service:** Handles order CRUD operations, publishes events to Kafka.
* **Event Service:** Consumes `order-events`, persists them in PostgreSQL, publishes `order-processed-events`.
* **Notification Service:** Consumes `order-processed-events` and sends notifications. Includes retry logic for failed notifications.
* **Analytics Service:** Consumes all order events, aggregates metrics in MongoDB.
* **API Gateway:** Routes client requests to respective microservices.

---

## Services & Ports

| Service              | Port | Database   | Kafka Topic                                              |
| -------------------- | ---- | ---------- | -------------------------------------------------------- |
| Order Service        | 8081 | PostgreSQL | order-events                                             |
| Event Service        | 8082 | PostgreSQL | order-events (consume), order-processed-events (produce) |
| Notification Service | 8083 | PostgreSQL | order-processed-events                                   |
| Analytics Service    | 8084 | MongoDB    | order-events                                             |
| Gateway              | 8080 | N/A        | N/A                                                      |

---

## Running the Project

### Prerequisites

* Docker & Docker Compose
* Java 21
* Maven

### Steps

1. Builds Docker images for all services and starts them including Kafka, PostgreSQL, and MongoDB:

```bash
docker-compose up --build  
```

2. Accesses the API Gateway at `http://localhost:8080`

3. Stops the services:

```bash
docker-compose up --build  
```

4. Optionally for cleanup:

```bash
docker builder prune -f 
docker system prune -a --volumes -f 
```

---

## API Endpoints & Postman Examples

### **Order Service**

* **Create Order**

  ```http
  POST /api/v1/orders
  Body: {
    "customerId": 101,
    "items": [{"productId": 1, "quantity": 2}],
    "total": 250.0
  }
  ```
* **Get Order**

  ```http
  GET /api/v1/orders/{orderId}
  ```

> All requests can be made via **Gateway**: e.g., `http://localhost:8080/api/v1/orders`.

---

## Request/Response Flow

### **Order Creation Flow**

1. Client sends POST `/api/v1/orders` → Gateway routes to **Order Service**.
2. Order Service saves order → publishes `OrderCreated` to Kafka.
3. Event Service consumes `OrderCreated`, persists it → publishes `OrderProcessed`.
4. Notification Service consumes `OrderProcessed` → sends notifications via EMAIL/SMS.
5. Analytics Service consumes `OrderCreated` → updates metrics in MongoDB.

### **Event Fetching Flow**

1. Client sends GET `/api/v1/events/order/{id}` → Gateway → Event Service
2. Event Service queries PostgreSQL → returns events list.

### **Notification Flow**

1. Notification Service consumes `OrderProcessed` → creates notification record.
2. Attempts sending (simulated success/failure) → updates status in PostgreSQL.
3. Retry logic invoked if sending fails (up to 3 attempts with exponential backoff).

### **Analytics Flow**

1. Analytics Service consumes order events (`OrderCreated`, `OrderProcessed`, `OrderShipped`).
2. Aggregates metrics: total orders, revenue, processed, shipped.
3. Stores metrics in MongoDB → available via API.

---

## Design Patterns Included

| Pattern                       | Usage                                                                              |
| ----------------------------- | ---------------------------------------------------------------------------------- |
| **Event-Driven Architecture** | Kafka topics handle asynchronous communication between services.                   |
| **Repository Pattern**        | Spring Data JPA/MongoDB repositories abstract database access.                     |
| **Builder Pattern**           | Used in entity/model creation (`OrderEvent.builder()`, `Notification.builder()`).  |
| **Singleton / Spring Beans**  | Services and configuration beans are singletons managed by Spring.                 |
| **Retry / Backoff Pattern**   | Notification Service retry logic with exponential backoff for failed sends.        |
| **API Gateway Pattern**       | Spring Cloud Gateway provides unified entry point, routing, and service discovery. |

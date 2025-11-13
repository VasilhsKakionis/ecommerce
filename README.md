# eCommerce Microservices Platform

This project implements a **distributed microservices architecture** for an eCommerce platform, including:

1. **Order Service** – Manage orders and their lifecycle
2. **Event Service** – Event-driven integration with Kafka
3. **Notification Service** – Async notifications (Email/SMS simulation)
4. **Analytics Service** – Metrics aggregation and reporting

---

## **Tech Stack**

- **Java 21 (Temurin) + Spring Boot 3.3**
- **PostgreSQL** – Order, Event, Notification databases
- **MongoDB** – Analytics metrics database
- **Kafka + Zookeeper** – Event-driven communication
- **Docker & Docker Compose** – Containerized deployment
- **Lombok** – Boilerplate reduction
- **JUnit 5 + Mockito + TestContainers** – Testing

---

## **Microservices Ports**

| Service               | Port  |
|-----------------------|-------|
| Order                 | 8081  |
| Event                 | 8082  |
| Notification          | 8083  |
| Analytics             | 8084  |
| Kafka                 | 9092  |
| Zookeeper             | 2181  |
| PostgreSQL (Order)    | 5432  |
| PostgreSQL (Event)    | 5433  |
| PostgreSQL (Notification) | 5434 |
| MongoDB (Analytics)   | 27017 |

---

## **Getting Started**

1. **Clone the repository**

```bash
git clone <repository-url>
cd ecommerce-microservices
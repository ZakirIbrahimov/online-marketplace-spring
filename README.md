# Online Marketplace System

A full-stack web-based marketplace built with **Java 17** and **Spring Boot 3.2** as part of an Object-Oriented Programming course project at **Baku Higher Oil School**.

The system supports three user roles — **Shopper**, **Merchant**, and **Admin** — each with their own dedicated area. Merchants list products, shoppers browse and purchase them, and admins moderate the entire platform.

---

## Features

**Shopper**
- Register, browse the product catalogue, and search for products
- Add items to cart and proceed to checkout
- View order history and raise disputes

**Merchant**
- Register (requires admin approval before selling)
- Create, edit, and delete product listings with up to 5 images
- View own orders and sales analytics

**Admin**
- Approve or reject merchant applications
- Moderate product listings (approve / reject / suspend)
- Manage orders and resolve disputes
- View platform-wide metrics on the dashboard

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Web | Spring MVC, Thymeleaf |
| Security | Spring Security (BCrypt, role-based access control) |
| ORM | Spring Data JPA (Hibernate) |
| Database | SQLite (development) / PostgreSQL (production) |
| Migrations | Flyway |
| Build | Apache Maven |
| Testing | JUnit 5, MockMvc |

---

## Architecture

The application follows a classic **four-layer architecture**:

```
Presentation Layer   →   Controllers + Thymeleaf Templates
Business Logic Layer →   Service classes + Exception Handlers
Domain Layer         →   JPA Entity classes + Enums + DTOs
Data Access Layer    →   Spring Data JPA Repositories + Flyway SQL Migrations
```

Controllers never contain business logic — they delegate to services. Services never talk directly to the database — they go through repositories. This separation makes the code modular, readable, and testable.

---

## OOP Concepts Demonstrated

This project was built specifically to demonstrate core Object-Oriented Programming principles in a real-world context:

- **Encapsulation** — All entity fields are `private`, accessed only through getter/setter methods (e.g., `User`, `Product`)
- **Inheritance** — `BusinessException` extends `RuntimeException`, inheriting standard exception behaviour while adding domain-specific semantics
- **Polymorphism** — `PaymentService` interface allows swapping payment implementations (currently `MockPaymentService`) without changing any calling code
- **Abstraction** — Service classes hide data access complexity from controllers; the checkout controller calls `paymentService.completeMockPayment()` without knowing the internal logic
- **Interfaces** — `PaymentService` defines the payment contract; `User` implements Spring Security's `UserDetails` interface directly
- **Method Overloading** — `RegistrationService` provides separate `registerShopper()` and `registerMerchant()` methods
- **Method Overriding** — All `@Override` annotations in `User` (implementing `UserDetails`) and `MockPaymentService`
- **Exception Handling** — Two-tier strategy: `BusinessException` thrown by services, caught globally by `GlobalExceptionHandler` (`@ControllerAdvice`)
- **Static Members** — `ProductService.MAX_IMAGES` is a `private static final` class-level constant
- **Collections & Streams** — `Set<Role>`, `List<CartItem>`, `List<OrderLine>`, Stream API used in `CartService`
- **Composition & Aggregation** — `CustomerOrder` owns `OrderLine` and `Payment` (cascade delete); `OrderLine` references `Product` without owning it

---

## Requirements

- Java 17 or higher
- Maven 3.8+ (or use the included `mvnw` wrapper — no installation needed)
- No database setup needed for development — SQLite runs automatically

---

## How to Run (Development)

```bash
# 1. Clone the repository
git clone https://github.com/BlackCimba/online-marketplace-spring.git
cd online-marketplace-spring

# 2. Run with the dev profile (uses SQLite + seeds sample data)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
# On Windows:
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev

# 3. Open your browser
http://localhost:8080
```

On first startup, Flyway creates all database tables automatically and the dev data loader seeds sample users so you can test immediately.

**Sample accounts (dev profile):**

| Role | Email | Password |
|---|---|---|
| Admin | admin@market.com | admin123 |
| Merchant | merchant@market.com | merchant123 |
| Shopper | shopper@market.com | shopper123 |

---

## How to Run (Production with PostgreSQL)

```bash
./mvnw spring-boot:run \
  -Dspring-boot.run.profiles=prod \
  -Dspring.datasource.url=jdbc:postgresql://localhost:5432/marketplace \
  -Dspring.datasource.username=YOUR_DB_USER \
  -Dspring.datasource.password=YOUR_DB_PASSWORD
```

---

## Running Tests

```bash
./mvnw test
```

Key test classes:
- `OrderServiceStockTest` — verifies stock deduction logic during checkout
- `AdminControllerMvcTest` — tests admin endpoints using MockMvc

---

## Project Structure

```
src/
├── main/
│   ├── java/com/marketplace/
│   │   ├── controller/        # Spring MVC controllers (Admin, Catalog, Order, etc.)
│   │   ├── domain/            # JPA entity classes (User, Product, CustomerOrder, Cart...)
│   │   ├── repository/        # Spring Data JPA repository interfaces
│   │   ├── service/           # Business logic (CartService, OrderService, ProductService...)
│   │   └── exception/         # BusinessException + GlobalExceptionHandler
│   └── resources/
│       ├── templates/         # Thymeleaf HTML templates (admin/, merchant/, orders/...)
│       ├── static/css/        # Custom stylesheet
│       └── db/migration/      # Flyway SQL migration scripts (SQLite + PostgreSQL)
└── test/
    └── java/com/marketplace/  # JUnit 5 unit and integration tests
docs/
├── Project-Report.pdf         # Full project report (39 pages)
└── UML-Diagram.pdf            # System UML diagram
```

---

## Documentation

- [Full Project Report](docs/Project-Report.pdf) — 39-page report covering architecture, OOP concepts, workflows, and future improvements
- [UML Diagram](docs/UML-Diagram.pdf) — Class and relationship diagram for the domain model

---

## Authors

- Mahammad Karimov
- Zakir Ibrahimov  
- Eltun Asgarov

**Course:** Object-Oriented Programming  
**Institution:** Baku Higher Oil School, Department of Information Technology  
**Instructor:** Huseynov Shamistan  
**Date:** April 2026

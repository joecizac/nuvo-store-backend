# Architecture & Code Review Report: Nuvo Delivery Platform

## CODE REVIEW REPORT

### 1. Database & Performance Optimization

#### 1.1 The N+1 Query Problem
*   **The Issue:** Current implementations for fetching `OrderHistory` or `Cart` iterate through children in memory. Since items are configured with `FetchType.LAZY`, Hibernate executes a separate SQL `SELECT` query for every single parent record to fetch its associated items. For 20 orders with 5 items each, this results in 21 queries instead of 1.
*   **The Fix:** Implement **`@EntityGraph`** in the repository layer. This allows us to define "fetch plans" that instruct Hibernate to perform an optimized `LEFT JOIN FETCH` in a single query, bringing back the parent and all required children in one database round-trip.

#### 1.2 Transaction Management
*   **The Issue:** Read operations (GET requests) currently lack explicit transaction boundaries. While they work, Hibernate still performs "dirty checking" on every entity fetched, which is a performance overhead for data that is only being viewed.
*   **The Fix:** Apply **`@Transactional(readOnly = true)`** to all retrieval methods in the Service layer. This tells Hibernate to skip dirty checking and flushes, reducing CPU usage and improving response times for search and history endpoints.

#### 1.3 Primary Key Strategy: UUID v7
*   **The Issue:** The system initially used standard **UUIDv4** (Random). Because random IDs are not sequential, inserting them into a PostgreSQL B-Tree index causes "Index Fragmentation." The database has to constantly re-organize the index pages, leading to massive write-performance degradation as the database grows.
*   **The Fix:** Migrate to **UUID v7 (Time-Ordered UUID)**. 
    *   **Why UUID v7?** Unlike version 4, UUID v7 is generated using a timestamp as the prefix. This makes the IDs **sequentially sortable**, ensuring new records are always appended to the end of the index. This provides the performance of an auto-incrementing integer with the global uniqueness and security of a 128-bit UUID.

### 2. Data Integrity & Validation

#### 2.1 Request Validation
*   **The Issue:** Inbound Data Transfer Objects (DTOs) lacked constraints. Malformed or empty data could travel all the way to the service or database layer before causing a crash, resulting in confusing 500 Internal Server Errors for the client.
*   **The Fix:** Implement **`jakarta.validation`** annotations (`@NotBlank`, `@Min`, `@Email`) on all DTO fields and enforce them in the Controllers using the **`@Valid`** annotation. This catches bad data at the entry point and returns a clean 400 Bad Request.

#### 2.2 Exception Handling Logic
*   **The Issue:** The application was throwing generic `java.lang.Exception`. This makes it impossible for the `GlobalExceptionHandler` to distinguish between a "User Error" (like a wrong ID) and a "System Error" (like a DB crash).
*   **The Fix:** Develop a **Custom Exception Hierarchy** (e.g., `ResourceNotFoundException`, `BusinessLogicException`). These carry specific application-level error codes that are mapped to the standardized `ApiResponse` payload.

### 3. Security & Scalability

#### 3.1 Asynchronous Event-Driven Architecture
*   **The Issue:** Business-critical operations like "Checkout" were blocked by secondary tasks like "Sending Push Notifications." If the notification service is slow, the user's checkout hangs.
*   **The Fix:** Implement an **Event-Driven Pattern** using Spring's `ApplicationEventPublisher`. The `OrderService` will simply "publish" an event that an order was created. A separate, asynchronous listener (`@Async`) will handle the notification task on a background thread, allowing the checkout to finish instantly.

#### 3.2 Actuator & Infrastructure Security
*   **The Issue:** Spring Boot Actuator endpoints (like `/env` or `/metrics`) expose sensitive details about your server and environment. These were previously public.
*   **The Fix:** Configure **Security Role-Based Access Control (RBAC)**. Only the `/health` and `/info` endpoints should be public; all other administrative endpoints must be restricted to an `ADMIN` role.

### 4. Database Strategy: PostGIS
*   **Standardization:** `GEOMETRY`, `POINT`, and `GIST` are PostGIS extensions, not standard SQL.
*   **Decision:** The platform will **strictly use PostGIS**. While this limits database agnosticism, the performance benefits of `GIST` indexing and the accuracy of `geography` type calculations for radius-based discovery are considered mission-critical. Standardizing to `Double` columns with Haversine math is rejected due to performance and maintenance overhead.

---

## ENHANCEMENT TASKS

### 🔴 High Priority (Stability & Write Performance)
- [x] **Implement UUID v7:** Refactor entity IDs to use native Hibernate UUID v7 generation to ensure sortable Primary Keys.
- [x] **PostGIS Strategy:** Formally adopted PostGIS for high-performance spatial operations.
- [x] **Resolve N+1 Queries:** Updated Repositories with `@EntityGraph` to fetch Order/Cart items in a single join.
- [x] **DTO Validation:** Added `@Valid` and `jakarta.validation` constraints to all request DTOs.
- [x] **Async Notifications:** Refactored `NotificationService` to be event-driven and non-blocking using `@Async`.

### 🟡 Medium Priority (Security & Integrity)
- [x] **Custom Exception Hierarchy:** Implemented specific exceptions and mapped them in `GlobalExceptionHandler`.
- [x] **JPA Auditing:** Enabled `@EnableJpaAuditing` for automated `createdAt` and `updatedAt` timestamps.
- [x] **Security Lockdown:** Restricted CORS and secured non-public Actuator endpoints.
- [x] **Read-Only Transactions:** Applied `@Transactional(readOnly = true)` to all retrieval methods.

### 🟢 Low Priority (Scalability)
- [ ] **Spring Cache:** Add `@Cacheable` to store catalog and category retrieval methods to reduce DB load.
- [ ] **API Documentation:** Update Swagger annotations to include detailed request/response examples.

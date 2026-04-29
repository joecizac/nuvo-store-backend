# Project State & Rationale: Nuvo Delivery Platform

## 1. Current Architectural State
The system is a production-grade, industry-agnostic delivery backend built with **Kotlin** and **Spring Boot 4.x**. It utilizes a **Vertical Slice Architecture**, ensuring that each feature (User, Store, Catalog, Order, Social, Admin) is highly cohesive and decoupled.

### Core Stack:
*   **Database:** PostgreSQL 15+ with **PostGIS** for high-performance spatial discovery.
*   **Security:** Stateless **Firebase JWT** authentication with a "Mock Mode" for local development.
*   **Persistence:** Hibernate 7 with **UUID v7** (Time-Sorted) primary keys.
*   **Communication:** RESTful API with a **Global Response Wrapper** (`code`, `message`, `data`) and unified error handling.
*   **Performance:** Spring Cache (In-memory) for read-heavy catalogs and **N+1 query resolution** via `@EntityGraph`.

## 2. Key Decisions & Rationale
*   **PostGIS over standard SQL:** We chose PostGIS for mission-critical spatial accuracy. Using `GIST` indexes and `geography` types allows the system to perform radius searches across millions of stores in milliseconds, which is impossible with standard math-based SQL.
*   **UUID v7 over UUID v4:** We refactored all primary keys to UUID v7. Unlike random v4 IDs, v7 is sequentially sortable. This prevents **B-Tree index fragmentation** in PostgreSQL, maintaining high-speed write performance as the database scales.
*   **Vertical Slice over Layered Architecture:** Each feature contains its own domain, repository, and controller. This allows the platform to evolve industry-specific features (e.g., Pharma vs. Food) without creating a "Big Ball of Mud" in a shared service layer.
*   **Event-Driven Notifications:** Push notifications are decoupled via `ApplicationEventPublisher` and `@Async` listeners. This ensures the **Checkout** transaction is never blocked by external network latency from Firebase Messaging.

## 3. Discovery & Constraints
*   **Spatial Casting:** PostgreSQL `geometry(Point, 4326)` calculates distance in **degrees**. We uncovered that all spatial queries must use explicit `CAST(location AS geography)` to perform searches in **meters**.
*   **Hibernate 7 JSON Mapping:** We discovered that standard String-to-JSONB mapping fails in Hibernate 7. We implemented `@JdbcTypeCode(SqlTypes.JSON)` on the `deliveryAddressSnapshot` field to ensure correct PostgreSQL persistence.
*   **Entity Lifecycle:** Primary keys are defined as `UUID? = null` with `@UuidGenerator`. This is required for Hibernate 7 to correctly detect "new" entities and avoid unnecessary "merge" operations before "persist."

## 4. The 'Why' of Change (Significant Pivots)
*   **Catalog Scope:** We pivoted from "Global Categories" to **"Store-wise Hierarchies."** This change was essential to support true industry-agnosticism, allowing a Grocery store and a fashion boutique to exist on the same platform with completely different category structures.
*   **Mock Security:** To facilitate rapid local learning and testing, we implemented a `mock-mode` property. This allows developers to bypass the Firebase Admin SDK requirement while still populating a `SecurityContext` with a valid mock user.

## 5. Recent Progress (2026-04-24)
*   **Source Control:** Repository initialized with optimized `.gitignore`. Remote configured (SSH) and code pushed to GitHub `main` branch.
*   **Bug Fix:** Resolved `Internal Server Error` during checkout caused by JSONB type mismatch in the `Order` entity.
*   **Validation:** Conducted manual API testing suite. Verified User, Discovery, Catalog, Cart (Single Store Rule), and Order flows are functional.

## 6. Pending Roadmap
1.  **Address Management:** Expand API to support Edit, Delete, and "Default" address toggling.
2.  **Advanced Discovery:** Implement global cross-entity search and complex sorting/filtering logic.
3.  **Engagement & Tracking:** Add Order Reviews and Real-Time courier location tracking.
4.  **Unit Testing (High Priority):** Implement JUnit/Mockito tests for `CartService` (Single-store rule enforcement) and `OrderService` (Total calculation logic).
5.  **Image Upload Service:** Create a unified `FileStorageService` to handle binary uploads for store logos and product images.
6.  **Integration Testing:** Set up **Testcontainers** with a PostGIS image to verify spatial repository logic in a real database environment.
7.  **Admin RBAC:** Implement Role-Based Access Control to distinguish between `ROLE_USER` and `ROLE_ADMIN` using Firebase custom claims.

## 7. Context Injection (The TL;DR)
This project is an industry-agnostic delivery platform (Kotlin/Spring Boot 4) designed for extreme scalability and performance. It features a PostGIS-powered discovery engine and a store-specific catalog hierarchy that can support anything from food to pharmacy. The backend is currently in a "stable-ready" state with a comprehensive data seeder, automated auditing, and a standardized API response structure. 

Architecturally, we have prioritized database integrity by using native UUID v7 sortable identifiers and resolved all N+1 query overhead using JPA EntityGraphs. The system is Docker-ready and supports "Mock Mode" for development without real Firebase credentials. The next immediate focus is securing the core business logic with unit tests and implementing a binary image upload service.

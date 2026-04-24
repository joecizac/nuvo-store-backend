# Implementation Plan: Delivery Platform Backend (HLD)

## 1. Objective
Develop a scalable, secure, and robust backend server in Kotlin and Spring Boot 4.x using a **Vertical Slice Architecture**, optimized for high-performance spatial discovery and transactional integrity.

## 2. System Architecture
*   **Framework:** Spring Boot 4.x (Kotlin 2.x)
*   **Database:** PostgreSQL 15+ with **PostGIS** extension.
*   **Persistence:** Hibernate 7 with native **UUID v7** (Time-Ordered) support.
*   **Migrations:** Flyway.
*   **Caching:** Spring Cache (In-memory/Caffeine) for catalog read optimization.
*   **Auth:** Firebase Admin SDK with a custom stateless JWT filter and local **Mock Mode** support.
*   **API Standard:** Unified JSON responses via `ResponseBodyAdvice`.

## 3. Detailed Domain Models
All identifiers are Hibernate-managed **UUID v7** (Time-Sorted).

### 3.1 Identity & Profile
*   **User:** `id`, `firebaseUid` (unique), `email`, `name`, `phoneNumber`, `profileImageUrl`, `fcmToken`, `createdAt`, `updatedAt`.
*   **UserAddress:** `id`, `user_id`, `title`, `fullAddress`, `location` (Point/4326), `isDefault`, `createdAt`, `updatedAt`.

### 3.2 Discovery (Chain & Store)
*   **Chain:** `id`, `name`, `description`, `logoUrl`, `bannerUrl`, `createdAt`, `updatedAt`.
*   **Store:** `id`, `chain_id` (nullable), `name`, `description`, `contactNumber`, `logoUrl`, `bannerUrl`, `location` (Point/4326), `address`, `isActive`, `averageRating`, `createdAt`, `updatedAt`.

### 3.3 Catalog (Store-Specific)
*   **Category:** `id`, `store_id`, `name`, `imageUrl`, `createdAt`, `updatedAt`.
*   **SubCategory:** `id`, `category_id`, `name`, `createdAt`, `updatedAt`.
*   **Product:** `id`, `store_id`, `sub_category_id`, `name`, `description`, `imageUrl`, `isAvailable`, `createdAt`, `updatedAt`.
*   **SKU:** `id`, `product_id`, `name`, `imageUrl`, `originalPrice`, `discountedPrice`, `isAvailable`, `createdAt`, `updatedAt`.

### 3.4 Cart & Orders
*   **Cart:** `id`, `user_id`, `store_id`, `createdAt`, `updatedAt`. (One cart per user).
*   **CartItem:** `id`, `cart_id`, `sku_id`, `quantity`, `createdAt`, `updatedAt`.
*   **Order:** `id`, `user_id`, `store_id`, `status` (Enum), `totalAmount`, `deliveryAddressSnapshot` (JSONB), `createdAt`, `updatedAt`.
*   **OrderItem:** `id`, `order_id`, `sku_id`, `snapshotPrice`, `quantity`, `createdAt`.

### 3.5 Social
*   **Review:** `id`, `user_id`, `store_id`, `rating` (1-5), `comment`, `createdAt`, `updatedAt`.
*   **FavouriteStore:** `user_id`, `store_id` (Composite PK).
*   **FavouriteProduct:** `user_id`, `product_id` (Composite PK).

## 4. Detailed Implementation Phases

### Phase 1: Infrastructure & Security
1.  **Bootstrap:** Initialized Spring Boot 4, PostgreSQL, and PostGIS.
2.  **Mock Security:** Implemented a custom JWT filter with a `mock-mode` to allow development without real Firebase credentials.
3.  **Standardized Response:** Created `ApiResponse` envelope and `GlobalResponseWrapper` to ensure all endpoints return consistent JSON.

### Phase 2: Core Vertical Slices
1.  **User & Address:** Implemented profile syncing and spatial address management.
2.  **Spatial Discovery:** Developed native PostGIS queries using `ST_DWithin` and `geography` casting for meter-based accuracy.
3.  **Store-Specific Catalog:** Built the flexible `Store -> Category -> SKU` hierarchy to support industry-agnosticism.

### Phase 3: Transactions & Social
1.  **Smart Cart:** Implemented the "Single Store" constraint and SKU-based quantity management.
2.  **Atomic Checkout:** Developed the conversion of Cart to Order with historical price and address snapshotting.
3.  **Social Layer:** Implemented Reviews with automated average rating denormalization for stores.
4.  **Event-Driven Notifications:** Integrated FCM with an asynchronous `@EventListener` pattern.

### Phase 4: Performance & Tooling
1.  **Optimization:** Migrated all IDs to UUID v7, enabled JPA Auditing, and resolved N+1 queries via `@EntityGraph`.
2.  **Scalability:** Implemented Spring Cache for read-heavy discovery and catalog retrieval.
3.  **Documentation:** Integrated Swagger UI (OpenAPI 3.1) and V2 Postman tooling.

## 5. Local Development Setup
*   **Prerequisites:** Docker Desktop and Java 17+.
*   **DB Boot:** Run `docker-compose up -d` (Includes Postgres, PostGIS, and Adminer on port 8081).
*   **Mock Mode:** Enabled by default in `application.yml` (`nuvo.mock-mode=true`). Use any Bearer token in Postman.
*   **Seeding:** The `DataSeeder` automatically populates 5 chains, 57 stores, and diverse catalogs on the first run.
*   **Run:** Execute `./gradlew bootRun`.

## 6. Production & Deployment Readiness
*   **CORS:** Parameterized via `nuvo.cors.allowed-origins` env var; must be restricted in prod.
*   **Security:** Administrative Actuator endpoints are locked behind an `ADMIN` role.
*   **Secrets:** All sensitive data (DB passwords, Firebase Keys) is externalized via environment variables.
*   **Performance:** Hibernate `show-sql` and JSON "non-null" inclusions are optimized for minimal I/O and payload size.

# Kanban Board: Industry-Agnostic Delivery Platform

## 🔴 TO-DO
- [ ] **Unit Tests:** Implement JUnit tests for `CartService` (Single Store Rule) and `OrderService` (Total Calculation).
- [ ] **Image Upload Service:** Integrate with a cloud storage provider for handling logo and product image uploads.
- [ ] **Logging & Monitoring:** Configure Micrometer and Prometheus for system health tracking.
- [ ] **Integration Tests:** Set up Testcontainers with PostGIS to verify spatial repositories.

## 🟡 ENHANCEMENT TASKS
### 🔴 High Priority
- [x] **Implement UUIDv7:** Refactored all entity IDs to use native Hibernate UUID v7 (Time-Sorted) for optimal indexing.
- [x] **Resolve N+1 Queries:** Update Repositories with `@EntityGraph` or `JOIN FETCH`.
- [x] **DTO Validation:** Add `@Valid` and `jakarta.validation` constraints to all request DTOs.
- [x] **Async Notifications:** Refactored `NotificationService` to be event-driven and non-blocking using `@EventListener` and `@Async`.

### 🟡 Medium Priority
- [x] **Custom Exception Hierarchy:** Implement specific exceptions and map them in `GlobalExceptionHandler`.
- [x] **JPA Auditing:** Enabled `@EnableJpaAuditing` and automated timestamps across all entities.
- [x] **Security Lockdown:** Restricted CORS and secured non-public Actuator endpoints.
- [x] **Read-Only Transactions:** Added `@Transactional(readOnly = true)` to all GET service methods.
- [x] **API Documentation (Enhanced):** Updated Swagger annotations with detailed request/response examples and schemas.
- [x] **Spring Cache:** Implemented `@Cacheable` for read-heavy store and catalog retrieval methods.

## 🟡 IN-PROGRESS
- [ ] *Select a new task to begin...*

## 🟢 DONE
### Phase 11: Testing & Data Integrity
- [x] **Comprehensive Data Seeding:** Updated `DataSeeder` to include Mock Users, Addresses, and initial Reviews.
- [x] **Database Refresh:** Performed a clean volume wipe and restart to verify full end-to-end data integrity.
- [x] **Runtime Stability:** Resolved type mismatches and runtime dependencies (Jackson/Cache) for a stable build.

### Phase 10: Performance & Scalability
- [x] **PostGIS Strategy:** Formally adopted PostGIS for high-performance spatial operations and accuracy.
- [x] **Spring Cache:** Implemented local in-memory caching for catalog and discovery data.

### Phase 9: Administration
- [x] **Admin API:** Implemented management endpoints for Chains, Stores, Categories, Products, and SKUs with integrated Cache Eviction logic.

### Phase 8: Tooling & Documentation
- [x] **API Documentation:** Integrated SpringDoc OpenAPI/Swagger UI for real-time endpoint testing.
- [x] **Postman Collection (V2):** Upgraded collection with Base Response Structure, Admin API, and fresh sample IDs.
- [x] **Database Browser:** Integrated Adminer into Docker Compose for browser-based DB management.
- [x] **Base Response Structure:** Implemented global response wrapping (`ResponseBodyAdvice`) and standardized error handling.

### Phase 7: Bug Fixes & Stability
- [x] **Hibernate Spatial Dialect Fix:** Resolved `PostgisPGDialect` removal for Hibernate 6 auto-detection.
- [x] **Spatial Query Fix:** Switched to Native Query with geography casting for accurate meter-based discovery.
- [x] **Local Environment Validation:** Verified full system flow from database boot to API response.
- [x] **Jackson Config:** Explicitly registered `ObjectMapper` bean to resolve dependency issues.

### Phase 6: Testing & Data
- [x] **Data Seeding Script:** `DataSeeder` implemented to populate diverse Catalogs (Food, Fashion, Pharma, etc.).

### Phase 5: Notifications & Social Slices
- [x] **Social Migration (V5):** Created `reviews`, `favourite_stores`, and `favourite_products` tables.
- [x] **Review Logic:** Average rating denormalization implemented.
- [x] **Favourites:** Toggle logic for stores and products.
- [x] **Push Notifications:** `NotificationService` wrapper for FCM implemented.

### Phase 4: Cart & Transactional Order Slice
- [x] **Order Migration (V4):** Created `carts`, `cart_items`, `orders`, and `order_items` tables.
- [x] **Cart Logic:** "Single Store" rule enforcement and item CRUD.
- [x] **Checkout Engine:** Atomic transition from Cart to Order with price locking and address snapshotting.

### Phase 3: Store-Specific Catalog Slice
- [x] **Catalog Migration (V3):** Created `categories`, `sub_categories`, `products`, and `skus` tables.
- [x] **Flexible Hierarchy:** Domain models implemented allowing stores to define unique categories.

### Phase 2: User & Discovery Slices
- [x] **User Profile Sync:** `/api/v1/users/me` for Firebase UID mapping.
- [x] **Address Management:** CRUD for `UserAddress` with PostGIS support.
- [x] **Discovery API:** Spatial search and chain browsing endpoints.

### Phase 1: Infrastructure & Core Security
- [x] **Project Bootstrapping:** Spring Boot 4.x initialized with Kotlin/Gradle.
- [x] **Database Setup:** Docker Compose with PostgreSQL + PostGIS.
- [x] **Security Layer:** Stateless Firebase JWT validation.

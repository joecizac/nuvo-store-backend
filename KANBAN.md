# Kanban Board: Nuvo Delivery Platform

## To Do
- [ ] **PostGIS Integration Tests:** Add Testcontainers with PostGIS to verify migrations, spatial queries, geography casting, and repository contracts against a real database.
- [ ] **DB-Level Catalog Integrity:** Add a trigger, constraint strategy, or schema redesign so product store ownership is enforced by the database, not only by service logic.
- [ ] **Admin RBAC Hardening:** Complete role-based admin authorization using Firebase custom claims and audit all admin endpoints.
- [ ] **Image Upload Service:** Add a `FileStorageService` backed by S3, Cloudinary, or equivalent for store/category/product/SKU images.
- [ ] **Observability:** Configure Micrometer/Prometheus and production logging/trace correlation.
- [ ] **Inventory Model:** Add stock quantity/reservation semantics for SKUs before production commerce rollout.
- [ ] **Admin Draft Catalog APIs:** Add explicit admin list/detail APIs for `DRAFT` and `ARCHIVED` products if the admin client needs them.
- [ ] **Strict UUIDv7 Review:** Confirm whether Hibernate `UuidGenerator.Style.TIME` satisfies the desired UUID version semantics or replace with an explicit UUIDv7 generator.

## In Progress
- [ ] **Select Next Task:** No active engineering task is currently assigned.

## Done

### Phase 14: Product Lifecycle and Price Summary
- [x] Added `ProductStatus` with `DRAFT`, `ACTIVE`, and `ARCHIVED`.
- [x] Added Flyway migration `V10__Product_Status.sql`.
- [x] Changed admin product creation to create draft products.
- [x] Kept SKU creation as a separate admin operation.
- [x] Added product activation endpoint with hierarchy, SKU existence, availability, and price validation.
- [x] Restricted public product APIs to active, available products with available SKUs.
- [x] Removed `priceCents` from product responses.
- [x] Added SKU-derived `priceSummary` with `minPrice`, `maxPrice`, `displayPrice`, `hasPriceRange`, and `currency`.
- [x] Added tests for activation failure/success and price-summary behavior.
- [x] Refreshed PRD, PLAN, KANBAN, PROJECT_STATE, and REVIEW_REPORT to match current implementation and rationale.

### Phase 13: Client Contract and Domain Integrity Fixes
- [x] Aligned API response envelope to `success`, `data`, `message`, `errorCode`.
- [x] Returned list payloads for client-facing store/product list endpoints.
- [x] Added clean 400 responses for invalid UUID path parameters.
- [x] Removed random UUID fallback values from DTO mappers.
- [x] Fixed product/store/sub-category ownership validation in admin product creation.
- [x] Added explicit `isAvailable` and `isFavourite` JSON names where Kotlin/Jackson would otherwise emit `available`/`favourite`.

### Phase 12: Enhanced Slices
- [x] Address management with edit, delete, set-default, and ownership validation.
- [x] Advanced discovery with search and filters for cuisine, price range, and openness.
- [x] Order-linked reviews while preserving one-review-per-store behavior.
- [x] Paginated store feedback.
- [x] Live tracking coordinate retrieval for active orders.
- [x] Event-driven async notifications.
- [x] Request DTO validation and custom exception hierarchy.

### Phase 11: Testing and Data Integrity
- [x] Added broad unit coverage for common, controller, user, store, catalog, order, social, and admin behavior.
- [x] Verified `gradle test` passes after product lifecycle changes.
- [x] Comprehensive data seeding for mock users, addresses, chains, stores, catalogs, and reviews.
- [x] Historical order FK nullability migration.

### Phase 10: Performance and Scalability
- [x] Adopted PostGIS and GIST indexes for location discovery.
- [x] Used geography casting for meter-accurate radius search.
- [x] Added Spring Cache for read-heavy discovery/catalog paths.
- [x] Added `@EntityGraph` to reduce N+1 risk on important aggregate reads.
- [x] Added read-only transactions to retrieval methods.

### Phase 9: Administration
- [x] Admin APIs for chains, stores, categories, sub-categories, products, SKUs, and product activation.
- [x] Cache eviction on catalog mutations.

### Phase 8: Tooling and Documentation
- [x] Swagger/OpenAPI integration.
- [x] Adminer in Docker Compose.
- [x] Standard response wrapper and global exception handling.

### Phase 7: Bug Fixes and Stability
- [x] Hibernate/PostGIS dialect compatibility.
- [x] Native spatial query accuracy.
- [x] Jackson/ObjectMapper and cache dependency fixes.
- [x] Mock mode default changed to safe `false`.

### Phase 6: Testing and Data
- [x] DataSeeder for diverse industries and catalog structures.

### Phase 5: Notifications and Social
- [x] Reviews and favourites migrations.
- [x] Average store rating denormalization.
- [x] FCM notification service wrapper.

### Phase 4: Cart and Orders
- [x] Cart and order migrations.
- [x] Single-store cart rule.
- [x] Checkout with price locking and delivery address snapshot.

### Phase 3: Store-Specific Catalog
- [x] Store-owned categories.
- [x] Sub-categories under categories.
- [x] Products under sub-categories.
- [x] SKUs under products.

### Phase 2: User and Discovery
- [x] User profile sync.
- [x] Address CRUD with PostGIS points.
- [x] Nearby store discovery and chain browsing.

### Phase 1: Infrastructure and Security
- [x] Kotlin/Spring Boot backend bootstrap.
- [x] Docker Compose PostgreSQL/PostGIS setup.
- [x] Firebase JWT security filter.

# Product Requirements Document (PRD): Nuvo Delivery Platform

## 1. Purpose
Nuvo is an industry-agnostic delivery backend for mobile commerce experiences across food, grocery, pharmacy, cosmetics, fashion, electronics, and similar store-based retail domains.

This document is the high-level product and architecture overview. Detailed implementation decisions live in `PLAN.md`; execution progress lives in `KANBAN.md`; rationale, review notes, and historical pivots live in `PROJECT_STATE.md`.

## 2. Product Goals
- Let customers discover nearby stores using location-aware search.
- Support independent stores and store chains with multiple store locations.
- Support store-specific catalogs instead of forcing one global taxonomy.
- Model sellable products correctly through SKUs, including product drafts and activation.
- Keep checkout historically accurate through order, price, and address snapshots.
- Provide a consistent API contract for mobile clients.
- Keep the backend secure, scalable, and production-oriented while retaining local mock-mode support for development.

## 3. Personas
- **Customer:** Browses stores, views products/SKUs, manages addresses, adds SKUs to cart, checks out, tracks active orders, reviews stores, and favourites stores/products.
- **Platform Admin:** Creates chains, stores, categories, sub-categories, draft products, SKUs, and activates products after catalog validation.
- **Store Owner / Manager (Future Scope):** Manages store profile, catalog, inventory, and order fulfilment with role-based access controls.

## 4. Core Product Requirements

### 4.1 Authentication and User Profile
- Mobile clients authenticate with Firebase Auth.
- Backend validates Firebase JWTs in production.
- Local development can use mock mode through `nuvo.mock-mode`.
- Backend stores synchronized user profile fields: Firebase UID, email, name, phone number, profile image URL, FCM token.
- Users can maintain multiple delivery addresses with geolocation and one default address.

### 4.2 Store Discovery
- Users can discover active stores near a latitude/longitude within a radius.
- Store discovery uses PostgreSQL/PostGIS for accurate spatial queries.
- Stores may be independent or linked to a chain.
- Chains represent shared brand identity, while each store location owns its own catalog and inventory.
- Users can search and filter stores by name/query, cuisine/industry, price range, and open-now state.

### 4.3 Catalog
- Catalog hierarchy is store-specific:
  `Store -> Category -> SubCategory -> Product -> SKU`
- Categories belong to a store.
- Sub-categories belong to categories.
- Products belong to sub-categories and are tied to a store for query efficiency and ownership validation.
- SKUs are the sellable inventory units and own price/availability.
- Apparel/shoe-style variants are represented as SKUs or future structured SKU attributes; a SKU should identify the lowest sellable variant such as color + size.

### 4.4 Product Lifecycle
- Products support `DRAFT`, `ACTIVE`, and `ARCHIVED` states.
- Admins may create a product as `DRAFT` before adding SKUs.
- Public customer-facing product APIs return only valid `ACTIVE` products with at least one available SKU.
- Product activation validates:
  - product/store/sub-category ownership consistency
  - at least one SKU exists
  - at least one SKU is available
  - SKU prices are valid
- Draft and archived products are not customer-visible.

### 4.5 Product Pricing
- Product-level price is not stored directly.
- Public product pricing is derived from available SKUs.
- Product responses expose `priceSummary`:
  - `minPrice`
  - `maxPrice`
  - `displayPrice`
  - `hasPriceRange`
  - `currency`
- Prices are integer minor units in API responses, but field names intentionally do not use the `Cents` suffix.
- Product cards show `displayPrice`; if `hasPriceRange` is true, clients should render it as a "From" price.

### 4.6 Cart
- Each user has a persistent database-backed cart.
- Cart items point to SKUs, not products.
- A cart can contain SKUs from only one store at a time.
- Adding a SKU from another store clears the previous store cart contents.

### 4.7 Checkout and Orders
- Checkout converts a cart to an order transactionally.
- Orders snapshot delivery address and SKU prices for historical correctness.
- Supported order statuses: `PENDING`, `PREPARING`, `DISPATCHED`, `DELIVERED`, `CANCELLED`.
- Order foreign keys are nullable where needed to preserve historical records after related entities are deleted.

### 4.8 Reviews, Ratings, and Favourites
- Users can review stores.
- Store average rating is denormalized for discovery/display.
- Order-linked reviews update the user's store review rather than violating one-review-per-store constraints.
- Users can favourite stores and products.

### 4.9 Notifications and Tracking
- Order status changes publish application events.
- Firebase Cloud Messaging is invoked asynchronously so checkout is not blocked by notification latency.
- Active orders can expose courier latitude/longitude for live tracking.

## 5. API Contract
- REST APIs return a unified envelope:
  - `success`
  - `data`
  - `message`
  - `errorCode`
- List endpoints used by the mobile app return arrays inside `data`, not Spring `Page` objects.
- Invalid UUID path parameters return a clean 400 error envelope.
- Public product responses do not include `priceCents`; use `priceSummary`.

## 6. Technical Constraints
- Kotlin 2.x, Spring Boot 4.x, Java 17.
- PostgreSQL 15+ with PostGIS.
- Flyway for database migrations.
- Hibernate ORM 7 with time-ordered UUID generation.
- Spring Security with Firebase JWT verification and local mock mode.
- Swagger/OpenAPI for API exploration.
- Docker Compose provides PostgreSQL/PostGIS and Adminer for local development.

## 7. Non-Goals / Future Scope
- Inventory quantities are not yet modelled.
- Store-owner self-service UI/RBAC is not complete.
- Image upload/storage is not implemented; image fields currently store URLs.
- Testcontainers/PostGIS integration tests are pending.
- Observability with Micrometer/Prometheus is pending.

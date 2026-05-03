# Project State, Rationale, and Review Notes: Nuvo Delivery Platform

This document is the canonical project state and rationale log. It absorbs the long-form review context that used to live separately in `REVIEW_REPORT.md`; that file now points here and keeps only a concise review summary.

## 1. Current State
Nuvo is a Kotlin/Spring Boot 4 backend for an industry-agnostic delivery platform. It uses vertical slices for User, Store, Catalog, Order, Social, and Admin functionality.

### Current Stack
- Kotlin 2.2.x, Java 17, Spring Boot 4.0.x.
- PostgreSQL 15+ with PostGIS.
- Hibernate ORM 7 and Flyway.
- Spring Security with Firebase JWT verification.
- Local mock mode via `nuvo.mock-mode`; default is `false`.
- Spring Cache for read-heavy catalog/discovery paths.
- Swagger/OpenAPI and Adminer for local development.

### API Contract
All ordinary controller responses are wrapped as:
- `success`
- `data`
- `message`
- `errorCode`

Client-facing list endpoints return arrays in `data`. Invalid UUID path parameters return 400 envelopes instead of generic 500s.

## 2. Core Domain Decisions

### Store and Chain Model
Stores may be independent or part of a chain.
- `stores.chain_id` is nullable.
- Chain deletion sets store `chain_id` to null.
- Chains hold shared brand identity.
- Stores hold location, active state, catalog ownership, and operational data.

### Store-Specific Catalog
The catalog is intentionally store-specific:
`Store -> Category -> SubCategory -> Product -> SKU`

This supports different retail verticals without forcing one global taxonomy. A pharmacy, restaurant, grocery store, and fashion boutique can each define categories that match their business.

### Product Lifecycle
Products now use a production-oriented lifecycle:
- `DRAFT`: editable product not visible to customers.
- `ACTIVE`: customer-visible product.
- `ARCHIVED`: retired product.

New products are drafts. SKUs are added separately. Activation validates hierarchy, SKU existence, SKU availability, and SKU pricing.

Public product APIs return only `ACTIVE`, available products with at least one available SKU. This prevents incomplete catalog data from leaking to customers.

### SKU and Pricing Model
SKU is the sellable unit. Product price is not stored directly.

Product list/detail responses expose `priceSummary`, derived from available SKUs:
- `minPrice`
- `maxPrice`
- `displayPrice`
- `hasPriceRange`
- `currency`

API price fields are integer minor units but intentionally omit the `Cents` suffix. `priceCents` has been removed.

For apparel/shoes, the expected SKU meaning is the lowest sellable variant, for example Nike Air Max + colorway + size.

## 3. Technical Rationale

### PostGIS over Standard SQL
PostGIS is required for accurate and scalable radius search. `geometry(Point, 4326)` alone measures in degrees, so spatial discovery uses geography casting for meter-based calculations. GIST indexing is preferred over application-level Haversine filtering because filtering millions of stores in application code would not scale.

### Time-Ordered UUIDs
Entities use Hibernate-managed time-ordered UUIDs through `@UuidGenerator(style = UuidGenerator.Style.TIME)`. The intent is B-tree locality and avoiding random UUIDv4 index fragmentation.

Important nuance: the code currently uses Hibernate's time-based generator, not a separately audited explicit RFC UUIDv7 generator. If strict UUIDv7 compliance matters, this should be verified or replaced.

### Vertical Slice Architecture
The code is organized by feature rather than a broad shared layer. This keeps User, Store, Catalog, Order, Social, and Admin behavior cohesive and reduces cross-feature coupling.

### Response Envelope
The backend moved from `code/message/data/error` to the mobile client contract:
`success/data/message/errorCode`.

This was done to keep Kotlin/Ktor client decoding stable and explicit.

### Mock Mode
Mock mode exists to support local development without Firebase credentials. It now defaults to `false` to avoid accidentally bypassing authentication in shared or production-like environments.

### Event-Driven Notifications
Checkout should not block on Firebase Messaging. Order status changes publish application events and notification work runs asynchronously.

## 4. Review Findings and Fix History

### Fixed
- **Mock auth default:** `MOCK_MODE` default changed to false.
- **API response mismatch:** Response envelope aligned with mobile client `BaseResponse`.
- **Invalid UUID path params:** Now return 400 error envelopes.
- **List response shape:** Store/product list endpoints return arrays in `data` for mobile decoding.
- **Review uniqueness:** Order-linked reviews now align with one-review-per-store semantics.
- **SKU cache staleness:** SKU creation evicts affected product/store catalog caches.
- **Historical order FKs:** Nullability fixed where `ON DELETE SET NULL` is required.
- **Test fragility:** Unit tests no longer require a live local PostgreSQL database for service/controller logic.
- **DTO UUID fallback:** Service mappers no longer synthesize random UUIDs when persisted IDs are missing.
- **Product hierarchy integrity:** Admin product creation validates that sub-category belongs to the target store.
- **Product lifecycle:** Draft/active/archive status added; public product APIs only expose active valid products.
- **Product price summary:** Replaced `priceCents` with SKU-derived `priceSummary`.

### Still Open / Scope for Improvement
- **DB-level product hierarchy integrity:** Product stores both `store_id` and `sub_category_id`. Service validation protects writes, but the database does not yet enforce cross-table consistency.
- **Testcontainers/PostGIS:** Spatial repository behavior should be verified against real PostgreSQL/PostGIS, not only unit tests.
- **Admin RBAC:** Admin endpoints exist, but role hardening and broader authorization review remain important.
- **Inventory:** SKU stock quantities, reservations, and concurrency controls are not implemented.
- **Observability:** Micrometer/Prometheus and production-grade logging/trace correlation are pending.
- **Image storage:** Image fields are URLs only; upload/storage service is future work.
- **Strict UUIDv7:** Verify or replace Hibernate time-based UUID generator if RFC UUIDv7 is a hard requirement.

## 5. Recent Change Timeline

### Product Lifecycle and Price Summary
- Added `ProductStatus` and `V10__Product_Status.sql`.
- Existing products were backfilled as `ACTIVE`.
- New products default to `DRAFT`.
- Added admin activation endpoint.
- Public product endpoints now require active products and available SKUs.
- Product price is summarized from SKU prices through `priceSummary`.

### Client Contract Alignment
- API response envelope changed to `success/data/message/errorCode`.
- Public list responses changed from paged objects to arrays.
- Boolean JSON names were pinned where Kotlin/Jackson would otherwise emit `available` instead of `isAvailable`.

### Review-Driven Stability
- Security, review, cache, and migration issues from code review were fixed.
- Unit coverage was expanded across backend domains.
- Historical FK behavior was corrected.

### Enhanced Slices
- Address CRUD and default address behavior.
- Advanced store discovery filters.
- Order-linked reviews and paginated feedback.
- Live tracking coordinates.
- Event-driven notifications.

## 6. Current Operational Notes
- `gradle test` passes after the latest lifecycle and price-summary changes.
- Local backend is run with:
  `/Users/hyperion/.gradle/wrapper/dists/gradle-8.14.3-bin/cv11ve7ro1n3o1j4so8xd9n66/gradle-8.14.3/bin/gradle bootRun --args='--nuvo.mock-mode=true'`
- Adminer credentials:
  - URL: `http://localhost:8081`
  - system: PostgreSQL
  - server: `postgres`
  - user: `postgres`
  - password: `postgres`
  - database: `nuvo_db`

## 7. TL;DR
Nuvo is now a stable Spring Boot 4/PostGIS delivery backend with store-specific catalogs, draft-to-active product lifecycle, SKU-derived product pricing, Firebase-compatible security, mock-mode local development, and a mobile-compatible response envelope. The main remaining production hardening items are database-level catalog integrity, real PostGIS integration tests, admin RBAC, inventory, observability, and image storage.

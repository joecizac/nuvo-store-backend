# Review Report: Nuvo Delivery Platform

`PROJECT_STATE.md` is now the canonical combined record for project state, architectural rationale, historical changes, review findings, and scope for improvement. This file remains as a concise review index for quick scanning.

## Current Review Status
- No active P0/P1 review findings are open from the latest review pass.
- The backend currently passes `gradle test`.
- Recent review-driven fixes were committed for response contract alignment, UUID/ID mapping behavior, product lifecycle, and SKU-derived product pricing.

## Key Review Areas

### Database and Performance
- **PostGIS:** Adopted intentionally for accurate radius search and GIST indexing.
- **Spatial casting:** Geography casting is required for meter-based distance.
- **N+1 risk:** Key repositories use `@EntityGraph`; additional integration profiling is still recommended.
- **Caching:** Store/catalog read paths use Spring Cache with explicit eviction on admin mutations.
- **Pending:** Add Testcontainers/PostGIS integration tests.

### Data Integrity
- **Fixed:** Historical order foreign-key nullability now matches `ON DELETE SET NULL`.
- **Fixed:** Product creation validates store/sub-category ownership in service logic.
- **Fixed:** Products now use `DRAFT`, `ACTIVE`, `ARCHIVED`; public APIs return only valid active products with available SKUs.
- **Pending:** Enforce product store/sub-category consistency at the database layer, because `products.store_id` and `products.sub_category_id` are redundant but useful for query performance.

### API Contract
- **Fixed:** Response envelope is `success/data/message/errorCode`.
- **Fixed:** Invalid UUID path parameters return clean 400 envelopes.
- **Fixed:** Public list endpoints return arrays for mobile clients.
- **Fixed:** Product responses use `priceSummary`; `priceCents` is removed.
- **Watch:** Keep backend domain correctness ahead of frontend convenience. Frontend contracts should adapt to domain-valid API behavior.

### Security
- **Fixed:** Mock mode defaults to false.
- **Implemented:** Firebase JWT verification path and local mock-mode path.
- **Pending:** Complete admin RBAC hardening using Firebase claims and endpoint-level authorization review.

### Testing
- **Implemented:** Broad unit coverage for services, controllers, response wrapping, and lifecycle logic.
- **Pending:** Real database integration tests for Flyway, PostGIS spatial queries, repository filtering, and product lifecycle persistence.

## Historical Findings Preserved
- UUIDv4 random IDs were replaced by Hibernate time-ordered UUID generation to improve index locality.
- Read operations gained read-only transactions to reduce Hibernate overhead.
- Custom exception hierarchy replaced generic exception handling.
- Asynchronous notification events prevent checkout from blocking on Firebase Messaging.
- Store-specific catalog hierarchy replaced earlier global category assumptions.

## Improvement Backlog
- Testcontainers with PostGIS.
- DB-level product hierarchy integrity.
- Admin RBAC.
- SKU inventory and reservation model.
- Image upload/storage service.
- Observability with metrics, structured logs, and tracing.
- Strict UUIDv7 verification if standards compliance is required beyond time-ordered UUID behavior.

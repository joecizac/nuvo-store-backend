# Implementation Plan (LLD): Nuvo Delivery Backend

## 1. Architecture
- **Runtime:** Kotlin 2.2.x, Java 17, Spring Boot 4.0.x.
- **Architecture style:** Vertical slices by feature: User, Store, Catalog, Order, Social, Admin.
- **Database:** PostgreSQL 15+ with PostGIS.
- **Migrations:** Flyway, currently through `V10__Product_Status.sql`.
- **Persistence:** Hibernate ORM 7, JPA repositories, PostGIS spatial queries, entity graphs for high-risk N+1 paths.
- **Security:** Stateless Spring Security filter validating Firebase JWTs; mock mode for local development.
- **API standard:** `GlobalResponseWrapper` wraps ordinary controller responses into `ApiResponse(success, data, message, errorCode)`.

## 2. Domain Model

### 2.1 Identity
- `User`: Firebase UID, email, name, phone, profile image URL, FCM token.
- `UserAddress`: user, title, full address, PostGIS point, default flag.
- Address ownership is validated for update/delete/default operations.

### 2.2 Store Discovery
- `Chain`: shared brand metadata.
- `Store`: optional `chain_id`, location, address, active flag, rating, cuisine/industry metadata, price range, delivery fee, opening hours.
- Independent stores have `chain_id = null`.
- Deleting a chain sets store `chain_id` to null rather than deleting stores.
- Spatial queries use PostGIS `ST_DWithin` with geography casting for meter-based distance.

### 2.3 Catalog
- Store-owned hierarchy:
  `Store -> Category -> SubCategory -> Product -> SKU`
- `Category.store_id` is required.
- `SubCategory.category_id` is required.
- `Product.store_id` and `Product.sub_category_id` are required.
- Service logic validates product store consistency against `subCategory.category.store`.
- `SKU.product_id` is required and stores price/availability.

### 2.4 Product Lifecycle
- `ProductStatus`: `DRAFT`, `ACTIVE`, `ARCHIVED`.
- New products are created as `DRAFT`.
- SKUs are created separately through admin APIs.
- Product activation endpoint:
  `PATCH /api/v1/admin/products/{productId}/activate`
- Activation validations:
  - product exists
  - product's sub-category belongs to the product's store
  - product has at least one SKU
  - at least one SKU is available
  - original price is greater than zero
  - discounted price, when present, is greater than zero and not greater than original price
- Public product endpoints query only `ACTIVE`, available products that have available SKUs.

### 2.5 Product Pricing
- Product price is derived from available SKUs and is not stored on `products`.
- Effective SKU price: `discountedPrice ?: originalPrice`.
- API price values are integer minor units.
- `ProductDTO.priceSummary`:
  - `minPrice`
  - `maxPrice`
  - `displayPrice`
  - `hasPriceRange`
  - `currency`
- `priceCents` has been removed from product responses.
- Public product DTOs include only available SKUs.

### 2.6 Cart and Order
- `Cart` is one-per-user and can point to one store.
- `CartItem` points to SKU and quantity.
- Single-store rule is enforced in `CartService`.
- `Order` snapshots delivery address as JSONB with `@JdbcTypeCode(SqlTypes.JSON)`.
- `OrderItem` snapshots SKU price and quantity.
- Historical order FKs are nullable where history must survive deletion.

### 2.7 Social and Notifications
- Store reviews are one per user/store, with optional order link.
- Store average rating is denormalized after review writes.
- Favourites use composite keys for stores/products.
- Order status changes publish events; notification delivery is asynchronous.

## 3. API Surface

### 3.1 Public Store/Catalog APIs
- `GET /api/v1/stores`
  - Returns nearby store list array.
- `GET /api/v1/stores/{id}`
  - `id` must be UUID.
- `GET /api/v1/stores/{storeId}/categories`
  - Returns store category array.
- `GET /api/v1/stores/{storeId}/products`
  - Returns active product array only.
  - Optional `subCategoryId`.
  - Product objects include `priceSummary`.
- `GET /api/v1/products/{productId}`
  - Returns only public-valid active products.

### 3.2 Admin APIs
- `POST /api/v1/admin/chains`
- `POST /api/v1/admin/stores`
- `POST /api/v1/admin/stores/{storeId}/categories`
- `POST /api/v1/admin/categories/{categoryId}/sub-categories`
- `POST /api/v1/admin/stores/{storeId}/products`
  - Creates draft product.
- `POST /api/v1/admin/products/{productId}/skus`
  - Adds SKU to product after price validation.
- `PATCH /api/v1/admin/products/{productId}/activate`
  - Publishes valid product.

## 4. Database Migrations
- `V1`: Users.
- `V2`: Chains, stores, PostGIS store location index.
- `V3`: Categories, sub-categories, products, SKUs.
- `V4`: Carts and orders.
- `V5`: Reviews and favourites.
- `V6`: Store discovery enhancements.
- `V7`: Engagement/tracking enhancements.
- `V8`: Search performance optimization.
- `V9`: Historical order FK nullability.
- `V10`: Product status with existing products backfilled to `ACTIVE` and default set to `DRAFT`.

## 5. Local Development
- Start database stack: `docker compose up -d`.
- Backend default datasource:
  - URL: `jdbc:postgresql://localhost:5432/nuvo_db`
  - username: `postgres`
  - password: `postgres`
- Adminer:
  - URL: `http://localhost:8081`
  - system: PostgreSQL
  - server: `postgres`
  - user: `postgres`
  - password: `postgres`
  - database: `nuvo_db`
- Mock mode default is `false`; run local backend with `--nuvo.mock-mode=true` when Firebase credentials are unavailable.
- Preferred Gradle install:
  `/Users/hyperion/.gradle/wrapper/dists/gradle-8.14.3-bin/cv11ve7ro1n3o1j4so8xd9n66/gradle-8.14.3/bin/gradle`

## 6. Test Strategy
- Unit tests cover common response wrapping, controller delegation, admin/catalog/order/user/social service behavior.
- Current `gradle test` passes.
- Testcontainers/PostGIS integration tests remain pending for real spatial query verification and migration/database contract validation.

## 7. Known Implementation Notes
- Entity IDs use Hibernate time-ordered UUID generation via `@UuidGenerator(style = UuidGenerator.Style.TIME)`. This is chosen for write locality; if strict RFC UUIDv7 semantics are required, the generator should be reviewed/replaced explicitly.
- Product keeps both `store_id` and `sub_category_id` for query efficiency. Service validation enforces consistency; a future DB trigger or schema redesign could enforce this invariant at database level.
- Public catalog APIs intentionally prioritize domain correctness over frontend convenience.

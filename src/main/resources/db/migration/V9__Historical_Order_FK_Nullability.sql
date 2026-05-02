-- Historical orders should survive deletion of users, stores, or SKUs.
-- V4 already used ON DELETE SET NULL; these columns must also allow NULL.
ALTER TABLE orders ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE orders ALTER COLUMN store_id DROP NOT NULL;
ALTER TABLE order_items ALTER COLUMN sku_id DROP NOT NULL;

-- Enable pg_trgm extension for fast ILIKE search
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Add GIN indexes for fuzzy search on names and descriptions
CREATE INDEX IF NOT EXISTS idx_stores_name_trgm ON stores USING gin (name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_stores_description_trgm ON stores USING gin (description gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_products_name_trgm ON products USING gin (name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_skus_name_trgm ON skus USING gin (name gin_trgm_ops);

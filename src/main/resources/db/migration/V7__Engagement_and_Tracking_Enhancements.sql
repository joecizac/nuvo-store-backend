-- Link reviews to specific orders
ALTER TABLE reviews ADD COLUMN order_id UUID REFERENCES orders(id) ON DELETE SET NULL;

-- Add tracking coordinates to orders
ALTER TABLE orders ADD COLUMN current_lat DOUBLE PRECISION;
ALTER TABLE orders ADD COLUMN current_lng DOUBLE PRECISION;

-- Chains table
CREATE TABLE chains (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    logo_url TEXT,
    banner_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Stores table
CREATE TABLE stores (
    id UUID PRIMARY KEY,
    chain_id UUID REFERENCES chains(id) ON DELETE SET NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    contact_number VARCHAR(20),
    logo_url TEXT,
    banner_url TEXT,
    location GEOMETRY(Point, 4326) NOT NULL,
    address TEXT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    average_rating DOUBLE PRECISION DEFAULT 0.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Spatial index for store discovery
CREATE INDEX idx_stores_location ON stores USING GIST (location);

-- Enable PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;

-- Users table (matches your User entity)
CREATE TABLE IF NOT EXISTS "user" (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'USER',  -- USER, MANAGER
    login_attempts INTEGER DEFAULT 0,
    is_blocked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table for road issues with geolocation
CREATE TABLE IF NOT EXISTS road_issues (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES "user"(id),
    location GEOGRAPHY(POINT, 4326),  -- Lat/Long point
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    description TEXT,
    status VARCHAR(50) DEFAULT 'nouveau',  -- nouveau, en_cours, terminé
    surface_m2 DECIMAL(10,2),
    budget DECIMAL(15,2),
    entreprise VARCHAR(255),
    photo_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Spatial index for faster queries
CREATE INDEX idx_road_issues_location ON road_issues USING GIST(location);

-- Create default manager account (password: manager123 - should be hashed in production)
INSERT INTO "user" (email, mot_de_passe, role) 
VALUES ('manager@gmail.com', 'man', 'MANAGER')
ON CONFLICT (email) DO NOTHING;

-- Example: Insert a test road issue in Antananarivo center
INSERT INTO road_issues (user_id, location, latitude, longitude, description, status, surface_m2, budget)
VALUES (
    1,
    ST_GeogFromText('POINT(47.5079 -18.9079)'),
    -18.9079,
    47.5079,
    'Nid de poule avenue de l''Indépendance',
    'nouveau',
    15.5,
    500000
) ON CONFLICT DO NOTHING;
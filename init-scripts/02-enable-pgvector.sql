-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Verify installation
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 
        FROM pg_extension 
        WHERE extname = 'vector'
    ) THEN
        RAISE NOTICE 'pgvector extension installed successfully';
    ELSE
        RAISE EXCEPTION 'pgvector extension installation failed';
    END IF;
END $$;


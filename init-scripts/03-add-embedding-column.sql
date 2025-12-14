-- Add embedding column to innovations table if not exists
-- This script is safe to run multiple times (idempotent)

DO $$
BEGIN
    -- Enable pgvector extension if not already enabled
    CREATE EXTENSION IF NOT EXISTS vector;

    -- Add embedding column if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'innovations' 
        AND column_name = 'embedding'
    ) THEN
        ALTER TABLE innovations 
        ADD COLUMN embedding vector(384);
        
        RAISE NOTICE 'Added embedding column to innovations table';
    ELSE
        RAISE NOTICE 'embedding column already exists in innovations table';
    END IF;
END $$;


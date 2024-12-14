-- Create extensions
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nom VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    motdepasse VARCHAR(255) NOT NULL
);

-- Create discussions table
CREATE TABLE IF NOT EXISTS discussion (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    message TEXT,
    date TIMESTAMP NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id)
);

-- Create langchain tables for vector store with updated schema
CREATE TABLE IF NOT EXISTS langchain_pg_collection (
    name VARCHAR(50) NOT NULL,
    cmetadata JSONB,  
    uuid UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS langchain_pg_embedding (
    collection_id UUID REFERENCES langchain_pg_collection(uuid) ON DELETE CASCADE,
    embedding vector(384),
    document TEXT,
    cmetadata JSONB,  
    uuid UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_collection_name ON langchain_pg_collection(name);
CREATE INDEX IF NOT EXISTS idx_embedding_collection ON langchain_pg_embedding(collection_id);
CREATE INDEX IF NOT EXISTS idx_collection_cmetadata ON langchain_pg_collection USING GIN (cmetadata);
CREATE INDEX IF NOT EXISTS idx_embedding_cmetadata ON langchain_pg_embedding USING GIN (cmetadata);
-- Create extensions if not exists
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nom VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    motdepasse VARCHAR(255) NOT NULL
);

-- Create spaces table
CREATE TABLE IF NOT EXISTS spaces (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE
);

-- Create discussions table
CREATE TABLE IF NOT EXISTS discussions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    message TEXT,
    created_at TIMESTAMP NOT NULL,
    space_id UUID NOT NULL REFERENCES spaces(id) ON DELETE CASCADE,
    message_type VARCHAR(20) NOT NULL -- 'CHAT' or 'CODE_ANALYSIS'
);

-- Update langchain tables schema with space_id and repo_url
CREATE TABLE IF NOT EXISTS langchain_pg_collection (
    name VARCHAR(50) NOT NULL,
    repo_url TEXT,
    space_id UUID REFERENCES spaces(id) ON DELETE CASCADE,
    cmetadata JSONB,
    uuid UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS langchain_pg_embedding (
    collection_id UUID REFERENCES langchain_pg_collection(uuid) ON DELETE CASCADE,
    embedding vector(384),
    document TEXT,
    cmetadata JSONB,
    uuid UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_space_user ON spaces(user_id);
CREATE INDEX IF NOT EXISTS idx_discussion_space ON discussions(space_id);
CREATE INDEX IF NOT EXISTS idx_collection_space ON langchain_pg_collection(space_id);
CREATE INDEX IF NOT EXISTS idx_collection_created ON langchain_pg_collection(created_at);
CREATE INDEX IF NOT EXISTS idx_embedding_collection ON langchain_pg_embedding(collection_id);
-- Initial schema creation for FlowFin AI documents table
CREATE TABLE IF NOT EXISTS documents (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT documents_status_check CHECK (status IN ('PENDING', 'PROCESSING', 'INDEXED', 'FAILED'))
);

CREATE INDEX IF NOT EXISTS idx_documents_status ON documents(status);
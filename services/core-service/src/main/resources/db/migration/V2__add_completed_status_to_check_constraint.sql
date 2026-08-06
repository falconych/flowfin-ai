-- Add COMPLETED to documents_status_check constraint
ALTER TABLE documents DROP CONSTRAINT IF EXISTS documents_status_check;

ALTER TABLE documents ADD CONSTRAINT documents_status_check 
    CHECK (status IN ('PENDING', 'PROCESSING', 'INDEXED', 'FAILED', 'COMPLETED'));
ALTER TABLE tickets
ADD COLUMN due_at TIMESTAMP;

UPDATE tickets
SET due_at = CASE priority
    WHEN 'HIGH' THEN created_at + INTERVAL '4 hours'
    WHEN 'MEDIUM' THEN created_at + INTERVAL '1 day'
    ELSE created_at + INTERVAL '3 days'
END
WHERE due_at IS NULL;

ALTER TABLE tickets
ALTER COLUMN due_at SET NOT NULL;

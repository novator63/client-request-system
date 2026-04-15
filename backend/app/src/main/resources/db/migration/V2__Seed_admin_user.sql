INSERT INTO users (
    email,
    password_hash,
    full_name,
    role,
    is_active,
    created_at,
    updated_at
) VALUES (
    'admin@example.com',
    '$2b$12$NUdfq6CHgzUwUCIIAQGojOUzwnnvfnw5jRqEChcOZwLxScW266HT.',
    'System Administrator',
    'ADMIN',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

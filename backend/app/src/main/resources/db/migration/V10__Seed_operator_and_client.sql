INSERT INTO users (
    email,
    password_hash,
    full_name,
    role,
    is_active,
    created_at,
    updated_at
) VALUES
    (
        'operator@example.com',
        '$2y$12$F.oG172MI/gKKmG2enBq4OPH6.MU2E2ISm18F/Vsv5rKyaIvtkMES',
        'Support Operator',
        'OPERATOR',
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'client@example.com',
        '$2y$12$sdnwVJWaGVHM9xv1lvNFyOjJFjiJdr8OabiJ0bnLsRlvViirFR7yu',
        'Test Client',
        'CLIENT',
        TRUE,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
ON CONFLICT (email) DO NOTHING;

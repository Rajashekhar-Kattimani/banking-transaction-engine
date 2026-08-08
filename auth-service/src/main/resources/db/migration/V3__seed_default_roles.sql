INSERT INTO roles (
    id,
    version,
    created_at,
    updated_at,
    name,
    description
)
VALUES (
    gen_random_uuid()::text,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    'ROLE_USER',
    'Default user role'
)
ON CONFLICT (name) DO NOTHING;
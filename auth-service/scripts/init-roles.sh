#!/bin/bash
# Insert default roles into auth_db if they don't exist

export PGPASSWORD="postgres"

echo "Inserting default roles into auth_db..."

psql -h localhost -U postgres -d auth_db << EOF
INSERT INTO roles (id, version, created_at, updated_at, name, description) 
VALUES (gen_random_uuid()::text, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ROLE_USER', 'Default user role')
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (id, version, created_at, updated_at, name, description) 
VALUES (gen_random_uuid()::text, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ROLE_ADMIN', 'Administrator role')
ON CONFLICT (name) DO NOTHING;

SELECT name FROM roles;
EOF

echo "Role initialization completed."

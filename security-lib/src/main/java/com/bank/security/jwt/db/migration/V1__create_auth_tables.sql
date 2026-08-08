-- ============================================================
-- AUTH SERVICE - V1
-- Create authentication and authorization tables
-- ============================================================

-- ============================================================
-- ROLES
-- ============================================================

CREATE TABLE roles (
    id VARCHAR(36) NOT NULL,
    version BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,

    name VARCHAR(50) NOT NULL,
    description VARCHAR(255),

    CONSTRAINT pk_roles PRIMARY KEY (id),
    CONSTRAINT uk_roles_name UNIQUE (name)
);

-- ============================================================
-- PERMISSIONS
-- ============================================================

CREATE TABLE permissions (
    id VARCHAR(36) NOT NULL,
    version BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,

    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),

    CONSTRAINT pk_permissions PRIMARY KEY (id),
    CONSTRAINT uk_permissions_name UNIQUE (name)
);

-- ============================================================
-- USERS
-- ============================================================

CREATE TABLE users (
    id VARCHAR(36) NOT NULL,
    version BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,

    username VARCHAR(50) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password VARCHAR(255) NOT NULL,

    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,

    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    account_locked_until TIMESTAMP WITH TIME ZONE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    password_changed_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email)
);

-- ============================================================
-- USER <-> ROLE
-- ============================================================

CREATE TABLE user_roles (
    user_id VARCHAR(36) NOT NULL,
    role_id VARCHAR(36) NOT NULL,

    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id)
        REFERENCES roles (id)
        ON DELETE CASCADE
);

-- ============================================================
-- ROLE <-> PERMISSION
-- ============================================================

CREATE TABLE role_permissions (
    role_id VARCHAR(36) NOT NULL,
    permission_id VARCHAR(36) NOT NULL,

    CONSTRAINT pk_role_permissions PRIMARY KEY (role_id, permission_id),

    CONSTRAINT fk_role_permissions_role
        FOREIGN KEY (role_id)
        REFERENCES roles (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_role_permissions_permission
        FOREIGN KEY (permission_id)
        REFERENCES permissions (id)
        ON DELETE CASCADE
);

-- ============================================================
-- INDEXES
-- ============================================================

CREATE INDEX idx_user_roles_role_id
    ON user_roles (role_id);

CREATE INDEX idx_role_permissions_permission_id
    ON role_permissions (permission_id);
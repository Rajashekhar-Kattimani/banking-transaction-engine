# Auth Service - User Registration Fix Summary

## Problem
When attempting to register a user via `/api/v1/auth/register`, the service throws:
```
java.lang.IllegalStateException: Default role ROLE_USER not found
```

This occurs because the auth service tries to assign `ROLE_USER` to new users during registration, but the role doesn't exist in the database.

---

## Root Cause
1. **Missing Database Initialization**: The `ROLE_USER` role is defined in Flyway migration script `V3__seed_default_roles.sql`, but:
   - Flyway dependency was not included in `auth-service/pom.xml`
   - Database migrations were never executed
   
2. **No Automatic Role Creation**: There was no mechanism to create default roles if they didn't exist on application startup

---

## Solution Implemented

### 1. Added Flyway Database Migration Support
**File**: `auth-service/pom.xml`

Added dependencies:
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

**Already configured in `application.yml`**:
```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
```

This enables automatic database migration on application startup.

### 2. Created RoleInitializer Component
**File**: `auth-service/src/main/java/com/bank/auth/config/RoleInitializer.java`

```java
@Component
@RequiredArgsConstructor
public class RoleInitializer {
    private final RoleRepository roleRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeRoles() {
        // Creates ROLE_USER and ROLE_ADMIN if they don't exist
        // Logs initialization status
    }
}
```

This component:
- Listens for application startup event
- Automatically creates `ROLE_USER` and `ROLE_ADMIN` if they don't exist
- Provides fallback in case Flyway migration doesn't run
- Logs all role initialization steps for debugging

### 3. Fixed Role Entity Builder
**File**: `auth-service/src/main/java/com/bank/auth/role/entity/Role.java`

Added annotations:
```java
@SuperBuilder      // Enables builder pattern for inheritance
@NoArgsConstructor // Required for JPA
public class Role extends BaseEntity {
    // ...
}
```

This allows `RoleInitializer` to use `Role.builder()` syntax for creating roles programmatically.

---

## Database Migration Files

The following Flyway migrations exist in `auth-service/src/main/resources/db/migration/`:

1. **V1__create_auth_tables.sql** - Creates users, roles, permissions, and junction tables
2. **V2__create_refresh_tokens.sql** - Creates refresh token table
3. **V3__seed_default_roles.sql** - Inserts ROLE_USER and ROLE_ADMIN roles

With Flyway enabled, these migrations run automatically on startup.

---

## Files Modified

| File | Change |
|------|--------|
| `auth-service/pom.xml` | Added Flyway dependencies |
| `auth-service/src/main/java/com/bank/auth/config/RoleInitializer.java` | NEW: Automatic role creation |
| `auth-service/src/main/java/com/bank/auth/role/entity/Role.java` | Added @SuperBuilder, @NoArgsConstructor |

---

## How It Works

1. **Application Startup**:
   - Spring Boot initializes the auth-service
   - Flyway runs migrations (V1, V2, V3)
   - V3 creates default roles if they don't exist

2. **Fallback Role Initialization**:
   - After Flyway runs, `RoleInitializer` component is instantiated
   - `@EventListener(ApplicationReadyEvent.class)` triggers on application startup
   - Checks if `ROLE_USER` exists; if not, creates it
   - Also creates `ROLE_ADMIN` for future use

3. **User Registration**:
   - `/api/v1/auth/register` endpoint is called
   - `AuthServiceImpl.register()` queries for `ROLE_USER`
   - Role is found (either from Flyway or RoleInitializer)
   - User is created with `ROLE_USER` role
   - Registration succeeds

---

## Testing the Fix

### Build auth-service
```bash
mvn clean install -DskipTests -f auth-service/pom.xml
```

### Start auth-service
```bash
java -Duser.timezone=Asia/Kolkata -jar auth-service/target/auth-service-0.1.0-SNAPSHOT.jar
```

### Register a user
```bash
curl -X POST http://localhost:9081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "raj",
    "email": "raj@example.com",
    "password": "Password@123"
  }'
```

**Expected Response** (HTTP 200):
```json
{
  "id": "uuid-here",
  "username": "raj",
  "email": "raj@example.com"
}
```

**Error Response** (If roles not created):
```json
{
  "success": false,
  "errorCode": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred"
}
```

---

## Verification Steps

### 1. Check Flyway Migration Status
The startup logs should show:
```
INFO  Flyway : Database driver: ... PostgreSQL
INFO  Flyway : Successfully validated ...
INFO  Flyway : Running migration V1__create_auth_tables.sql
INFO  Flyway : Running migration V2__create_refresh_tokens.sql
INFO  Flyway : Running migration V3__seed_default_roles.sql
```

### 2. Check Role Initialization Logs
The startup logs should show:
```
INFO  RoleInitializer : Initializing default roles...
INFO  RoleInitializer : ROLE_USER already exists
INFO  RoleInitializer : ROLE_ADMIN already exists
INFO  RoleInitializer : Role initialization completed
```

### 3. Query Database
Connect to auth_db and verify:
```sql
SELECT * FROM roles;

-- Should return:
-- id           | name      | description
-- -------------|-----------|---------------------
-- (uuid)       | ROLE_USER | Default user role
-- (uuid)       | ROLE_ADMIN| Administrator role
```

---

## Troubleshooting

### If roles are still not found:
1. **Check Flyway is enabled** in `application.yml`
2. **Verify database connection** in `application.yml` datasource config
3. **Check database exists** - auth_db must exist on PostgreSQL
4. **Check logs** for Flyway or RoleInitializer errors

### Manual Role Creation (Emergency)
If automatic initialization fails, manually insert roles:
```bash
psql -h localhost -U postgres -d auth_db << EOF
INSERT INTO roles (id, version, created_at, updated_at, name, description) 
VALUES (gen_random_uuid()::text, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ROLE_USER', 'Default user role')
ON CONFLICT (name) DO NOTHING;
EOF
```

---

## Next Steps

1. ✅ Added Flyway dependency
2. ✅ Created RoleInitializer component
3. ✅ Fixed Role entity builder
4. **TODO**: Test user registration endpoint
5. **TODO**: Verify role creation in database
6. **TODO**: Test login with registered user

---

**Date**: August 9, 2026
**Service**: auth-service (v0.1.0-SNAPSHOT)
**Port**: 9081
**Status**: Ready for testing


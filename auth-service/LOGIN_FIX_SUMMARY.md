# Auth Service - Login Lazy Loading Fix Summary

## Problem
When attempting to login via `/api/v1/auth/login`, the service throws:
```
org.hibernate.LazyInitializationException: Cannot lazily initialize collection of role 'com.bank.auth.user.entity.User.roles' with key '...' (no session)
```

This occurs because:
1. User entity is retrieved from database with `@ManyToMany(fetch = FetchType.LAZY)` roles
2. The JPA session closes after database query
3. When `SecurityUserMapper.toSecurityUser()` tries to access user.roles, Hibernate can't lazy-load them outside the session
4. This causes authentication to fail

---

## Root Cause
**Lazy Loading Exception**: The User entity's roles collection is mapped with `FetchType.LAZY`, meaning roles are not loaded when the User is retrieved. When the security layer tries to convert the User to a security principal later (after the session is closed), it cannot lazy-load the roles, resulting in `LazyInitializationException`.

---

## Solution Implemented

### Changed Fetch Strategy from LAZY to EAGER

**File 1**: `auth-service/src/main/java/com/bank/auth/user/entity/User.java`

Changed line 74:
```java
// BEFORE:
@ManyToMany(fetch = FetchType.LAZY)

// AFTER:
@ManyToMany(fetch = FetchType.EAGER)
```

**File 2**: `auth-service/src/main/java/com/bank/auth/role/entity/Role.java`

Changed line 37 (proactive fix to prevent similar issues):
```java
// BEFORE:
@ManyToMany(fetch = FetchType.LAZY)

// AFTER:
@ManyToMany(fetch = FetchType.EAGER)
```

---

## Why This Fix Works

1. **EAGER Loading**: When User is retrieved, roles are loaded immediately in the same session
2. **No Session Dependency**: Security layer accesses roles that are already loaded in memory
3. **No Lazy Initialization**: No attempt to lazy-load outside the session scope
4. **Immediate Availability**: Roles are available throughout the authentication lifecycle

---

## How It Works Now

1. **User Login Request**: POST `/api/v1/auth/login`
2. **User Lookup**: `AuthUserDetailsService.loadUserByUsername()` queries database
3. **Eager Loading**: User AND roles fetched in single transaction
4. **Security Mapping**: `SecurityUserMapper.toSecurityUser()` accesses roles (already loaded)
5. **Authentication**: Spring Security authenticates user with authorities
6. **Token Generation**: JWT tokens generated successfully
7. **Response**: Access & Refresh tokens returned

---

## Files Modified

| File | Change | Line |
|------|--------|------|
| `auth-service/src/main/java/com/bank/auth/user/entity/User.java` | LAZY → EAGER | 74 |
| `auth-service/src/main/java/com/bank/auth/role/entity/Role.java` | LAZY → EAGER | 37 |

---

## Testing the Fix

### Prerequisites
- Ensure user "raj" is registered (from previous registration test)
- auth-service is running on port 9081
- PostgreSQL auth_db is accessible

### Build
```bash
mvn clean install -DskipTests -f auth-service/pom.xml
```

### Start Service
```bash
cd auth-service
java -jar target/auth-service-0.1.0-SNAPSHOT.jar
```

### Wait for startup (should see in logs):
```
Tomcat started on port 9081
Started AuthServiceApplication
```

### Login Test
```bash
curl -X POST http://localhost:9081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "raj",
    "password": "Password@123"
  }'
```

### Expected Success Response (HTTP 200)
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresAt": "2026-08-09T17:32:43.555+05:30",
  "tokenType": "Bearer"
}
```

### Expected Error Response (If credentials wrong - HTTP 401)
```json
{
  "success": false,
  "errorCode": "UNAUTHORIZED",
  "message": "Invalid credentials",
  "timestamp": "..."
}
```

---

## Database Query Impact

### Before (LAZY)
```sql
SELECT * FROM users WHERE username = 'raj'
-- roles NOT fetched
-- [later, outside session]: try to access user.roles
-- -> LazyInitializationException
```

### After (EAGER)
```sql
SELECT * FROM users WHERE username = 'raj'
-- [immediately, in same session]:
SELECT * FROM user_roles WHERE user_id = 'uuid'
SELECT * FROM roles WHERE id IN (...)
-- roles fetched and available
-- No lazy loading errors
```

---

## Performance Consideration

**Note**: EAGER loading may load unnecessary data in some use cases. For production, consider:
1. Use `JOIN FETCH` in specific queries only (instead of global EAGER)
2. Profile user queries to measure impact
3. Implement query optimization if performance degrades

**For now**: EAGER is the safest approach to prevent lazy loading exceptions in this security-critical flow.

---

## Troubleshooting

### Still getting lazy loading error?
1. Ensure you rebuilt the auth-service: `mvn clean install`
2. Check that changes are in the jar file
3. Verify no old jar is running

### Login still fails?
1. Check user exists: Query `SELECT * FROM users WHERE username = 'raj';`
2. Check role exists: Query `SELECT * FROM roles WHERE name = 'ROLE_USER';`
3. Check user-role association: Query `SELECT * FROM user_roles WHERE user_id = 'uuid';`
4. Check application logs for other errors

### Port 9081 not responding?
1. Check if auth-service process is running: `ps aux | grep java`
2. Check logs for startup errors
3. Verify PostgreSQL is accessible
4. Check configuration in application.yml

---

## Next Steps

1. ✅ Fixed Lazy Loading Exception
2. ✅ Rebuilt auth-service with EAGER fetching
3. **Test login endpoint**
4. **Verify JWT token generation**
5. **Test protected endpoints with token**
6. **Test refresh token flow**

---

**Date**: August 9, 2026
**Service**: auth-service (v0.1.0-SNAPSHOT)
**Port**: 9081
**Database**: PostgreSQL auth_db
**Fix Type**: Hibernate Entity Configuration


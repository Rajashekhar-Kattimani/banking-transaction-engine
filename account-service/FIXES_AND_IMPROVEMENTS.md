# Account Service - Fixes & Improvements

## Issue Fixed
**Problem:** The `/actuator/health` endpoint returned HTTP 500 with generic error message:
```json
{
  "success": false,
  "errorCode": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred"
}
```

**Root Cause:** The global `@RestControllerAdvice` exception handler was intercepting exceptions from Spring Boot Actuator endpoints (which run outside the `com.bank.account` package) and masking them with the generic ErrorResponse, causing the health endpoint to fail.

**Solution Applied:**
1. Restricted `GlobalExceptionHandler` to only apply to `com.bank.account` package
2. Added `spring-boot-starter-actuator` dependency
3. Configured actuator profiles for dev/prod
4. Added logging to exception handler
5. Produced executable Spring Boot jar with proper packaging

---

## Files Changed

### 1. GlobalExceptionHandler.java
- **Added:** `basePackages = "com.bank.account"` to `@RestControllerAdvice` annotation
- **Effect:** Handler now only applies to your app's controllers, not framework internals
- **Added:** Logger for unhandled exceptions (logs stack trace for debugging)

### 2. pom.xml
- **Added:** `spring-boot-starter-actuator` dependency
- **Added:** Spring Boot Maven plugin configuration with `repackage` execution
- **Effect:** Builds an executable fat jar with all dependencies; can run with `java -jar`

### 3. application.yml
- **Removed:** `management.endpoint.health.show-details: always` from default config
- **Effect:** Details are now controlled per profile (dev shows all, prod shows on auth)

### 4. application-dev.yml (new)
```yaml
management:
  endpoint:
    health:
      show-details: always
```
- **Use:** Activate with `--spring.profiles.active=dev` to expose health details in development

### 5. application-prod.yml (new)
```yaml
management:
  endpoint:
    health:
      show-details: when-authorized
```
- **Use:** Activate with `--spring.profiles.active=prod` for secure production mode

### 6. scripts/run-account-service.ps1 (new)
PowerShell script to start the jar with proper timezone handling.

**Usage:**
```powershell
# From scripts directory or anywhere:
& D:\tools\services\banking-transaction-engine\account-service\scripts\run-account-service.ps1

# Custom jar path and port:
& script.ps1 -JarPath "path\to\jar" -Timezone "UTC" -Port 8080
```

### 7. ci/smoke-test.ps1 (new)
PowerShell script for CI/CD health check validation.

**Usage:**
```powershell
# From CI pipeline or manually:
& D:\tools\services\banking-transaction-engine\account-service\ci\smoke-test.ps1

# Custom URL and retries:
& smoke-test.ps1 -Url "http://localhost:9082/actuator/health" -Attempts 5 -DelaySeconds 2
```

---

## Verification - Current Status

### ✅ Health Endpoint Now Returns 200 OK
```bash
curl -i http://localhost:9082/actuator/health
```

**Response (200 OK):**
```json
{
  "components": {
    "db": {
      "details": {"database": "PostgreSQL", "validationQuery": "isValid()"},
      "status": "UP"
    },
    "redis": {
      "details": {"version": "8.10.0"},
      "status": "UP"
    },
    "diskSpace": {"status": "UP", ...},
    "livenessState": {"status": "UP"},
    "readinessState": {"status": "UP"},
    "ssl": {"status": "UP"}
  },
  "status": "UP"
}
```

### ✅ Smoke Test Passes
```
Health check succeeded (HTTP 200).
```

---

## Build & Run Instructions

### Build
```powershell
# Compile and package the service
mvn -DskipTests -f account-service\pom.xml package
```

This produces: `account-service\target\account-service-0.1.0-SNAPSHOT.jar` (executable fat jar)

### Run Executable Jar (Standalone)
```powershell
# Option 1: Using provided script
.\scripts\run-account-service.ps1

# Option 2: Direct java command
java -Duser.timezone=Asia/Kolkata -jar target\account-service-0.1.0-SNAPSHOT.jar

# Option 3: With development profile (shows health details)
java -Duser.timezone=Asia/Kolkata -Dspring.profiles.active=dev -jar target\account-service-0.1.0-SNAPSHOT.jar

# Option 4: With production profile (secure, requires auth for details)
java -Duser.timezone=Asia/Kolkata -Dspring.profiles.active=prod -jar target\account-service-0.1.0-SNAPSHOT.jar
```

### Run via Maven (Development)
```powershell
# Runs in foreground with full logs
mvn -f account-service\pom.xml spring-boot:run
```

---

## Security Recommendations

### Current Setup
- ✅ Exception handler restricted to app package
- ✅ Actuator endpoints exposed: `/actuator/health`, `/actuator/info`
- ⚠️ Health details shown to all (dev profile only)

### For Production
1. **Deploy with production profile:**
   ```powershell
   java -Dspring.profiles.active=prod -jar account-service-0.1.0-SNAPSHOT.jar
   ```
   - Health endpoint returns minimal status without details
   - Details only shown to authenticated/authorized users

2. **Restrict actuator network access:**
   - Run on internal port (e.g., `management.server.port: 9099`)
   - Or use firewall/proxy rules

3. **Audit logs:**
   - Enable detailed logging for exception traces (already configured in GlobalExceptionHandler)
   - Integrate with centralized logging (observability-lib)

---

## Testing

### Health Check (Manual)
```powershell
# Should return 200 with full details (dev) or minimal status (prod)
curl -i http://localhost:9082/actuator/health
```

### Health Check (CI/CD Automated)
```powershell
# Run smoke test (retries 5x with 2s delay)
.\ci\smoke-test.ps1

# Exit code 0 = success, 1 = failure
echo $LASTEXITCODE
```

### Endpoint Listing
```powershell
curl http://localhost:9082/actuator
```

---

## Profiles & Configuration

### Development Profile (default, or -Dspring.profiles.active=dev)
- Health details: **always** (all internal components shown)
- Use for: local development, debugging

### Production Profile (-Dspring.profiles.active=prod)
- Health details: **when-authorized** (minimal unless authenticated)
- Use for: production deployment

### Custom Profiles
Add `application-<profile>.yml` for other environments (staging, qa, etc.)

---

## Related Services

The same issue may affect other services if they have global exception handlers. Check:
- `auth-service/src/main/java/.../exception/GlobalExceptionHandler.java`
- `transaction-service/src/main/java/.../exception/GlobalExceptionHandler.java`

**Recommended fix for all services:**
```java
@RestControllerAdvice(basePackages = "com.bank.<service-name>")
```

---

## Summary of Changes
| Item | Change | Impact |
|------|--------|--------|
| Exception Handler | Restricted to app package | Actuator endpoints no longer masked |
| Dependencies | Added actuator | Health/metrics/info endpoints available |
| Profiles | Created dev/prod configs | Secure by default, debug in dev |
| Packaging | Spring Boot repackage plugin | Executable jar + standalone capability |
| Scripts | Added run & smoke-test | Automated deployment & health validation |
| Logging | Added exception logging | Better diagnostics for errors |

---

## Questions?
For questions about profiles, deployment, or security, refer to Spring Boot official docs:
- https://spring.io/guides/gs/actuator-service/
- https://spring.io/guides/tutorials/spring-boot-for-beginners/

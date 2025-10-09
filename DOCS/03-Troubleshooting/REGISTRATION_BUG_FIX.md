# Registration Bug Fix - NullPointerException Solution

## Problem Description

The user registration endpoint was failing with the error message "Registration failed: An unexpected error occurred". Upon investigation, the root cause was identified as a `NullPointerException` in the `AuthServlet.handleRegister()` method.

## Root Cause Analysis

### Error Details
- **Error Type**: `java.lang.NullPointerException`
- **Error Message**: "Cannot invoke 'String.equals(Object)' because the return value of 'java.lang.System.getenv(String)' is null"
- **Location**: `AuthServlet.java` line 127
- **Cause**: The code was calling `.equals()` on a null environment variable without null checking

### Problematic Code
```java
// BEFORE (Buggy code)
String secretCode = requestData.get("secretCode");
if (System.getenv("ADMIN_SECRET").equals(secretCode)) {
    role = "ADMIN";
}
```

### Issue Explanation
1. `System.getenv("ADMIN_SECRET")` returns `null` when the environment variable is not set
2. Calling `.equals(secretCode)` on a `null` value throws `NullPointerException`
3. The exception was caught by the general exception handler, resulting in the generic error message

## Solution Implemented

### Fixed Code
```java
// AFTER (Fixed code)
String secretCode = requestData.get("secretCode");
String adminSecret = System.getenv("ADMIN_SECRET");
if (adminSecret != null && adminSecret.equals(secretCode)) {
    role = "ADMIN";
}
```

### Fix Details
1. **Null Check Added**: Check if `adminSecret` is not null before calling `.equals()`
2. **Safe Comparison**: Use the null-safe pattern `adminSecret != null && adminSecret.equals(secretCode)`
3. **Graceful Handling**: If `ADMIN_SECRET` is not set, the user gets the default "USER" role

## Testing Results

### Before Fix
- Registration requests failed with "Registration failed: An unexpected error occurred"
- Server logs showed `NullPointerException` stack traces

### After Fix
- ✅ Registration works successfully
- ✅ Login works successfully
- ✅ No more NullPointerException errors
- ✅ Users are created with "USER" role by default
- ✅ Admin role assignment works when `ADMIN_SECRET` environment variable is properly set

## Test Examples

### Successful Registration
```bash
curl -X POST -H "Content-Type: application/json" \
  -H "ngrok-skip-browser-warning: true" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123"}' \
  http://localhost:8083/api/auth/register
```

**Response:**
```json
{
  "message": "Registration successful",
  "user": {
    "role": "USER",
    "username": "testuser",
    "email": "test@example.com"
  }
}
```

### Successful Login
```bash
curl -X POST -H "Content-Type: application/json" \
  -H "ngrok-skip-browser-warning: true" \
  -d '{"username":"testuser","password":"password123"}' \
  http://localhost:8083/api/auth/login
```

**Response:**
```json
{
  "message": "Login successful",
  "user": {
    "role": "USER",
    "username": "testuser",
    "email": "test@example.com"
  },
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

## Dependencies Issue Resolution

During troubleshooting, we also discovered that the server was initially running without proper Maven dependencies in the classpath, causing `ClassNotFoundException` for the PostgreSQL JDBC driver.

### Solution
1. **Proper Build Process**: Used `mvn clean compile package` to create a shaded JAR with all dependencies
2. **Correct Startup**: Run the server using `java -jar target/school-management.jar 8083`
3. **Dependency Verification**: Confirmed "Database connection successful!" message appears on startup

## Prevention Measures

### Code Review Guidelines
1. **Always null-check environment variables** before using them
2. **Use defensive programming** patterns for external dependencies
3. **Implement proper error logging** to identify root causes quickly
4. **Test with missing environment variables** to ensure graceful degradation

### Recommended Pattern for Environment Variables
```java
// Recommended pattern
String envValue = System.getenv("ENV_VAR_NAME");
if (envValue != null && envValue.equals(expectedValue)) {
    // Handle case when environment variable matches
} else {
    // Handle default case or missing environment variable
}
```

## Files Modified

- **File**: `src/main/java/com/canalprep/auth/servlets/AuthServlet.java`
- **Lines**: 126-129
- **Change Type**: Bug fix - Added null safety check

## Status

✅ **RESOLVED** - Registration and authentication endpoints are now working correctly.

The fix ensures that:
- User registration works without requiring the `ADMIN_SECRET` environment variable
- Admin role assignment works when the environment variable is properly configured
- No more NullPointerException errors occur during registration
- The application gracefully handles missing environment variables
# Role-Based Access Control Documentation

## Overview

The system implements comprehensive role-based access control with enhanced privileges for developers and standalone OTP endpoints for different user roles.

## Implementation Details

### Modified Files
- **File**: `src/main/java/com/canalprep/auth/filter/AuthenticationFilter.java`
- **File**: `src/main/java/com/canalprep/otp/servlet/StandaloneOTPServlet.java` (NEW)
- **File**: `src/main/java/com/canalprep/main/MainApp.java`

### Code Changes

Updated the role check in the `AuthenticationFilter.doFilter()` method:

```java
// Check admin privileges for admin endpoints (allow both ADMIN and DEVELOPER)
if (path.startsWith("/api/admin/") && !"ADMIN".equals(claims.get("role")) && !"DEVELOPER".equals(claims.get("role"))) {
    sendErrorResponse(httpResponse, "Admin or Developer privileges required", HttpServletResponse.SC_FORBIDDEN);
    return;
}
```

## Endpoint Categories

### License Management Endpoints
1. **GET** `/api/protected/licence` - Get current license information
2. **POST** `/api/protected/licence/renew` - Renew license with OTP
3. **DELETE** `/api/protected/licence/remove` - Remove license with OTP

### Standalone OTP Endpoints
1. **POST** `/api/protected/otp/developer` - Generate OTP for developer operations
2. **POST** `/api/protected/otp/admin` - Generate OTP for admin operations

### Admin Endpoints
- All endpoints under `/api/admin/*`

### Protected Endpoints
- All other endpoints under `/api/protected/*`

## Access Control Matrix

| User Role | License Endpoints | Admin Endpoints | OTP Developer | OTP Admin | Protected Endpoints |
|-----------|------------------|-----------------|---------------|-----------|---------------------|
| USER      | ❌ Forbidden (403) | ❌ Forbidden (403) | ❌ Forbidden (403) | ❌ Forbidden (403) | ✅ Allowed |
| ADMIN     | ❌ Forbidden (403) | ✅ Allowed | ❌ Forbidden (403) | ✅ Allowed | ✅ Allowed |
| DEVELOPER | ✅ Allowed | ✅ Allowed | ✅ Allowed | ✅ Allowed | ✅ Allowed |

## Error Responses

### Admin Endpoint Access Denied
**HTTP Status**: `403 Forbidden`
**Response Body**:
```json
{
  "status": "error",
  "message": "Admin or Developer privileges required"
}
```

### OTP Endpoint Access Denied
**HTTP Status**: `403 Forbidden`
**Response Body**:
```json
{
  "success": false,
  "error": "Access denied. Only developer users can request developer OTPs."
}
```

### License Endpoint Access Denied (from servlet level)
**HTTP Status**: `403 Forbidden`
**Response Body**:
```json
{
  "success": false,
  "error": "Access denied. Only developer users can renew licenses."
}
```

## OTP Operations by Role

### Developer OTP Operations
Accessible only by DEVELOPER role:
- `RENEW` - License renewal operations
- `REMOVE` - License removal operations  
- `SYSTEM_CONFIG` - System configuration changes

### Admin OTP Operations
Accessible by ADMIN and DEVELOPER roles:
- `USER_MANAGEMENT` - User account operations
- `SYSTEM_RESET` - System reset operations
- `DATABASE_BACKUP` - Database backup operations

## Testing the Access Control

### 1. Create Test Users

Create users with different roles for testing:

```sql
-- Developer user (should have access to everything)
INSERT INTO users (username, email, password_hash, role) 
VALUES ('developer', 'developer@example.com', 'hashed_password', 'DEVELOPER');

-- Regular user (should only access protected endpoints)
INSERT INTO users (username, email, password_hash, role) 
VALUES ('testuser', 'test@example.com', 'hashed_password', 'USER');

-- Admin user (should access admin and protected endpoints)
INSERT INTO users (username, email, password_hash, role) 
VALUES ('admin', 'admin@example.com', 'hashed_password', 'ADMIN');
```

### 2. Test Developer Access (Full Access)

#### Login as Developer
```bash
curl -X POST -H "Content-Type: application/json" \
  -d '{"username":"developer","password":"dev123"}' \
  http://localhost:8083/api/auth/login
```

#### Test All Endpoints (Should All Succeed)
```bash
# License endpoint
curl -H "Authorization: Bearer DEVELOPER_JWT_TOKEN" \
  http://localhost:8083/api/protected/licence

# Admin endpoint
curl -H "Authorization: Bearer DEVELOPER_JWT_TOKEN" \
  http://localhost:8083/api/admin/users

# Developer OTP
curl -X POST -H "Authorization: Bearer DEVELOPER_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"operation":"RENEW"}' \
  http://localhost:8083/api/protected/otp/developer

# Admin OTP
curl -X POST -H "Authorization: Bearer DEVELOPER_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"operation":"USER_MANAGEMENT"}' \
  http://localhost:8083/api/protected/otp/admin
```

### 3. Test Admin Access (Limited)

#### Login as Admin
```bash
curl -X POST -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  http://localhost:8083/api/auth/login
```

#### Test Endpoints
```bash
# Admin endpoint (Should Succeed)
curl -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  http://localhost:8083/api/admin/users

# Admin OTP (Should Succeed)
curl -X POST -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"operation":"USER_MANAGEMENT"}' \
  http://localhost:8083/api/protected/otp/admin

# License endpoint (Should Fail)
curl -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  http://localhost:8083/api/protected/licence

# Developer OTP (Should Fail)
curl -X POST -H "Authorization: Bearer ADMIN_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"operation":"RENEW"}' \
  http://localhost:8083/api/protected/otp/developer
```

### 4. Test User Access (Most Limited)

#### Login as User
```bash
curl -X POST -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test123"}' \
  http://localhost:8083/api/auth/login
```

#### Test Endpoints (Most Should Fail)
```bash
# Protected endpoint (Should Succeed)
curl -H "Authorization: Bearer USER_JWT_TOKEN" \
  http://localhost:8083/api/protected/students

# License endpoint (Should Fail)
curl -H "Authorization: Bearer USER_JWT_TOKEN" \
  http://localhost:8083/api/protected/licence

# Admin endpoint (Should Fail)
curl -H "Authorization: Bearer USER_JWT_TOKEN" \
  http://localhost:8083/api/admin/users

# Any OTP endpoint (Should Fail)
curl -X POST -H "Authorization: Bearer USER_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"operation":"RENEW"}' \
  http://localhost:8083/api/protected/otp/developer
```

## Security Benefits

1. **Enhanced Developer Privileges**: Developers have full system access for development and maintenance
2. **Granular OTP Control**: Separate OTP endpoints for different operation types
3. **Role-Based Separation**: Clear separation between admin and developer operations
4. **Audit Trail**: All operations are logged with user identification
5. **Principle of Least Privilege**: Users only get access to what they need

## Integration with Existing Security

This enhanced access control works alongside existing security measures:

- **JWT Authentication**: Users must still have valid JWT tokens
- **License Validation**: The system still validates license status for all protected endpoints
- **Enhanced Role Checks**: Admin endpoints now allow both ADMIN and DEVELOPER roles
- **CORS Protection**: OPTIONS requests are still handled appropriately
- **Servlet-Level Security**: Additional role checks in individual servlets

## Filter Execution Order

1. **CORS Check**: OPTIONS requests bypass authentication
2. **JWT Validation**: Token must be valid and not expired
3. **License Validation**: System license must be active
4. **Enhanced Admin Role Check**: Admin endpoints require ADMIN or DEVELOPER role *(UPDATED)*
5. **Servlet-Level Checks**: Individual servlets perform additional role validation
6. **Request Processing**: If all checks pass, request proceeds

## Troubleshooting

### Common Issues

1. **403 Forbidden for Admin Endpoints**
   - **Cause**: User does not have ADMIN or DEVELOPER role
   - **Solution**: Ensure user account has appropriate role assigned

2. **403 Forbidden for OTP Endpoints**
   - **Cause**: User role doesn't match endpoint requirements
   - **Solution**: Use correct OTP endpoint for user role (developer vs admin)

3. **403 Forbidden for License Endpoints**
   - **Cause**: User does not have DEVELOPER role
   - **Solution**: Only DEVELOPER role can access license management

4. **Filter Not Working**
   - **Cause**: Filter may not be properly registered
   - **Solution**: Verify `@WebFilter` annotation includes correct URL patterns

5. **Role Not Found in JWT**
   - **Cause**: JWT token doesn't contain role claim
   - **Solution**: Ensure JWT generation includes role information

### Debugging

To debug role-based access issues:

1. Check application logs for authentication filter messages
2. Verify JWT token contains correct role claim:
   ```bash
   # Decode JWT token (header.payload.signature)
   echo "JWT_PAYLOAD" | base64 -d
   ```
3. Confirm user role in database:
   ```sql
   SELECT username, role FROM users WHERE username = 'your_username';
   ```

## Future Enhancements

Possible improvements to the role-based access control:

1. **Permission-Based Access**: Move from role-based to permission-based access control
2. **Time-based Access**: Restrict sensitive operations to specific time windows
3. **IP-based Restrictions**: Additional IP address validation for critical operations
4. **Multi-Factor Authentication**: Require additional authentication for sensitive operations
5. **Operation Logging**: Enhanced logging for all role-based access decisions

---

**Implementation Date**: January 2024  
**Last Updated**: January 2024  
**Status**: Active  
**Security Level**: High Priority
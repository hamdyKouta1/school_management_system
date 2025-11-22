# Auth Refresh Endpoint

Provides a secure refresh token rotation flow that issues a new refresh token, invalidates the old one, and enforces rate limiting, HTTPS, and audit logging.

## URL

- `POST /api/auth/refresh`

## Request

- Headers:
  - `Content-Type: application/json`
  - Optional `X-Forwarded-Proto: https` when behind a proxy
  - Optional `User-Agent`

- Body:
  ```json
  {
    "refreshToken": "<current_refresh_token>",
    "verification": {
      "deviceId": "optional-device-id"
    }
  }
  ```

## Workflow

- Validate refresh token signature and type.
- Verify token is not expired and not revoked in storage.
- Optional verification: match `deviceId` claim if provided.
- Generate a new refresh token with updated expiration.
- Revoke the old token by `jti`.
- Return the new refresh token and expiration.

## Responses

### 200 OK
```json
{
  "status": "success",
  "message": "Refresh token issued",
  "refreshToken": "<new_refresh_token>",
  "expiresAt": 1739999999999
}
```

### 400 Bad Request
```json
{ "status": "error", "message": "Refresh token is required" }
```
or
```json
{ "status": "error", "message": "HTTPS is required for this endpoint" }
```

### 401 Unauthorized
```json
{ "status": "error", "message": "Invalid or expired refresh token" }
```
or
```json
{ "status": "error", "message": "Refresh token has expired" }
```
or
```json
{ "status": "error", "message": "Refresh token is revoked" }
```
or
```json
{ "status": "error", "message": "Refresh token not found" }
```

### 403 Forbidden
```json
{ "status": "error", "message": "Security verification failed" }
```

### 429 Too Many Requests
```json
{ "status": "error", "message": "Too many requests" }
```

### 500 Internal Server Error
```json
{ "status": "error", "message": "An error occurred while refreshing the token" }
```

## Security

- Enforce HTTPS via `security.enforce_https=true` in properties.
- Rate limit per IP (`rate.refresh.max_requests`, `rate.refresh.window_ms`).
- Log all security events: rate limiting, invalid tokens, successful rotations.
- Persist refresh tokens with `jti` and revoke old ones to prevent reuse.

## Usage Examples

### Curl
```bash
curl -X POST http://localhost:8081/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "<current_refresh_token>",
    "verification": { "deviceId": "laptop-123" }
  }'
```

### JavaScript (fetch)
```js
await fetch("/api/auth/refresh", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({
    refreshToken: currentRefreshToken,
    verification: { deviceId: myDeviceId }
  })
});
```

## Configuration

- `jwt.refresh_expiration_ms` (optional; defaults to `jwt.expiration_ms`)
- `security.enforce_https=true|false`
- `rate.refresh.max_requests` (default: 10)
- `rate.refresh.window_ms` (default: 300000)
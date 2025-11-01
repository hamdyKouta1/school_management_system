# Postman Collection - Automatic Token Management Guide

## 🔧 **Fixed Issues**

The following issues have been resolved in the Postman collection:

### ✅ **1. Hardcoded Tokens Removed**
- **Register Request**: Replaced hardcoded JWT token with `{{token}}` variable
- **Create Student Request**: Replaced hardcoded JWT token with `{{token}}` variable

### ✅ **2. Duplicate Authorization Fixed**
- **Create Student Request**: Removed duplicate Authorization header (kept auth object)
- This prevents conflicts between auth object and header-based authentication

### ✅ **3. Enhanced Login Script**
- Improved error handling and validation
- Better console logging for debugging
- More robust token extraction and storage

### ✅ **4. Debug Support Added**
- **Get All Students Request**: Added pre-request script for token debugging
- Console messages help identify token issues

## 🚀 **How to Use Automatic Token Management**

### **Step 1: Import the Collection**
1. Open Postman
2. Click **Import** → **File** → Select the updated collection JSON
3. The collection will include all fixed requests

### **Step 2: Login to Get Token**
1. Open the **Authentication** folder
2. Run the **Login** request
3. Check the console for confirmation:
   ```
   ✅ Token saved to collection variables: eyJhbGciOiJIUzUxMiJ9...
   ```

### **Step 3: Use Protected Endpoints**
1. All protected requests now automatically use `{{token}}`
2. No need to manually copy/paste tokens
3. Token is shared across all requests in the collection

## 🔍 **Debugging Token Issues**

### **Check Console Messages**
When running requests, check the Postman console for:
- ✅ **Success**: "Token saved to collection variables"
- ❌ **Error**: "No token found. Please run the Login request first"

### **Verify Token Variable**
1. Go to collection **Variables** tab
2. Check that `token` variable has a value
3. The value should start with `eyJ...`

### **Common Issues & Solutions**

| Issue | Solution |
|-------|----------|
| "401 Unauthorized" | Run Login request first to get fresh token |
| "No token found" | Check if Login request completed successfully |
| "Token expired" | Run Login request again to get new token |
| Mixed auth methods | Use either auth object OR Authorization header, not both |

## 📋 **Request Types & Authentication**

### **Public Endpoints (No Token Required)**
- `POST /api/auth/login`
- `POST /api/auth/forget_password`
- `POST /api/auth/verify_reset_otp`
- `GET /api/attendance/date/{date}`

### **Protected Endpoints (Token Required)**
- All `/api/protected/*` endpoints
- User management endpoints
- Student CRUD operations
- License management
- Dashboard endpoints

## 🔄 **Token Workflow**

```mermaid
graph TD
    A[Run Login Request] --> B[Token Saved to {{token}} Variable]
    B --> C[All Protected Requests Use {{token}} Automatically]
    C --> D[Token Expires?]
    D -->|Yes| A
    D -->|No| E[Continue Using Protected Endpoints]
```

## ⚙️ **Collection Variables**

| Variable | Description | Example Value |
|----------|-------------|---------------|
| `base_url` | API server URL | `http://localhost:8081` |
| `token` | JWT authentication token | `eyJhbGciOiJIUzUxMiJ9...` |

## 🛠️ **Advanced Configuration**

### **Environment-Specific URLs**
Create Postman environments for different deployments:

**Development Environment:**
```json
{
  "base_url": "http://localhost:8081"
}
```

**Production Environment:**
```json
{
  "base_url": "https://your-production-api.com"
}
```

### **Custom Token Handling**
For advanced use cases, you can modify the Login test script:

```javascript
// Custom token storage
if (jsonData.token) {
    pm.collectionVariables.set("token", jsonData.token);
    pm.environment.set("auth_token", jsonData.token); // Also save to environment
    
    // Set expiration reminder
    const expiryTime = new Date(Date.now() + 30 * 60 * 1000); // 30 minutes
    console.log("Token expires at:", expiryTime.toLocaleTimeString());
}
```

## 🔐 **Security Best Practices**

### ✅ **Do:**
- Use collection variables for token storage
- Run Login request when token expires
- Keep tokens secure and don't share them
- Use HTTPS in production environments

### ❌ **Don't:**
- Hardcode tokens in requests
- Share collections with embedded tokens
- Use expired tokens
- Mix auth object and Authorization headers

## 📞 **Support**

If you encounter issues:

1. **Check Console**: Look for error messages in Postman console
2. **Verify Login**: Ensure Login request returns 200 status
3. **Check Variables**: Verify `{{token}}` variable is populated
4. **Test Manually**: Try copying token manually to verify API works

## 🎯 **Quick Test Checklist**

- [ ] Import updated collection
- [ ] Run Login request
- [ ] Check console for "Token saved" message
- [ ] Run "Get All Students" request
- [ ] Verify 200 response (not 401)
- [ ] Check that other protected endpoints work

---

**Collection Version**: Updated with automatic token management
**Last Updated**: November 2024
**Compatible With**: Postman v10+
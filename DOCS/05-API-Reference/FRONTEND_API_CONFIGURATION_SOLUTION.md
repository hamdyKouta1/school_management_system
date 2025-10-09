# Frontend API Configuration Solution

## Problem Description

The user encountered an ngrok error page when trying to access the "GET student" endpoint from their frontend running on `http://localhost:5173/`. This issue occurred because:

1. **Frontend-Backend Port Mismatch**: The frontend was running on port 5173 while the backend was on port 8081
2. **Relative API URLs**: The JavaScript files were using relative URLs (e.g., `/api/students`) which caused requests to go to the frontend server instead of the backend
3. **Missing API Configuration**: No centralized configuration for API endpoints

## Root Cause Analysis

The frontend JavaScript files were making API calls using relative paths:
- `fetch('/api/students')` in student.js
- `fetch('/api/attendance')` in attendance.js
- `fetch('/api/students/')` in home.html

When the frontend runs on `localhost:5173`, these relative calls attempt to reach `localhost:5173/api/*` instead of the backend at `localhost:8081/api/*`.

## Solution Implemented

### 1. Created Centralized API Configuration

Created `config.js` with:
- Base URL configuration pointing to the correct backend port
- Endpoint definitions for all API routes
- Helper functions for authenticated requests
- JWT token management utilities

### 2. Updated All Frontend Files

#### Files Modified:
- `js/config.js` - New centralized API configuration
- `js/student.js` - Updated to use config.js functions
- `js/attendance.js` - Updated to use config.js functions
- `api/home.html` - Updated to use config.js functions
- `js/app.js` - Updated to load config.js for dynamic pages
- `index2.html` - Added config.js script reference

#### Key Changes:
- Replaced `fetch('/api/students')` with `makeAuthenticatedRequest(getApiUrl(API_CONFIG.ENDPOINTS.STUDENTS))`
- Added proper JWT token handling for authenticated requests
- Centralized all API endpoint definitions

### 3. Configuration Details

```javascript
const API_CONFIG = {
    BASE_URL: 'http://localhost:8081',
    ENDPOINTS: {
        STUDENTS: '/api/students',
        STUDENTS_FULL: '/api/protected/insertStudent',
        ATTENDANCE: '/api/attendance',
        DASHBOARD: '/api/protected/dashboard'
    }
};
```

## How to Use for Different Environments

### For Local Development
- Backend on `localhost:8081` (current configuration)
- Frontend on any port (5173, 3000, etc.)
- No changes needed - configuration is already set

### For ngrok Tunneling
1. Start your backend server on port 8081
2. Create ngrok tunnel: `ngrok http 8081`
3. Update `config.js`:
   ```javascript
   const API_CONFIG = {
       BASE_URL: 'https://your-ngrok-url.ngrok-free.app',
       // ... rest of configuration
   };
   ```

### For Production Deployment
1. Update `config.js` with production backend URL:
   ```javascript
   const API_CONFIG = {
       BASE_URL: 'https://your-production-domain.com',
       // ... rest of configuration
   };
   ```

## Testing the Solution

### 1. Verify Backend is Running
```bash
netstat -an | findstr :8081
```
Should show the backend listening on port 8081.

### 2. Test API Endpoints
Use the provided test scripts:
- `test-student-endpoint.bat` (Windows)
- `test-student-endpoint.sh` (Linux/Mac)

### 3. Check Frontend Configuration
1. Open browser developer tools (F12)
2. Go to Network tab
3. Perform actions in the frontend
4. Verify API calls are going to `localhost:8081` instead of `localhost:5173`

## Additional Benefits

### 1. JWT Token Management
The solution includes proper JWT token handling:
- Automatic token inclusion in requests
- Token storage in localStorage
- Helper functions for token management

### 2. Error Handling
Improved error handling for:
- Network connectivity issues
- Authentication failures
- API response errors

### 3. Maintainability
- Single point of configuration for all API settings
- Easy environment switching
- Consistent request handling across all frontend files

## Troubleshooting

### If Still Getting ngrok Errors:
1. **Check Backend Status**: Ensure backend is running on port 8081
2. **Verify Configuration**: Check that `config.js` has the correct BASE_URL
3. **Clear Browser Cache**: Hard refresh (Ctrl+F5) to ensure new scripts are loaded
4. **Check Console**: Look for JavaScript errors in browser developer tools

### Common Issues:
- **CORS Errors**: Backend already has CORS configured for all origins
- **Authentication Errors**: Ensure JWT token is properly set in localStorage
- **Port Conflicts**: Verify no other services are using port 8081

## Files Created/Modified

### New Files:
- `js/config.js` - Centralized API configuration
- `FRONTEND_API_CONFIGURATION_SOLUTION.md` - This documentation

### Modified Files:
- `js/student.js` - Updated API calls
- `js/attendance.js` - Updated API calls
- `api/home.html` - Updated API calls
- `js/app.js` - Added config.js loading
- `index2.html` - Added config.js script reference

This solution resolves the ngrok connectivity issue and provides a robust, maintainable approach to frontend-backend communication across different deployment environments.
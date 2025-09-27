# Authentication Solution for Remote Frontend

## Problem Analysis

You're receiving the error:
```json
{
  "status": "error",
  "message": "Authorization header missing"
}
```

This error occurs because **all student API endpoints require authentication**. The StudentServlet is mapped to `/api/protected/students/*`, which means the AuthenticationFilter requires a valid JWT token in the Authorization header.

## Authentication Flow Required

### Step 1: Login to Get JWT Token

First, your remote frontend must authenticate to get a JWT token:

**Endpoint:** `POST https://4f8ed4c66c10.ngrok-free.app/api/auth/login`

**Request Headers:**
```javascript
{
  'Content-Type': 'application/json',
  'ngrok-skip-browser-warning': 'true'
}
```

**Request Body:**
```javascript
{
  "username": "your_username",
  "password": "your_password"
}
```

**Response (Success):**
```javascript
{
  "status": "success",
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "username": "your_username",
    "email": "user@example.com",
    "role": "USER"
  }
}
```

### Step 2: Use JWT Token for API Requests

After getting the token, include it in all subsequent API requests:

**Endpoint:** `GET https://4f8ed4c66c10.ngrok-free.app/api/protected/students`

**Request Headers:**
```javascript
{
  'Content-Type': 'application/json',
  'Authorization': 'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
  'ngrok-skip-browser-warning': 'true'
}
```

## Implementation Examples

### JavaScript/Fetch Implementation

```javascript
class SchoolAPI {
  constructor(baseUrl) {
    this.baseUrl = baseUrl;
    this.token = localStorage.getItem('jwt_token');
  }

  // Login and store token
  async login(username, password) {
    try {
      const response = await fetch(`${this.baseUrl}/api/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'ngrok-skip-browser-warning': 'true'
        },
        body: JSON.stringify({ username, password })
      });

      const data = await response.json();
      
      if (data.status === 'success') {
        this.token = data.token;
        localStorage.setItem('jwt_token', this.token);
        return data;
      } else {
        throw new Error(data.message);
      }
    } catch (error) {
      console.error('Login failed:', error);
      throw error;
    }
  }

  // Get all students
  async getStudents() {
    if (!this.token) {
      throw new Error('Not authenticated. Please login first.');
    }

    try {
      const response = await fetch(`${this.baseUrl}/api/protected/students`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${this.token}`,
          'ngrok-skip-browser-warning': 'true'
        }
      });

      if (response.status === 401) {
        // Token expired or invalid
        this.token = null;
        localStorage.removeItem('jwt_token');
        throw new Error('Authentication expired. Please login again.');
      }

      const data = await response.json();
      return data;
    } catch (error) {
      console.error('Failed to fetch students:', error);
      throw error;
    }
  }

  // Generic authenticated request method
  async makeAuthenticatedRequest(endpoint, options = {}) {
    if (!this.token) {
      throw new Error('Not authenticated. Please login first.');
    }

    const defaultOptions = {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.token}`,
        'ngrok-skip-browser-warning': 'true'
      }
    };

    const mergedOptions = {
      ...defaultOptions,
      ...options,
      headers: {
        ...defaultOptions.headers,
        ...options.headers
      }
    };

    try {
      const response = await fetch(`${this.baseUrl}${endpoint}`, mergedOptions);
      
      if (response.status === 401) {
        this.token = null;
        localStorage.removeItem('jwt_token');
        throw new Error('Authentication expired. Please login again.');
      }

      return response;
    } catch (error) {
      console.error('API request failed:', error);
      throw error;
    }
  }
}

// Usage example
const api = new SchoolAPI('https://4f8ed4c66c10.ngrok-free.app');

// Login first
api.login('your_username', 'your_password')
  .then(() => {
    console.log('Login successful');
    // Now you can make authenticated requests
    return api.getStudents();
  })
  .then(students => {
    console.log('Students:', students);
  })
  .catch(error => {
    console.error('Error:', error);
  });
```

### Axios Implementation

```javascript
import axios from 'axios';

class SchoolAPIAxios {
  constructor(baseUrl) {
    this.baseUrl = baseUrl;
    this.token = localStorage.getItem('jwt_token');
    
    // Create axios instance with default config
    this.api = axios.create({
      baseURL: baseUrl,
      headers: {
        'Content-Type': 'application/json',
        'ngrok-skip-browser-warning': 'true'
      }
    });

    // Add request interceptor to include token
    this.api.interceptors.request.use((config) => {
      if (this.token) {
        config.headers.Authorization = `Bearer ${this.token}`;
      }
      return config;
    });

    // Add response interceptor to handle auth errors
    this.api.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          this.token = null;
          localStorage.removeItem('jwt_token');
          // Optionally redirect to login
        }
        return Promise.reject(error);
      }
    );
  }

  async login(username, password) {
    try {
      const response = await this.api.post('/api/auth/login', {
        username,
        password
      });

      if (response.data.status === 'success') {
        this.token = response.data.token;
        localStorage.setItem('jwt_token', this.token);
        return response.data;
      }
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Login failed');
    }
  }

  async getStudents() {
    const response = await this.api.get('/api/protected/students');
    return response.data;
  }
}
```

## Available API Endpoints

### Authentication Endpoints (No token required)
- `POST /api/auth/login` - Login and get JWT token
- `POST /api/auth/register` - Register new user
- `POST /api/auth/logout` - Logout (client-side token removal)

### Protected Endpoints (Require JWT token)
- `GET /api/protected/students` - Get all students
- `GET /api/protected/students/{id}` - Get student by ID
- `POST /api/protected/students` - Create new student
- `PUT /api/protected/students/{id}` - Update student
- `DELETE /api/protected/students/{id}` - Delete student
- `GET /api/protected/attendance` - Get attendance records
- `POST /api/protected/attendance` - Create attendance record
- And more...

## User Account Setup

If you don't have a user account, you can:

1. **Register a new account:**
   ```javascript
   fetch('https://4f8ed4c66c10.ngrok-free.app/api/auth/register', {
     method: 'POST',
     headers: {
       'Content-Type': 'application/json',
       'ngrok-skip-browser-warning': 'true'
     },
     body: JSON.stringify({
       username: 'your_username',
       email: 'your_email@example.com',
       password: 'your_password'
     })
   })
   ```

2. **Use existing test credentials** (if available in the database)

## Troubleshooting

### Common Issues:

1. **"Authorization header missing"**
   - Solution: Include `Authorization: Bearer <token>` header
   - Make sure you've logged in first to get the token

2. **"Token expired"**
   - Solution: Login again to get a new token
   - Implement automatic token refresh logic

3. **CORS issues**
   - Solution: Ensure `ngrok-skip-browser-warning: true` header is included
   - Check that the backend CORS configuration allows your origin

4. **401 Unauthorized after login**
   - Check that the token is being stored and sent correctly
   - Verify the token format: `Bearer <actual_token>`

## Security Notes

- Store JWT tokens securely (localStorage, sessionStorage, or secure cookies)
- Implement token expiration handling
- Never expose tokens in URLs or logs
- Use HTTPS in production (ngrok provides this)
- Consider implementing refresh tokens for better security

## Testing with Postman

1. **Login Request:**
   - Method: POST
   - URL: `https://4f8ed4c66c10.ngrok-free.app/api/auth/login`
   - Headers: `ngrok-skip-browser-warning: true`
   - Body: `{"username": "test", "password": "test"}`

2. **Copy the token from the response**

3. **Student Request:**
   - Method: GET
   - URL: `https://4f8ed4c66c10.ngrok-free.app/api/protected/students`
   - Headers: 
     - `Authorization: Bearer <your_token>`
     - `ngrok-skip-browser-warning: true`

This should resolve your authentication issues and allow your remote frontend to successfully communicate with the ngrok-published backend!
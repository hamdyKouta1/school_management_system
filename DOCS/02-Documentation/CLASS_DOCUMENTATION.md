# School Management System - Class Documentation

## Overview

This document provides comprehensive documentation for all classes in the School Management System, including their purpose, methods, parameters, and return values.

## Table of Contents

1. [Main Application](#main-application)
2. [Authentication Package](#authentication-package)
3. [Data Access Objects (DAO)](#data-access-objects-dao)
4. [Model Classes](#model-classes)
5. [Service Classes](#service-classes)
6. [Servlet Classes](#servlet-classes)
7. [License Management](#license-management)
8. [Utility Classes](#utility-classes)
9. [Exception Classes](#exception-classes)
10. [Debug Classes](#debug-classes)

---

## Main Application

### MainApp
**Package:** `com.canalprep.main`  
**File:** `MainApp.java`

**Purpose:** Entry point of the application. Configures and starts the Jetty server with all servlets and filters.

#### Methods:

##### `main(String[] args)`
- **Purpose:** Application entry point
- **Parameters:** 
  - `args` (String[]): Command line arguments, first argument can specify port number
- **Return:** void
- **Description:** Initializes logging, validates license, configures Jetty server, registers servlets and filters, starts the server

##### `testDatabaseConnection()`
- **Purpose:** Tests database connectivity during startup
- **Parameters:** None
- **Return:** void
- **Description:** Attempts to establish database connection and logs result. Exits application if connection fails.

##### `initializeLogging()`
- **Purpose:** Initializes the logging system
- **Parameters:** None
- **Return:** void
- **Description:** Loads logging configuration from resources and initializes LoggerUtil

**Key Features:**
- Runs on port 8081 by default (configurable via command line)
- Binds to all network interfaces (0.0.0.0)
- Configures CORS for frontend access
- Sets up authentication filters
- Registers all API endpoints
- Implements graceful shutdown
- Starts license scheduler

---

## Authentication Package

### User
**Package:** `com.canalprep.auth.model`  
**File:** `User.java`

**Purpose:** Model class representing a user in the system.

#### Properties:
- `id` (int): User ID
- `username` (String): Username
- `email` (String): Email address
- `passwordHash` (String): Hashed password
- `salt` (String): Password salt
- `createdAt` (Timestamp): Creation timestamp
- `role` (String): User role (USER, ADMIN, DEVELOPER)

#### Methods:

##### Constructors
- `User()`: Default constructor
- `User(int id, String username, String email, String passwordHash, String salt, Timestamp createdAt, String role)`: Full constructor

##### Getters and Setters
- Standard getter/setter methods for all properties

### UserDAO
**Package:** `com.canalprep.auth.dao`  
**File:** `UserDAO.java`

**Purpose:** Data Access Object for user-related database operations.

#### Methods:

##### `getUserByUsername(String username)`
- **Purpose:** Retrieve user by username
- **Parameters:** 
  - `username` (String): Username to search for
- **Return:** `User` object or null if not found
- **Throws:** `DataAccessException`

##### `getUserByEmail(String email)`
- **Purpose:** Retrieve user by email
- **Parameters:** 
  - `email` (String): Email to search for
- **Return:** `User` object or null if not found
- **Throws:** `DataAccessException`

##### `createUser(User user)`
- **Purpose:** Create a new user
- **Parameters:** 
  - `user` (User): User object to create
- **Return:** boolean indicating success
- **Throws:** `DataAccessException`

##### `updatePassword(String username, String newPasswordHash, String newSalt)`
- **Purpose:** Update user password
- **Parameters:** 
  - `username` (String): Username
  - `newPasswordHash` (String): New hashed password
  - `newSalt` (String): New salt
- **Return:** boolean indicating success
- **Throws:** `DataAccessException`

### AuthServlet
**Package:** `com.canalprep.auth.servlets`  
**File:** `AuthServlet.java`

**Purpose:** Handles authentication-related HTTP requests including login, registration, and password recovery.

#### Endpoints:
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/forget_password` - Password recovery request
- `POST /api/auth/verify_reset_otp` - OTP verification for password reset

#### Methods:

##### `doPost(HttpServletRequest request, HttpServletResponse response)`
- **Purpose:** Handles POST requests for authentication
- **Parameters:** 
  - `request` (HttpServletRequest): HTTP request
  - `response` (HttpServletResponse): HTTP response
- **Return:** void
- **Description:** Routes requests based on path info to appropriate handlers

##### `handleLogin(HttpServletRequest request, HttpServletResponse response)`
- **Purpose:** Processes login requests
- **Parameters:** 
  - `request` (HttpServletRequest): HTTP request
  - `response` (HttpServletResponse): HTTP response
- **Return:** void
- **Description:** Validates credentials and returns JWT token

##### `handleRegister(HttpServletRequest request, HttpServletResponse response)`
- **Purpose:** Processes registration requests
- **Parameters:** 
  - `request` (HttpServletRequest): HTTP request
  - `response` (HttpServletResponse): HTTP response
- **Return:** void
- **Description:** Creates new user account

##### `handleForgetPassword(HttpServletRequest request, HttpServletResponse response)`
- **Purpose:** Processes password recovery requests
- **Parameters:** 
  - `request` (HttpServletRequest): HTTP request
  - `response` (HttpServletResponse): HTTP response
- **Return:** void
- **Description:** Initiates password recovery process with OTP

##### `handleVerifyResetOTP(HttpServletRequest request, HttpServletResponse response)`
- **Purpose:** Verifies OTP and resets password
- **Parameters:** 
  - `request` (HttpServletRequest): HTTP request
  - `response` (HttpServletResponse): HTTP response
- **Return:** void
- **Description:** Validates OTP and updates password

### AuthenticationFilter
**Package:** `com.canalprep.auth.filter`  
**File:** `AuthenticationFilter.java`

**Purpose:** Servlet filter that validates JWT tokens for protected endpoints.

#### Methods:

##### `doFilter(ServletRequest request, ServletResponse response, FilterChain chain)`
- **Purpose:** Filters incoming requests to validate authentication
- **Parameters:** 
  - `request` (ServletRequest): HTTP request
  - `response` (ServletResponse): HTTP response
  - `chain` (FilterChain): Filter chain
- **Return:** void
- **Description:** Validates JWT token and sets user context

### JwtUtil
**Package:** `com.canalprep.auth.utilities`  
**File:** `JwtUtil.java`

**Purpose:** Utility class for JWT token operations.

#### Methods:

##### `generateToken(User user)`
- **Purpose:** Generate JWT token for user
- **Parameters:** 
  - `user` (User): User object
- **Return:** String JWT token
- **Description:** Creates signed JWT token with user information

##### `validateToken(String token)`
- **Purpose:** Validate JWT token
- **Parameters:** 
  - `token` (String): JWT token to validate
- **Return:** Claims object if valid, null if invalid
- **Description:** Verifies token signature and expiration

##### `getUsernameFromToken(String token)`
- **Purpose:** Extract username from token
- **Parameters:** 
  - `token` (String): JWT token
- **Return:** String username
- **Description:** Extracts username claim from token

### PasswordUtils
**Package:** `com.canalprep.auth.utilities`  
**File:** `PasswordUtils.java`

**Purpose:** Utility class for password hashing and validation.

#### Methods:

##### `hashPassword(String password, String salt)`
- **Purpose:** Hash password with salt
- **Parameters:** 
  - `password` (String): Plain text password
  - `salt` (String): Salt value
- **Return:** String hashed password
- **Description:** Uses BCrypt to hash password with salt

##### `generateSalt()`
- **Purpose:** Generate random salt
- **Parameters:** None
- **Return:** String salt value
- **Description:** Generates cryptographically secure random salt

##### `verifyPassword(String password, String hash, String salt)`
- **Purpose:** Verify password against hash
- **Parameters:** 
  - `password` (String): Plain text password
  - `hash` (String): Stored hash
  - `salt` (String): Salt value
- **Return:** boolean indicating match
- **Description:** Verifies password using BCrypt

---

*This documentation continues with detailed information for all remaining classes...*

---

**Last Updated:** January 2025  
**Version:** 1.0  
**Total Classes Documented:** [To be completed]
# School Management System - Technical Documentation

## System Architecture

### Overview

The School Management System is built using a Java-based backend with a web frontend. The architecture follows a layered approach:

1. **Presentation Layer**: HTML/CSS/JavaScript frontend and RESTful API endpoints
2. **Business Logic Layer**: Java servlets and service classes
3. **Data Access Layer**: DAO (Data Access Object) classes for database operations
4. **Database Layer**: PostgreSQL database

### Component Diagram

```
+------------------+     +------------------+     +------------------+     +------------------+
|                  |     |                  |     |                  |     |                  |
|  Web Browser     |<--->|  Servlet API     |<--->|  Service Layer   |<--->|  Database        |
|  (HTML/JS/CSS)   |     |  (REST Endpoints)|     |  (Business Logic)|     |  (PostgreSQL)    |
|                  |     |                  |     |                  |     |                  |
+------------------+     +------------------+     +------------------+     +------------------+
                                    ^                      ^
                                    |                      |
                                    v                      v
                         +------------------+     +------------------+
                         |                  |     |                  |
                         |  Authentication  |     |  Data Access     |
                         |  (JWT)           |     |  (DAO Classes)   |
                         |                  |     |                  |
                         +------------------+     +------------------+
```

## Core Components

### 1. Main Application (MainApp.java)

The entry point of the application that configures and starts the embedded Jetty server. It sets up:

- **Server Configuration**: Binds to 0.0.0.0:8081 (all interfaces, port 8081)
- **Servlet Mappings**: Registers all API endpoints and servlets
- **Authentication Filter**: JWT-based authentication for protected endpoints
- **CORS Configuration**: Enables cross-origin requests for frontend integration
- **License Validation**: Automatic license checking on startup
- **Static Resource Handling**: Serves frontend assets
- **Logging**: Configures application logging with rotation

### 2. Authentication System

#### Components:

- **AuthServlet**: Handles login, registration, logout, and password recovery
- **AuthenticationFilter**: Validates JWT tokens for protected endpoints
- **JwtUtil**: Generates and validates JWT tokens
- **PasswordUtils**: Handles password hashing and verification
- **PasswordRecoveryService**: Manages password recovery flow with OTP generation and email sending
- **UserDAO**: Manages user data in the database

#### Authentication Flow:

1. User submits credentials to `/api/auth/login`
2. System validates credentials against the database
3. If valid, a JWT token is generated and returned
4. Client includes the token in subsequent requests
5. AuthenticationFilter validates the token for protected endpoints

### 3. Data Access Layer

#### Key DAO Classes:

- **StudentDAO**: Manages student records
- **AttendanceDAO**: Handles attendance records
- **MedicalHistoryDAO**: Manages student medical information
- **AdditionalQualificationDAO**: Handles student qualifications
- **UserDAO**: Manages user accounts
- **LicenseDAO**: Manages license data and validation
- **OTPDAO**: Handles OTP generation and validation

#### Database Connection:

The `DBConnection` class manages database connections with hardcoded configuration:

- **Database URL**: `jdbc:postgresql://localhost:5432/school_management_system`
- **Username**: `postgres`
- **Password**: `admin`
- **Driver**: PostgreSQL JDBC Driver (`org.postgresql.Driver`)

**Note**: Database configuration is currently hardcoded in the `DBConnection.java` file.

### 4. API Endpoints

The system exposes RESTful API endpoints through various servlet classes:

#### Authentication Endpoints:

- `POST /api/auth/login`: Authenticate user and get JWT token
- `POST /api/auth/register`: Register a new user
- `POST /api/auth/logout`: Invalidate the current session
- `POST /api/auth/forget_password`: Initiate password recovery by sending OTP to email
- `POST /api/auth/verify_reset_otp`: Verify OTP and reset user password

#### Student Management Endpoints:

- `GET /api/protected/students`: Get all students
- `GET /api/protected/students/{id}`: Get student by ID
- `POST /api/protected/students`: Create a new student
- `PUT /api/protected/students/{id}`: Update student information
- `DELETE /api/protected/students/{id}`: Delete a student

#### Attendance Management Endpoints:

- `GET /api/protected/attendance`: Get all attendance records
- `GET /api/protected/attendance/id/{id}`: Get attendance by ID
- `GET /api/protected/attendance/getByDate/{date}`: Get attendance by date
- `POST /api/protected/attendance`: Create attendance record
- `PUT /api/protected/attendance/{id}`: Update attendance record

#### License Management Endpoints:

- `GET /api/protected/licence`: Get current license information
- `POST /api/protected/licence/renew`: Renew license with OTP validation
- `DELETE /api/protected/licence/remove`: Remove/deactivate license with OTP validation

#### OTP Management Endpoints:

- `POST /api/protected/otp/developer`: Generate OTP for developer operations (DEVELOPER role only)
- `POST /api/protected/otp/admin`: Generate OTP for admin operations (ADMIN role only)

#### Additional Endpoints:

- Student notes, phone numbers, medical history, and qualifications endpoints
- Dashboard endpoints for statistics and reporting

### 5. Frontend

The frontend is built using HTML, CSS (Bootstrap), and JavaScript. Key features include:

- Responsive design for various screen sizes
- Tab-based navigation between sections
- Modal forms for data entry
- Table-based data display with search and filter capabilities
- Client-side validation for form inputs

## Implementation Details

### Database Schema

#### Main Tables:

- **students**: Core student information
  - student_id (PK)
  - student_name
  - nid (National ID)
  - nationality_id (FK)
  - current_address
  - religion_id (FK)
  - medical_status

- **attendance**: Student attendance records
  - attendance_id (PK)
  - student_id (FK)
  - attendance_date
  - status_id (FK)
  - class_id
  - grade_id
  - today_date

- **users**: Authentication and authorization
  - id (PK)
  - username
  - email
  - password_hash
  - salt
  - created_at
  - last_login
  - role

- **student_notes**: Notes about students
  - note_id (PK)
  - student_id (FK)
  - note_text
  - created_by

- **student_phone**: Student contact information
  - student_id (FK)
  - phone_number

- **medical_history**: Student health information
  - student_id (FK)
  - description

- **additional_qualifications**: Student achievements
  - qualification_id (PK)
  - student_id (FK)
  - description

- **licenses**: License management
  - license_id (PK)
  - license_key (UNIQUE)
  - start_date
  - end_date
  - status
  - created_at
  - updated_at

- **otps**: One-time password management
  - otp_id (PK)
  - otp_code
  - email
  - operation
  - created_at
  - expires_at
  - used_at
  - is_used

### Security Implementation

#### Password Security:

Passwords are secured using the following approach:

1. A unique salt is generated for each user
2. The password is hashed using BCrypt with the salt
3. Both the hash and salt are stored in the database
4. During login, the provided password is hashed with the stored salt and compared

#### JWT Implementation:

JWT tokens contain the following claims:

- `userId`: Unique identifier for the user
- `username`: User's username
- `email`: User's email address
- `role`: User's role (USER, ADMIN, or DEVELOPER)
- `iat`: Issued at timestamp

#### Role-Based Access Control:

The system implements three user roles with different access levels:

- **USER**: Basic access to protected endpoints
- **ADMIN**: Access to admin endpoints and admin OTP generation
- **DEVELOPER**: Full access including license management and developer OTP generation
- `exp`: Expiration timestamp (30 minutes from issuance)

The token is signed using HMAC-SHA256 with a secret key from the `JWT_SECRET` environment variable.

#### Security Headers:

The application sets the following security headers:

- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`
- `Strict-Transport-Security: max-age=31536000; includeSubDomains`
- `Content-Security-Policy: default-src 'self'`

### License Management

The application includes a license management system that:

1. Generates a license file (`license.dat`) on first run
2. Sets the license expiration to 365 days from installation
3. Validates the license on each application start
4. Prevents the application from running if the license is expired

## Error Handling and Logging

### Exception Hierarchy:

- **DataAccessException**: Custom exception for database-related errors
- Standard Java exceptions for other error types

### Logging Strategy:

The application uses Java's built-in logging (java.util.logging) with different log levels:

- **SEVERE**: Critical errors that prevent normal operation
- **WARNING**: Issues that don't prevent operation but require attention
- **INFO**: Informational messages about normal operation
- **FINE/FINER/FINEST**: Detailed debugging information

## Performance Considerations

### Connection Pooling:

The current implementation creates a new database connection for each request. For production use, consider implementing connection pooling using HikariCP or similar libraries.

### Caching:

Implement caching for frequently accessed data to reduce database load, especially for:

- Student lists
- Attendance records
- Dashboard statistics

### Query Optimization:

Some database operations use stored procedures for complex operations:

- `update_full_student`: Updates all student information in a single transaction
- `delete_student_and_related_data`: Deletes a student and all related records
- `get_student_details`: Retrieves complete student information including related data

## Testing

### API Testing:

Use the provided Postman collection for testing API endpoints:

1. Import the collection and environment files
2. Set the base URL to match your server
3. Run the authentication requests first to get a valid token
4. Test each endpoint with valid and invalid data

### Manual Testing:

Test the web interface by:

1. Logging in with valid credentials
2. Creating, viewing, updating, and deleting student records
3. Recording and viewing attendance
4. Testing form validation and error handling
5. Verifying dashboard statistics

## Deployment Considerations

### Production Environment:

1. Use a production-grade database server
2. Set up proper connection pooling
3. Configure secure environment variables
4. Use HTTPS with a valid SSL certificate
5. Implement proper backup and recovery procedures

### Scaling:

For larger deployments, consider:

1. Load balancing across multiple application servers
2. Database replication or clustering
3. Caching layers for frequently accessed data
4. Separate servers for static content delivery

---

This technical documentation provides an overview of the system architecture, components, and implementation details. For specific code-level documentation, refer to the comments in the source code files.
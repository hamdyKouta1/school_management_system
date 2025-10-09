# License Management System Documentation

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Database Schema](#database-schema)
4. [Core Components](#core-components)
5. [API Endpoints](#api-endpoints)
6. [Security Features](#security-features)
7. [Installation & Setup](#installation--setup)
8. [Usage Guide](#usage-guide)
9. [Troubleshooting](#troubleshooting)
10. [Migration from Old System](#migration-from-old-system)

## Overview

The School Management System now includes a comprehensive database-driven license management system that replaces the previous file-based approach. This new system provides enhanced security, better tracking, and administrative control over license operations.

### Key Features

- **Database-driven**: Licenses stored securely in PostgreSQL database
- **OTP Security**: Role-based OTP authentication for sensitive operations
- **REST API**: Complete API for license management
- **Automated Scheduling**: Daily license validation and cleanup
- **Email Notifications**: Automated alerts for license operations
- **Comprehensive Logging**: Detailed audit trail for all operations
- **Status Management**: Multiple license states (ACTIVE, EXPIRED, EXPIRING_SOON, etc.)
- **Role-based Access**: Separate OTP endpoints for DEVELOPER and ADMIN roles

## Architecture

### System Components

```
┌─────────────────────────────────────────────────────────────┐
│                    License Management System                │
├─────────────────────────────────────────────────────────────┤
│  Web Layer (Servlets)                                      │
│  ├── LicenseServlet (/api/protected/licence)               │
│  └── StandaloneOTPServlet (/api/protected/otp)             │
├─────────────────────────────────────────────────────────────┤
│  Service Layer                                              │
│  ├── LicenseService (Business Logic)                       │
│  └── OTPService (OTP Generation & Validation)              │
├─────────────────────────────────────────────────────────────┤
│  Data Access Layer                                          │
│  ├── LicenseDAO (License CRUD Operations)                  │
│  └── OTPDAO (OTP CRUD Operations)                          │
├─────────────────────────────────────────────────────────────┤
│  Model Layer                                                │
│  ├── License (License Entity)                              │
│  └── OTP (OTP Entity)                                      │
├─────────────────────────────────────────────────────────────┤
│  Scheduler                                                  │
│  └── LicenseScheduler (Daily Tasks)                        │
├─────────────────────────────────────────────────────────────┤
│  Database Layer                                             │
│  ├── licenses table                                        │
│  └── otps table                                            │
└─────────────────────────────────────────────────────────────┘
```

## Database Schema

### Licenses Table

```sql
CREATE TABLE licenses (
    id SERIAL PRIMARY KEY,
    license_key VARCHAR(255) NOT NULL UNIQUE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    remaining_days INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL,
    
    CONSTRAINT chk_license_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'EXPIRED', 'EXPIRING_SOON', 'REMOVED')),
    CONSTRAINT chk_license_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_remaining_days CHECK (remaining_days >= 0)
);
```

### OTPs Table

```sql
CREATE TABLE otps (
    id SERIAL PRIMARY KEY,
    otp_code VARCHAR(10) NOT NULL,
    email VARCHAR(255) NOT NULL,
    operation VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    ip_address VARCHAR(45),
    
    CONSTRAINT chk_otp_operation CHECK (operation IN ('RENEW', 'REMOVE')),
    CONSTRAINT chk_otp_expires CHECK (expires_at > created_at)
);
```

### Sample Data

The system includes sample 3-day test licenses:

```sql
-- 3-day test license
INSERT INTO licenses (license_key, start_date, end_date, remaining_days, status, created_by)
VALUES (
    'LIC-TEST-3DAY-001',
    CURRENT_DATE,
    CURRENT_DATE + INTERVAL '30 days',
    3,
    'ACTIVE',
    'system'
) ON CONFLICT (license_key) DO NOTHING;
```

## Core Components

### 1. License Model (`License.java`)

Represents a license entity with the following properties:

- `id`: Unique identifier
- `licenseKey`: Unique license key string
- `startDate`: License validity start date
- `endDate`: License validity end date
- `remainingDays`: Days remaining until expiration
- `status`: Current license status
- `createdAt/updatedAt`: Timestamps
- `createdBy`: User who created the license

### 2. License Service (`LicenseService.java`)

Core business logic for license management:

#### Key Methods:

- `getCurrentLicense()`: Retrieves current active license
- `isLicenseValid()`: Validates license status
- `renewLicense()`: Extends license validity
- `removeLicense()`: Deactivates license
- `performDailyLicenseCheck()`: Daily validation routine
- `createLicense()`: Creates new license

### 3. License DAO (`LicenseDAO.java`)

Data access layer for license operations:

- Database CRUD operations
- Connection management
- SQL query execution
- Result set mapping

### 4. OTP Service (`OTPService.java`)

Handles One-Time Password operations:

- `generateOTP()`: Creates 6-digit OTP
- `validateOTP()`: Verifies OTP validity
- `sendOTPEmail()`: Sends OTP via email
- `cleanupExpiredOTPs()`: Removes expired OTPs

### 5. License Scheduler (`LicenseScheduler.java`)

Automated background tasks:

- **Daily License Check**: Runs at 2:00 AM daily
- **OTP Cleanup**: Runs every 30 minutes
- **Manual Triggers**: For testing and immediate execution

## API Endpoints

### License Management Endpoints

#### 1. Get Current License

```http
GET /api/protected/licence
Authorization: Bearer <JWT_TOKEN>
```

**Response:**
```json
{
  "success": true,
  "license": {
    "id": 1,
    "licenseKey": "LIC-TEST-3DAY-001",
    "startDate": "2024-01-15",
    "endDate": "2024-01-18",
    "remainingDays": 3,
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:00:00",
    "createdBy": "system"
  }
}
```

#### 2. Renew License

```http
POST /api/protected/licence/renew
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "email": "admin@example.com",
  "otp": "123456",
  "newEndDate": "2024-12-31"
}
```

**Response:**
```json
{
  "success": true,
  "message": "License renewed successfully",
  "license": {
    "endDate": "2024-12-31",
    "remainingDays": 365,
    "status": "ACTIVE"
  }
}
```

#### 3. Remove License

```http
DELETE /api/protected/licence/remove
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "email": "admin@example.com",
  "otp": "123456"
}
```

### OTP Management Endpoints

#### 1. Generate OTP (Developer)

```http
POST /api/protected/otp/developer
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "email": "developer@example.com",
  "operation": "RENEW"
}
```

#### 2. Generate OTP (Admin)

```http
POST /api/protected/otp/admin
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "email": "admin@example.com",
  "operation": "SYSTEM_CONFIG"
}
```

**Response:**
```json
{
  "success": true,
  "message": "OTP sent to email successfully",
  "expiresIn": 600
}
```

## Security Features

### 1. JWT Authentication

- All endpoints require valid JWT token
- Token validation on each request
- Role-based access control

### 2. OTP Verification

- 6-digit random OTP generation
- 10-minute expiration time
- Single-use validation
- Email delivery confirmation

### 3. Input Validation

- SQL injection prevention
- XSS protection
- Data type validation
- Business rule enforcement

### 4. Audit Logging

- All operations logged with timestamps
- User identification tracking
- Security event monitoring
- Error tracking and reporting

## Installation & Setup

### 1. Database Setup

```bash
# Execute the schema file
psql -U postgres -d school_management -f src/main/resources/database/license_schema.sql
```

### 2. Environment Configuration

Ensure these environment variables are set:

```bash
DB_URL=jdbc:postgresql://localhost:5432/school_management
DB_USER=postgres
DB_PASSWORD=your_password
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your_email@gmail.com
SMTP_PASSWORD=your_app_password
```

### 3. Application Integration

The license system is automatically integrated into:

- **MainApp.java**: Application startup validation
- **AuthenticationFilter.java**: Request-level validation
- **LicenseScheduler**: Background task management

## Usage Guide

### For Administrators

#### 1. Check License Status

```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     http://localhost:8080/api/protected/licence
```

#### 2. Renew License Process

1. Generate OTP (for developers):
```bash
curl -X POST -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"email":"developer@example.com","operation":"RENEW"}' \
     http://localhost:8080/api/protected/otp/developer
```

Or for admin operations:
```bash
curl -X POST -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"email":"admin@example.com","operation":"SYSTEM_CONFIG"}' \
     http://localhost:8080/api/protected/otp/admin
```

2. Check email for OTP

3. Renew license:
```bash
curl -X POST -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     -H "Content-Type: application/json" \
     -d '{"email":"admin@example.com","otp":"123456","newEndDate":"2024-12-31"}' \
     http://localhost:8080/api/protected/licence/renew
```

### For Developers

#### 1. Manual License Check

```java
LicenseScheduler scheduler = LicenseScheduler.getInstance();
scheduler.triggerLicenseCheck();
```

#### 2. Create Test License

```java
LicenseService licenseService = new LicenseService();
License license = new License();
license.setLicenseKey("TEST-LICENSE-001");
license.setStartDate(LocalDate.now());
license.setEndDate(LocalDate.now().plusDays(30));
license.setRemainingDays(30);
license.setStatus("ACTIVE");
license.setCreatedBy("developer");

licenseService.createLicense(license);
```

## Troubleshooting

### Common Issues

#### 1. License Validation Fails

**Symptoms:**
- Application won't start
- "Invalid license" error messages

**Solutions:**
- Check database connectivity
- Verify license table exists and has data
- Check system date/time
- Review application logs

#### 2. OTP Not Received

**Symptoms:**
- OTP generation succeeds but email not received

**Solutions:**
- Verify SMTP configuration
- Check email spam/junk folder
- Validate email address format
- Review email service logs

#### 3. Database Connection Issues

**Symptoms:**
- DataAccessException errors
- Connection timeout messages

**Solutions:**
- Verify PostgreSQL service is running
- Check database credentials
- Test database connectivity
- Review connection pool settings

### Log Analysis

#### License-related Log Entries

```
# Successful license validation
INFO: License validation result: true, remaining days: 30

# License expiration warning
WARNING: License expiring soon: 7 days remaining

# License expired
SEVERE: License has expired. Application access restricted.

# OTP generation
INFO: OTP generated for operation: RENEW, email: admin@example.com
```

## Migration from Old System

### Differences from File-based System

| Feature | Old System (license.dat) | New System (Database) |
|---------|-------------------------|----------------------|
| Storage | File-based | Database-driven |
| Security | Basic file validation | OTP + JWT authentication |
| Management | Manual file operations | REST API |
| Monitoring | Limited logging | Comprehensive audit trail |
| Scalability | Single instance | Multi-instance support |
| Backup | File backup required | Database backup included |

### Migration Steps

1. **Remove Old Components:**
   - Delete `LicenseManager.java`
   - Remove `license.dat` file references
   - Update imports in affected classes

2. **Database Setup:**
   - Execute `license_schema.sql`
   - Insert initial license data
   - Verify table creation

3. **Application Updates:**
   - Update `MainApp.java` to use `LicenseService`
   - Modify `AuthenticationFilter.java`
   - Configure scheduler startup

4. **Testing:**
   - Verify license validation works
   - Test API endpoints
   - Confirm scheduler operations

### Backward Compatibility

The new system is **not backward compatible** with the old file-based approach. Complete migration is required.

---

## Support and Maintenance

### Regular Maintenance Tasks

1. **Daily:**
   - Monitor license status
   - Review error logs
   - Check scheduler execution

2. **Weekly:**
   - Clean up expired OTPs
   - Review audit logs
   - Backup license data

3. **Monthly:**
   - Analyze license usage patterns
   - Update documentation
   - Review security settings

### Contact Information

For technical support or questions about the license system:

- **Development Team**: [Contact Information]
- **System Administrator**: [Contact Information]
- **Documentation**: This file and related guides in the `/READ` folder

---

*Last Updated: January 2024*
*Version: 2.0.0*
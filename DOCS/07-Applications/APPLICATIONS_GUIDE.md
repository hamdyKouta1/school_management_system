# School Management System - Applications Guide

## Table of Contents

1. [Overview](#overview)
2. [Core Applications](#core-applications)
3. [Student Management](#student-management)
4. [Authentication & Authorization](#authentication--authorization)
5. [Attendance Management](#attendance-management)
6. [Administrative Tools](#administrative-tools)
7. [Bulk Operations](#bulk-operations)
8. [System Utilities](#system-utilities)
9. [Configuration & Deployment](#configuration--deployment)
10. [Troubleshooting](#troubleshooting)

## Overview

The School Management System consists of multiple interconnected applications and services that provide comprehensive school administration functionality. This guide documents all applications, their purposes, configuration requirements, and deployment considerations.

### System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    School Management System                      │
├─────────────────────────────────────────────────────────────────┤
│  Web Layer (Servlets)                                          │
│  ├── Authentication (AuthServlet)                              │
│  ├── Student Management (StudentServlet, InsertFullStudentServlet) │
│  ├── Attendance (AttendanceServlet)                            │
│  ├── Admin Tools (DashboardServlet, SchoolConfigServlet)       │
│  └── Utilities (UserServlet, LicenseServlet)                   │
├─────────────────────────────────────────────────────────────────┤
│  Business Logic Layer                                          │
│  ├── Schedulers (AttendanceScheduler, LicenseScheduler)        │
│  ├── Configuration (ConfigLoader)                              │
│  └── Security (JWT, OTP, CSRF)                                 │
├─────────────────────────────────────────────────────────────────┤
│  Data Layer                                                     │
│  ├── Database (PostgreSQL)                                     │
│  ├── File Storage (Templates, Uploads)                         │
│  └── External Services (Email, SMS)                            │
└─────────────────────────────────────────────────────────────────┘
```

## Core Applications

### MainApp - Application Bootstrap

**Purpose**: Main application entry point that initializes all system components and starts the HTTP server.

**Location**: `com.canalprep.MainApp`

**Functionality**:
- HTTP server initialization and configuration
- Database connection setup
- Servlet registration and routing
- Scheduler initialization (License and Attendance)
- Configuration loading and validation
- Graceful shutdown handling

**Configuration Parameters**:
```properties
# Server configuration
server.bind_address=0.0.0.0
server.port=8081

# Database configuration
db.driver=org.postgresql.Driver
db.url=jdbc:postgresql://localhost:5432/canal_prep_school_clone
db.username=postgres
db.password=123
```

**Environment-Specific Deployment**:

*Development*:
```bash
java -Dapp.env=dev -jar target/school-management.jar 8081
```

*Staging*:
```bash
java -Dapp.env=staging -Dconfig.secure.path=/etc/app/secure.properties -jar school-management.jar
```

*Production*:
```bash
java -Xmx4g -Dapp.env=prod -Dconfig.secure.path=/etc/app/secure.properties -jar school-management.jar
```

**Troubleshooting**:
- **Issue**: Application fails to start
  - **Cause**: Database connection failure
  - **Solution**: Verify database configuration and connectivity
- **Issue**: Port already in use
  - **Cause**: Another process using the configured port
  - **Solution**: Change port or stop conflicting process

### ConfigLoader - Configuration Management

**Purpose**: Centralized configuration loading with environment-specific overrides and secure property handling.

**Location**: `com.canalprep.config.ConfigLoader`

**Functionality**:
- Load base application properties
- Load secure properties from external files
- Environment-specific property overrides
- Property validation and type conversion
- Runtime configuration updates

**Configuration Hierarchy**:
1. System properties (-Dproperty=value)
2. Environment variables
3. External secure configuration
4. Environment-specific properties (.dev, .staging, .prod)
5. Base application properties
6. Default values

**Secure Configuration Paths** (checked in order):
1. System property: `config.secure.path`
2. Environment variable: `CONFIG_SECURE_PATH`
3. `./config/secure.properties`
4. `/etc/app/secure.properties`
5. `config/secure.properties`

**Environment-Specific Deployment**:

*Development*:
```properties
# No secure properties required
app.env=dev
```

*Staging*:
```bash
export CONFIG_SECURE_PATH=/etc/app/secure.properties
```

*Production*:
```bash
# Set in systemd service
Environment=CONFIG_SECURE_PATH=/etc/app/secure.properties
```

**Troubleshooting**:
- **Issue**: Configuration not loading
  - **Cause**: File permissions or path issues
  - **Solution**: Check file exists and has correct permissions (600)
- **Issue**: Environment overrides not working
  - **Cause**: Incorrect property naming
  - **Solution**: Use format `property.name.environment` (e.g., `db.url.prod`)

## Student Management

### StudentServlet - Student Information Management

**Purpose**: Handles all student-related operations including CRUD operations, search, and data retrieval.

**Location**: `com.canalprep.servlet.StudentServlet`

**Endpoints**:
- `GET /api/students` - List all students with pagination
- `GET /api/students/{id}` - Get specific student details
- `POST /api/students` - Create new student
- `PUT /api/students/{id}` - Update student information
- `DELETE /api/students/{id}` - Delete student record

**Configuration Parameters**:
```properties
# Pagination settings
students.page_size=50
students.max_page_size=200

# Search configuration
students.search.enabled=true
students.search.fields=name,email,phone,student_id
```

**Required Permissions**: ADMIN, TEACHER

**Environment-Specific Deployment**:

*Development*:
- Full CRUD operations enabled
- Debug logging enabled
- No data validation restrictions

*Staging*:
- Limited delete operations
- Enhanced logging
- Data validation enabled

*Production*:
- Audit logging for all operations
- Strict data validation
- Rate limiting enabled

**Troubleshooting**:
- **Issue**: Student data not saving
  - **Cause**: Database constraint violations
  - **Solution**: Check required fields and data formats
- **Issue**: Search not working
  - **Cause**: Database indexing issues
  - **Solution**: Rebuild search indexes

### InsertFullStudentServlet - Comprehensive Student Registration

**Purpose**: Handles complete student registration including personal information, medical history, qualifications, and contact details.

**Location**: `com.canalprep.servlet.InsertFullStudentServlet`

**Functionality**:
- Complete student profile creation
- Medical history recording
- Academic qualifications management
- Emergency contact information
- Document upload handling
- Data validation and verification

**Configuration Parameters**:
```properties
# File upload settings
student.upload.max_file_size=10MB
student.upload.allowed_types=pdf,jpg,png,doc,docx
student.upload.path=/opt/app/uploads/students

# Validation settings
student.validation.required_fields=name,email,phone,date_of_birth
student.validation.email_domain_whitelist=
```

**Required Permissions**: ADMIN

**Environment-Specific Deployment**:

*Development*:
```properties
student.upload.path=./uploads/students
student.validation.strict=false
```

*Staging*:
```properties
student.upload.path=/opt/app/staging/uploads/students
student.validation.strict=true
```

*Production*:
```properties
student.upload.path=/opt/app/uploads/students
student.validation.strict=true
student.upload.virus_scan=true
```

**Troubleshooting**:
- **Issue**: File upload fails
  - **Cause**: Directory permissions or disk space
  - **Solution**: Check upload directory permissions and available space
- **Issue**: Validation errors
  - **Cause**: Missing required fields
  - **Solution**: Review validation configuration and required fields

### AddStudentPhoneServlet - Phone Number Management

**Purpose**: Manages student phone number information including multiple contact numbers and emergency contacts.

**Location**: `com.canalprep.servlet.AddStudentPhoneServlet`

**Functionality**:
- Add multiple phone numbers per student
- Categorize phone numbers (primary, emergency, parent)
- Phone number validation and formatting
- SMS notification integration

**Configuration Parameters**:
```properties
# Phone validation
phone.validation.enabled=true
phone.validation.country_code=+1
phone.validation.format=international

# SMS integration
sms.provider=twilio
sms.enabled=false
```

**Required Permissions**: ADMIN, TEACHER

**Environment-Specific Deployment**:

*Development*:
```properties
phone.validation.enabled=false
sms.enabled=false
```

*Staging*:
```properties
phone.validation.enabled=true
sms.enabled=true
sms.test_mode=true
```

*Production*:
```properties
phone.validation.enabled=true
sms.enabled=true
sms.test_mode=false
```

### AddStudentNoteServlet - Student Notes Management

**Purpose**: Manages student notes, comments, and behavioral records for tracking student progress and incidents.

**Location**: `com.canalprep.servlet.AddStudentNoteServlet`

**Functionality**:
- Add timestamped notes to student records
- Categorize notes (academic, behavioral, medical, administrative)
- Note visibility controls (private, teacher-only, parent-visible)
- Note history and audit trail

**Configuration Parameters**:
```properties
# Note settings
notes.max_length=2000
notes.categories=academic,behavioral,medical,administrative
notes.default_visibility=teacher_only

# Audit settings
notes.audit_enabled=true
notes.retention_days=365
```

**Required Permissions**: ADMIN, TEACHER

**Environment-Specific Deployment**:

*Development*:
```properties
notes.audit_enabled=false
notes.retention_days=30
```

*Staging*:
```properties
notes.audit_enabled=true
notes.retention_days=180
```

*Production*:
```properties
notes.audit_enabled=true
notes.retention_days=365
notes.encryption_enabled=true
```

### AddMedicalHistoryServlet - Medical Records Management

**Purpose**: Manages student medical history, allergies, medications, and health-related information.

**Location**: `com.canalprep.servlet.AddMedicalHistoryServlet`

**Functionality**:
- Record medical conditions and allergies
- Track medications and dosages
- Emergency medical contact information
- Medical document uploads
- HIPAA compliance features

**Configuration Parameters**:
```properties
# Medical records settings
medical.encryption_enabled=true
medical.access_log_enabled=true
medical.retention_years=7

# Document settings
medical.documents.max_size=20MB
medical.documents.allowed_types=pdf,jpg,png,doc,docx
medical.documents.path=/opt/app/secure/medical
```

**Required Permissions**: ADMIN, NURSE

**Security Requirements**:
- All medical data must be encrypted at rest
- Access logging required for compliance
- Restricted access based on roles
- Secure document storage

**Environment-Specific Deployment**:

*Development*:
```properties
medical.encryption_enabled=false
medical.documents.path=./uploads/medical
```

*Staging*:
```properties
medical.encryption_enabled=true
medical.documents.path=/opt/app/staging/secure/medical
```

*Production*:
```properties
medical.encryption_enabled=true
medical.documents.path=/opt/app/secure/medical
medical.backup_enabled=true
medical.compliance_mode=hipaa
```

### AddQualificationsServlet - Academic Qualifications Management

**Purpose**: Manages student academic qualifications, certifications, and educational background.

**Location**: `com.canalprep.servlet.AddQualificationsServlet`

**Functionality**:
- Record academic qualifications and degrees
- Track certifications and licenses
- Grade and GPA management
- Transcript uploads and verification
- Academic history timeline

**Configuration Parameters**:
```properties
# Qualifications settings
qualifications.grade_scale=4.0
qualifications.verification_required=true
qualifications.document_required=true

# Document settings
qualifications.documents.path=/opt/app/uploads/qualifications
qualifications.documents.max_size=15MB
```

**Required Permissions**: ADMIN, REGISTRAR

**Environment-Specific Deployment**:

*Development*:
```properties
qualifications.verification_required=false
qualifications.documents.path=./uploads/qualifications
```

*Staging*:
```properties
qualifications.verification_required=true
qualifications.documents.path=/opt/app/staging/uploads/qualifications
```

*Production*:
```properties
qualifications.verification_required=true
qualifications.documents.path=/opt/app/uploads/qualifications
qualifications.audit_enabled=true
```

### StudentQRCodeServlet - QR Code Generation

**Purpose**: Generates QR codes for students for quick identification, attendance tracking, and access control.

**Location**: `com.canalprep.servlet.StudentQRCodeServlet`

**Functionality**:
- Generate unique QR codes for each student
- QR code customization (size, format, data)
- Batch QR code generation
- QR code printing and export
- Integration with attendance system

**Configuration Parameters**:
```properties
# QR Code settings
qr.size=200
qr.format=PNG
qr.error_correction=M
qr.margin=4

# Data encoding
qr.data_format=student_id
qr.encryption_enabled=false
```

**Required Permissions**: ADMIN, TEACHER

**Environment-Specific Deployment**:

*Development*:
```properties
qr.encryption_enabled=false
qr.batch_size=10
```

*Staging*:
```properties
qr.encryption_enabled=true
qr.batch_size=50
```

*Production*:
```properties
qr.encryption_enabled=true
qr.batch_size=100
qr.watermark_enabled=true
```

## Authentication & Authorization

### AuthServlet - Authentication Management

**Purpose**: Handles user authentication, session management, and security token operations.

**Location**: `com.canalprep.servlet.AuthServlet`

**Endpoints**:
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout
- `POST /api/auth/refresh` - Token refresh
- `GET /api/auth/status` - Authentication status
- `POST /api/auth/change-password` - Password change

**Configuration Parameters**:
```properties
# JWT settings
jwt.secret=your_jwt_secret_key
jwt.expiration_ms=1800000
jwt.refresh_expiration_ms=604800000

# Session settings
session.timeout_minutes=30
session.max_concurrent=5

# Security settings
auth.max_login_attempts=5
auth.lockout_duration_minutes=15
```

**Required Permissions**: PUBLIC (for login), authenticated users (for other operations)

**Environment-Specific Deployment**:

*Development*:
```properties
jwt.expiration_ms=3600000  # 1 hour
auth.max_login_attempts=10
auth.lockout_duration_minutes=5
```

*Staging*:
```properties
jwt.expiration_ms=1800000  # 30 minutes
auth.max_login_attempts=5
auth.lockout_duration_minutes=15
```

*Production*:
```properties
jwt.expiration_ms=1800000  # 30 minutes
auth.max_login_attempts=3
auth.lockout_duration_minutes=30
auth.audit_enabled=true
```

**Troubleshooting**:
- **Issue**: Login fails with correct credentials
  - **Cause**: Account locked due to failed attempts
  - **Solution**: Wait for lockout period or admin unlock
- **Issue**: Token expired errors
  - **Cause**: JWT token expiration
  - **Solution**: Implement token refresh mechanism

### UserServlet - User Management

**Purpose**: Manages user accounts, roles, permissions, and profile information.

**Location**: `com.canalprep.servlet.UserServlet`

**Functionality**:
- User account creation and management
- Role assignment and permission management
- Profile information updates
- Password reset and recovery
- User activity tracking

**Configuration Parameters**:
```properties
# User settings
user.password.min_length=8
user.password.require_special_chars=true
user.password.expiry_days=90

# Role settings
user.roles.default=STUDENT
user.roles.available=ADMIN,TEACHER,STUDENT,PARENT,STAFF

# Profile settings
user.profile.required_fields=name,email
user.profile.optional_fields=phone,address,bio
```

**Required Permissions**: ADMIN (for user management), authenticated users (for own profile)

**Environment-Specific Deployment**:

*Development*:
```properties
user.password.min_length=4
user.password.require_special_chars=false
user.password.expiry_days=0
```

*Staging*:
```properties
user.password.min_length=8
user.password.require_special_chars=true
user.password.expiry_days=180
```

*Production*:
```properties
user.password.min_length=12
user.password.require_special_chars=true
user.password.expiry_days=90
user.audit_enabled=true
```

### StandaloneOTPServlet - One-Time Password Management

**Purpose**: Handles OTP generation, validation, and delivery for secure operations like password reset and admin role assignment.

**Location**: `com.canalprep.servlet.StandaloneOTPServlet`

**Functionality**:
- OTP generation for various operations
- Email and SMS delivery
- OTP validation and expiration
- Rate limiting and abuse prevention
- Operation-specific OTP types

**Configuration Parameters**:
```properties
# OTP settings
otp.length=6
otp.expiry_minutes=10
otp.max_attempts=3

# Delivery settings
otp.email.enabled=true
otp.sms.enabled=false
otp.email.template_path=templates/otp_email.html

# Operations
otp.operations.allowed=ADMIN_CREATION,PASSWORD_RESET,REMOVE,RENEW

# Rate limiting
otp.rate_limit.requests_per_hour=10
otp.rate_limit.requests_per_day=50
```

**Required Permissions**: PUBLIC (for password reset), ADMIN (for admin creation)

**Environment-Specific Deployment**:

*Development*:
```properties
otp.expiry_minutes=60
otp.max_attempts=10
otp.rate_limit.requests_per_hour=100
```

*Staging*:
```properties
otp.expiry_minutes=15
otp.max_attempts=5
otp.rate_limit.requests_per_hour=20
```

*Production*:
```properties
otp.expiry_minutes=10
otp.max_attempts=3
otp.rate_limit.requests_per_hour=10
otp.audit_enabled=true
```

## Attendance Management

### AttendanceServlet - Attendance Operations

**Purpose**: Manages student attendance recording, tracking, and reporting.

**Location**: `com.canalprep.servlet.AttendanceServlet`

**Functionality**:
- Manual attendance recording
- Bulk attendance operations
- Attendance reports and analytics
- Absence notifications
- Integration with QR code scanning

**Configuration Parameters**:
```properties
# Attendance settings
attendance.auto_mark_absent=true
attendance.grace_period_minutes=15
attendance.notification_enabled=true

# Reporting settings
attendance.report.formats=pdf,excel,csv
attendance.report.retention_days=365

# Integration settings
attendance.qr_scan_enabled=true
attendance.mobile_app_enabled=false
```

**Required Permissions**: ADMIN, TEACHER

**Environment-Specific Deployment**:

*Development*:
```properties
attendance.grace_period_minutes=60
attendance.notification_enabled=false
```

*Staging*:
```properties
attendance.grace_period_minutes=30
attendance.notification_enabled=true
attendance.test_mode=true
```

*Production*:
```properties
attendance.grace_period_minutes=15
attendance.notification_enabled=true
attendance.audit_enabled=true
```

### AttendanceScheduler - Automated Attendance Processing

**Purpose**: Automated scheduler that processes daily attendance, marks absences, and sends notifications.

**Location**: `com.canalprep.scheduler.AttendanceScheduler`

**Functionality**:
- Daily automated attendance processing
- Automatic absence marking
- Parent/guardian notifications
- Attendance report generation
- Integration with school calendar

**Configuration Parameters**:
```properties
# Scheduler settings
attendance.schedule.time=16:30
attendance.schedule.enabled=true
attendance.schedule.timezone=UTC

# Processing settings
attendance.auto_process.enabled=true
attendance.auto_process.cutoff_time=09:00
attendance.auto_process.weekend_skip=true

# Notification settings
attendance.notifications.parents=true
attendance.notifications.admin=true
attendance.notifications.template=daily_attendance
```

**Required Permissions**: SYSTEM (automated)

**Environment-Specific Deployment**:

*Development*:
```properties
attendance.schedule.time=*/5 * * * *  # Every 5 minutes for testing
attendance.schedule.enabled=true
attendance.notifications.parents=false
```

*Staging*:
```properties
attendance.schedule.time=16:30
attendance.schedule.enabled=true
attendance.notifications.parents=true
attendance.notifications.test_mode=true
```

*Production*:
```properties
attendance.schedule.time=16:30
attendance.schedule.enabled=true
attendance.schedule.timezone=America/New_York
attendance.notifications.parents=true
attendance.backup_enabled=true
```

**Troubleshooting**:
- **Issue**: Scheduler not running
  - **Cause**: Scheduler disabled or time configuration error
  - **Solution**: Check `attendance.schedule.enabled` and time format
- **Issue**: Notifications not sent
  - **Cause**: Email configuration issues
  - **Solution**: Verify SMTP settings and email templates

## Administrative Tools

### DashboardServlet - Administrative Dashboard

**Purpose**: Provides administrative dashboard with system overview, statistics, and quick access to common operations.

**Location**: `com.canalprep.servlet.DashboardServlet`

**Functionality**:
- System statistics and metrics
- Recent activity overview
- Quick action buttons
- User activity monitoring
- System health indicators

**Configuration Parameters**:
```properties
# Dashboard settings
dashboard.refresh_interval_seconds=30
dashboard.max_recent_activities=50
dashboard.cache_enabled=true

# Metrics settings
dashboard.metrics.enabled=true
dashboard.metrics.retention_days=30
dashboard.metrics.real_time=true

# Display settings
dashboard.widgets.enabled=students,attendance,users,system
dashboard.theme=default
```

**Required Permissions**: ADMIN, TEACHER (limited view)

**Environment-Specific Deployment**:

*Development*:
```properties
dashboard.refresh_interval_seconds=5
dashboard.metrics.real_time=true
dashboard.debug_mode=true
```

*Staging*:
```properties
dashboard.refresh_interval_seconds=15
dashboard.metrics.real_time=true
dashboard.cache_enabled=true
```

*Production*:
```properties
dashboard.refresh_interval_seconds=30
dashboard.metrics.real_time=false
dashboard.cache_enabled=true
dashboard.performance_monitoring=true
```

### SchoolConfigServlet - School Configuration Management

**Purpose**: Manages school-specific configuration settings, academic calendar, and institutional information.

**Location**: `com.canalprep.servlet.SchoolConfigServlet`

**Functionality**:
- School information management
- Academic calendar configuration
- Term and semester settings
- Holiday and break scheduling
- Grading system configuration

**Configuration Parameters**:
```properties
# School settings
school.name=Canal Prep School
school.address=123 Education St, Learning City
school.phone=+1-555-0123
school.email=info@canalprep.edu

# Academic settings
school.academic_year.start_month=9
school.academic_year.end_month=6
school.grading.scale=4.0
school.grading.passing_grade=2.0

# Calendar settings
school.calendar.holidays_enabled=true
school.calendar.weekend_days=saturday,sunday
```

**Required Permissions**: ADMIN

**Environment-Specific Deployment**:

*Development*:
```properties
school.name=Canal Prep School (Dev)
school.email=dev@canalprep.edu
```

*Staging*:
```properties
school.name=Canal Prep School (Staging)
school.email=staging@canalprep.edu
```

*Production*:
```properties
school.name=Canal Prep School
school.email=info@canalprep.edu
school.backup_enabled=true
```

### LicenseServlet - License Management

**Purpose**: Manages software licensing, validation, and renewal processes.

**Location**: `com.canalprep.servlet.LicenseServlet`

**Functionality**:
- License validation and verification
- License renewal processing
- Usage tracking and reporting
- License expiration notifications
- Multi-tenant license management

**Configuration Parameters**:
```properties
# License settings
license.validation_enabled=true
license.check_interval_hours=24
license.grace_period_days=7

# Renewal settings
license.auto_renewal=false
license.renewal_notification_days=30,15,7,1
license.renewal_url=https://licensing.canalprep.com

# Tracking settings
license.usage_tracking=true
license.anonymous_stats=true
```

**Required Permissions**: ADMIN

**Environment-Specific Deployment**:

*Development*:
```properties
license.validation_enabled=false
license.check_interval_hours=168  # Weekly
```

*Staging*:
```properties
license.validation_enabled=true
license.check_interval_hours=24
license.test_mode=true
```

*Production*:
```properties
license.validation_enabled=true
license.check_interval_hours=24
license.strict_mode=true
```

## Bulk Operations

### BulkStudentBatchServlet - Bulk Student Operations

**Purpose**: Handles bulk student operations including batch imports, exports, and mass updates.

**Location**: `com.canalprep.servlet.BulkStudentBatchServlet`

**Functionality**:
- Excel/CSV file imports
- Batch student creation
- Data validation and error reporting
- Progress tracking for large operations
- Template generation and download

**Configuration Parameters**:
```properties
# Bulk operation settings
bulk.max_batch_size=1000
bulk.chunk_size=100
bulk.timeout_minutes=30

# File settings
bulk.upload.max_file_size=50MB
bulk.upload.allowed_formats=xlsx,csv
bulk.template.path=templates/student_batch_template.xlsx

# Validation settings
bulk.validation.strict=true
bulk.validation.skip_duplicates=false
bulk.validation.required_fields=name,email,student_id
```

**Required Permissions**: ADMIN

**Environment-Specific Deployment**:

*Development*:
```properties
bulk.max_batch_size=100
bulk.validation.strict=false
bulk.template.path=student_batch_template.xlsx
```

*Staging*:
```properties
bulk.max_batch_size=500
bulk.validation.strict=true
bulk.template.path=/opt/app/staging/templates/student_batch_template.xlsx
```

*Production*:
```properties
bulk.max_batch_size=1000
bulk.validation.strict=true
bulk.template.path=/opt/app/templates/student_batch_template.xlsx
bulk.audit_enabled=true
```

**Troubleshooting**:
- **Issue**: Import fails with large files
  - **Cause**: Memory or timeout limits
  - **Solution**: Increase heap size or reduce batch size
- **Issue**: Validation errors
  - **Cause**: Data format issues
  - **Solution**: Check template format and required fields

## System Utilities

### LicenseScheduler - License Monitoring

**Purpose**: Automated scheduler that monitors license status, validates licensing, and handles renewal notifications.

**Location**: `com.canalprep.scheduler.LicenseScheduler`

**Functionality**:
- Periodic license validation
- Expiration monitoring and alerts
- Automatic renewal attempts (if configured)
- Usage statistics collection
- Compliance reporting

**Configuration Parameters**:
```properties
# Scheduler settings
license.scheduler.enabled=true
license.scheduler.interval_hours=24
license.scheduler.startup_delay_minutes=5

# Monitoring settings
license.monitor.expiration_warning_days=30,15,7,1
license.monitor.usage_tracking=true
license.monitor.compliance_check=true

# Notification settings
license.notifications.admin_email=admin@example.com
license.notifications.enabled=true
license.notifications.template=license_expiration
```

**Required Permissions**: SYSTEM (automated)

**Environment-Specific Deployment**:

*Development*:
```properties
license.scheduler.enabled=false
license.monitor.usage_tracking=false
```

*Staging*:
```properties
license.scheduler.enabled=true
license.scheduler.interval_hours=168  # Weekly
license.notifications.test_mode=true
```

*Production*:
```properties
license.scheduler.enabled=true
license.scheduler.interval_hours=24
license.notifications.enabled=true
license.backup_enabled=true
```

## Configuration & Deployment

### Environment-Specific Configuration

Each application component supports environment-specific configuration through property overrides:

**Property Override Pattern**:
```properties
# Base property
property.name=base_value

# Environment-specific overrides
property.name.dev=development_value
property.name.staging=staging_value
property.name.prod=production_value
```

**Common Environment Configurations**:

*Development Environment*:
- Relaxed security settings
- Debug logging enabled
- Local database connections
- Permissive CORS settings
- Disabled external integrations

*Staging Environment*:
- Production-like security
- Enhanced logging
- Staging database connections
- Restricted CORS settings
- Test mode for external integrations

*Production Environment*:
- Maximum security settings
- Audit logging
- Production database connections
- Strict CORS settings
- Full external integrations

### Deployment Checklist

**Pre-Deployment**:
- [ ] Configuration files validated
- [ ] Database migrations applied
- [ ] Security settings reviewed
- [ ] External dependencies verified
- [ ] Backup procedures tested

**Deployment**:
- [ ] Application deployed to target environment
- [ ] Configuration files in place
- [ ] Services started and verified
- [ ] Health checks passing
- [ ] Monitoring configured

**Post-Deployment**:
- [ ] Functionality testing completed
- [ ] Performance monitoring active
- [ ] Log aggregation configured
- [ ] Backup verification
- [ ] Documentation updated

## Troubleshooting

### Common Issues

#### Application Startup Issues

**Symptoms**:
- Application fails to start
- Service won't start
- Port binding errors

**Diagnosis Steps**:
1. Check application logs
2. Verify configuration files
3. Test database connectivity
4. Check port availability
5. Validate file permissions

**Solutions**:
```bash
# Check logs
tail -f /var/log/app/application.log

# Test configuration
java -jar school-management.jar --validate-config

# Check database
psql -h database-host -U username -d database_name

# Check ports
netstat -tlnp | grep 8081

# Fix permissions
chmod 600 /etc/app/secure.properties
chown app:app /etc/app/secure.properties
```

#### Database Connection Issues

**Symptoms**:
- Connection refused errors
- Authentication failures
- Timeout errors

**Diagnosis Steps**:
1. Verify database server status
2. Check network connectivity
3. Validate credentials
4. Review connection pool settings
5. Check database logs

**Solutions**:
```bash
# Test connectivity
telnet database-host 5432

# Verify credentials
psql -h database-host -U username -d database_name

# Check connection pool
grep -i "connection" /etc/app/secure.properties

# Review database logs
tail -f /var/log/postgresql/postgresql.log
```

#### Performance Issues

**Symptoms**:
- Slow response times
- High CPU/memory usage
- Database query timeouts

**Diagnosis Steps**:
1. Monitor system resources
2. Analyze application metrics
3. Review database performance
4. Check network latency
5. Examine log files

**Solutions**:
```bash
# Monitor resources
top
htop
free -h

# Check application metrics
curl http://localhost:8081/api/metrics

# Database performance
psql -c "SELECT * FROM pg_stat_activity;"

# Optimize JVM
-Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

#### Security Issues

**Symptoms**:
- Authentication failures
- Authorization errors
- CORS policy violations

**Diagnosis Steps**:
1. Review security logs
2. Check authentication configuration
3. Validate JWT settings
4. Review CORS configuration
5. Check SSL/TLS setup

**Solutions**:
```bash
# Check security logs
grep -i "auth\|security" /var/log/app/application.log

# Validate JWT
echo "JWT_TOKEN" | base64 -d

# Test CORS
curl -H "Origin: https://frontend.com" -I https://api.example.com/api/health

# Check SSL
openssl s_client -connect api.example.com:443
```

### Application-Specific Troubleshooting

#### Student Management Issues

**Common Problems**:
- Student data not saving
- Search functionality not working
- File uploads failing

**Solutions**:
- Check database constraints and validation rules
- Rebuild search indexes
- Verify upload directory permissions and disk space

#### Attendance System Issues

**Common Problems**:
- Scheduler not running
- Attendance not being marked
- Notifications not sent

**Solutions**:
- Verify scheduler configuration and enable status
- Check attendance processing rules and cutoff times
- Validate email/SMS configuration

#### Authentication Issues

**Common Problems**:
- Login failures with correct credentials
- Token expiration errors
- Permission denied errors

**Solutions**:
- Check account lockout status and reset if needed
- Implement token refresh mechanism
- Review role assignments and permissions

---

*Last updated: January 2025*
*Version: 1.0*
*For technical support, refer to the specific application troubleshooting sections or contact the development team.*
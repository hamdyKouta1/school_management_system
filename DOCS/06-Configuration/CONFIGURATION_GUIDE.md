# School Management System - Configuration Guide

## Table of Contents

1. [Overview](#overview)
2. [Configuration Files](#configuration-files)
3. [Environment-Specific Configuration](#environment-specific-configuration)
4. [Application Properties Reference](#application-properties-reference)
5. [Security Configuration](#security-configuration)
6. [Environment Setup](#environment-setup)
7. [Deployment Configurations](#deployment-configurations)
8. [Configuration Validation](#configuration-validation)
9. [Troubleshooting](#troubleshooting)
10. [Best Practices](#best-practices)

## Overview

The School Management System uses a flexible configuration system that supports:

- **Environment-specific overrides** (development, staging, production)
- **External secure configuration files** for sensitive data
- **Runtime configuration loading** with fallback mechanisms
- **Property inheritance** and override patterns

### Configuration Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Configuration Hierarchy                   │
├─────────────────────────────────────────────────────────────┤
│ 1. System Properties (-Dproperty=value)                    │
│ 2. Environment Variables                                    │
│ 3. External Secure Configuration (secure.properties)       │
│ 4. Environment-Specific Properties (.dev, .staging, .prod) │
│ 5. Base Application Properties (application.properties)    │
│ 6. Default Values (hardcoded in application)              │
└─────────────────────────────────────────────────────────────┘
```

## Configuration Files

### Primary Configuration Files

| File | Purpose | Location | Security Level |
|------|---------|----------|----------------|
| `application.properties` | Main configuration | `src/main/resources/` | Public |
| `secure.properties` | Sensitive configuration | External paths | Restricted |
| Environment Variables | Runtime overrides | System environment | Varies |
| System Properties | JVM-level overrides | Command line | Varies |

### Configuration File Locations

#### application.properties
- **Location**: `src/main/resources/application.properties`
- **Purpose**: Contains all non-sensitive configuration options
- **Version Control**: Committed to repository
- **Access**: Public, included in JAR file

#### secure.properties
- **Locations** (checked in order):
  1. System property: `-Dconfig.secure.path=/path/to/secure.properties`
  2. Environment variable: `CONFIG_SECURE_PATH=/path/to/secure.properties`
  3. `./config/secure.properties` (relative to working directory)
  4. `/etc/app/secure.properties` (Linux/Unix systems)
  5. `config/secure.properties` (fallback)
- **Purpose**: Contains sensitive configuration (passwords, secrets, keys)
- **Version Control**: **NEVER** committed to repository
- **Access**: Restricted (600 permissions recommended)

## Environment-Specific Configuration

### Environment Detection

The system detects the current environment through:

1. **System Property**: `-Dapp.env=production`
2. **Application Property**: `app.env=development`
3. **Default**: No environment (uses base properties)

### Environment Override Pattern

Properties can be overridden per environment using suffixes:

```properties
# Base property
db.url=jdbc:postgresql://localhost:5432/school_dev

# Development override
db.url.dev=jdbc:postgresql://localhost:5432/school_dev

# Staging override  
db.url.staging=jdbc:postgresql://staging-db.example.com:5432/school

# Production override
db.url.prod=jdbc:postgresql://prod-db.example.com:5432/school
```

### Supported Environments

| Environment | Suffix | Use Case |
|-------------|--------|----------|
| Development | `.dev` | Local development |
| Staging | `.staging` | Pre-production testing |
| Production | `.prod` | Live production system |

## Application Properties Reference

### API Configuration

```properties
# Base URL for API documentation and tools
api.base_url=http://localhost:8081/api
api.base_url.dev=http://localhost:8081/api
api.base_url.staging=http://staging.example.com:8081/api
api.base_url.prod=https://api.example.com/api
```

**Purpose**: Defines the base URL for API endpoints used by documentation tools and frontend applications.

**Default**: `http://localhost:8081/api`

**Environment Overrides**: Yes

### Admin Configuration

```properties
# Admin email for notifications and OTP delivery
admin.email=admin@example.com
admin.secret_code=123456

# Environment-specific overrides
admin.email.dev=dev-admin@example.com
admin.secret_code.dev=dev123
admin.email.staging=staging-admin@example.com
admin.secret_code.staging=staging123
admin.email.prod=admin@example.com
admin.secret_code.prod=
```

**Purpose**: 
- `admin.email`: Email address for administrative notifications and OTP delivery
- `admin.secret_code`: Secret code required to assign ADMIN role during registration

**Security Note**: The admin secret code should be empty or complex in production environments.

### Bulk Upload Configuration

```properties
# Excel template path for bulk student import
bulk_upload.template_path=student_batch_template.xlsx
bulk_upload.template_path.dev=student_batch_template.xlsx
bulk_upload.template_path.staging=/opt/app/templates/student_batch_template.xlsx
bulk_upload.template_path.prod=/opt/app/templates/student_batch_template.xlsx
```

**Purpose**: Specifies the location of the Excel template file used for bulk student imports.

**Path Types**: 
- Relative paths (development)
- Absolute paths (staging/production)

### CORS Configuration

```properties
# Cross-Origin Resource Sharing settings
cors.allow_credentials=true
cors.allowed_headers=Accept,Authorization,Content-Length,Content-Type,Origin,X-Requested-With,ngrok-skip-browser-warning
cors.allowed_methods=DELETE,GET,OPTIONS,POST,PUT
cors.allowed_origins=*

# Environment-specific CORS settings
cors.allowed_origins.dev=*
cors.allowed_origins.staging=https://staging-frontend.example.com
cors.allowed_origins.prod=https://frontend.example.com
```

**Purpose**: Configures Cross-Origin Resource Sharing for frontend integration.

**Security Considerations**:
- Use `*` only in development
- Specify exact origins in staging/production
- Include necessary headers for authentication

### CSRF Protection

```properties
# CSRF validation settings
security.csrf.allowed_origin=*
security.csrf.allowed_origin.dev=http://localhost:5173
security.csrf.allowed_origin.staging=https://staging-frontend.example.com
security.csrf.allowed_origin.prod=https://frontend.example.com
```

**Purpose**: Configures Cross-Site Request Forgery protection.

**Security Requirements**: Must exactly match the frontend application origin.

### Database Configuration

```properties
# Database connection settings
db.driver=org.postgresql.Driver
db.url=jdbc:postgresql://localhost:5432/canal_prep_school_clone
db.username=postgres
db.password=123

# Environment-specific database settings
db.url.dev=jdbc:postgresql://localhost:5432/canal_prep_school_clone
db.username.dev=postgres
db.password.dev=123

db.url.staging=jdbc:postgresql://staging-db.example.com:5432/school
db.username.staging=school_user
db.password.staging=

db.url.prod=jdbc:postgresql://prod-db.example.com:5432/school
db.username.prod=school_user
db.password.prod=
```

**Purpose**: Database connection configuration for PostgreSQL.

**Security Note**: Database passwords should be empty in application.properties and provided via secure.properties or environment variables.

### Email/SMTP Configuration

```properties
# SMTP settings for email functionality
email.host=smtp.gmail.com
email.port=587
email.username=canalprepschool@gmail.com
email.password=
email.from_address=canalprepschool@gmail.com
email.auth.enable=true
email.tls.enable=true

# Environment-specific email settings
email.host.dev=smtp.gmail.com
email.password.dev=
email.host.staging=smtp.staging.example.com
email.password.staging=
email.host.prod=smtp.example.com
email.password.prod=
```

**Purpose**: SMTP configuration for sending emails (OTP, notifications).

**Security Requirements**:
- Email passwords must be provided via secure.properties
- Use TLS encryption (port 587)
- Enable SMTP authentication

### JWT Configuration

```properties
# JWT token settings
jwt.secret=development-jwt-secret-key-for-testing-only-not-for-production-use-must-be-at-least-32-chars
jwt.expiration_ms=1800000

# Environment-specific JWT settings
jwt.secret.dev=dev_secret_key
jwt.expiration_ms.dev=1800000
jwt.secret.staging=
jwt.expiration_ms.staging=1800000
jwt.secret.prod=
jwt.expiration_ms.prod=1800000
```

**Purpose**: JWT token generation and validation configuration.

**Security Requirements**:
- JWT secret must be at least 256 bits (32 characters)
- Production secrets must be provided via secure.properties
- Expiration time in milliseconds (default: 30 minutes)

### Attendance Scheduler Configuration

```properties
# Automated attendance scheduling
attendance.schedule.time=16:30
attendance.schedule.enabled=true
attendance.schedule.timezone=UTC

# Environment-specific scheduler settings
attendance.schedule.time.dev=16:30
attendance.schedule.enabled.dev=true
attendance.schedule.timezone.dev=UTC

attendance.schedule.time.staging=17:00
attendance.schedule.enabled.staging=true
attendance.schedule.timezone.staging=UTC

attendance.schedule.time.prod=16:30
attendance.schedule.enabled.prod=true
attendance.schedule.timezone.prod=UTC
```

**Purpose**: Configures automated daily attendance updates.

**Format**: 
- Time: 24-hour format (HH:mm)
- Timezone: Standard timezone identifiers
- Enabled: Boolean flag to enable/disable scheduler

### Server Configuration

```properties
# HTTP server settings
server.bind_address=0.0.0.0
server.port=8081

# Environment-specific server settings
server.bind_address.dev=0.0.0.0
server.port.dev=8081
server.bind_address.staging=0.0.0.0
server.port.staging=8081
server.bind_address.prod=0.0.0.0
server.port.prod=8081
```

**Purpose**: HTTP server binding and port configuration.

**Network Settings**:
- `0.0.0.0`: Bind to all network interfaces
- `127.0.0.1`: Bind to localhost only
- Port: HTTP port number (default: 8081)

### UI Configuration

```properties
# Frontend application settings
ui.base_url=http://localhost:5173

# Environment-specific UI settings
ui.base_url.dev=http://localhost:5173
ui.base_url.staging=https://staging-frontend.example.com
ui.base_url.prod=https://frontend.example.com
```

**Purpose**: Frontend application base URL for CSRF validation and documentation.

### OTP Configuration

```properties
# OTP operation types
otp.operations.allowed=ADMIN_CREATION,PASSWORD_RESET,REMOVE,RENEW
```

**Purpose**: Defines allowed OTP operation types (must match database constraints).

### NGROK Configuration

```properties
# NGROK tunneling support
ngrok.allowed_header=ngrok-skip-browser-warning
```

**Purpose**: Additional header support for NGROK tunneling during development.

## Security Configuration

### secure.properties File Structure

Create a `secure.properties` file for sensitive configuration:

```properties
# Database credentials
db.password.prod=your_secure_database_password
db.password.staging=your_staging_database_password

# Email credentials
email.password.prod=your_secure_email_password
email.password.staging=your_staging_email_password

# JWT secrets
jwt.secret.prod=your_very_long_and_secure_jwt_secret_key_at_least_32_characters_long
jwt.secret.staging=your_staging_jwt_secret_key_at_least_32_characters_long

# Admin credentials
admin.secret_code.prod=your_complex_admin_secret_code
admin.secret_code.staging=your_staging_admin_secret_code
```

### Security Best Practices

1. **File Permissions**: Set secure.properties to 600 (owner read/write only)
2. **Never Commit**: Add secure.properties to .gitignore
3. **Strong Secrets**: Use complex, randomly generated secrets
4. **Environment Separation**: Use different secrets for each environment
5. **Regular Rotation**: Rotate secrets periodically

### Setting Up secure.properties

#### Linux/Unix Systems

```bash
# Create secure configuration directory
sudo mkdir -p /etc/app
sudo chown app:app /etc/app

# Create secure.properties file
sudo nano /etc/app/secure.properties

# Set secure permissions
sudo chmod 600 /etc/app/secure.properties
sudo chown app:app /etc/app/secure.properties
```

#### Windows Systems

```powershell
# Create config directory
New-Item -ItemType Directory -Path ".\config" -Force

# Create secure.properties file
New-Item -ItemType File -Path ".\config\secure.properties" -Force

# Set file permissions (remove inheritance, grant full control to current user only)
icacls ".\config\secure.properties" /inheritance:r /grant:r "$env:USERNAME:(F)"
```

## Environment Setup

### Development Environment

#### Configuration Steps

1. **Database Setup**:
   ```sql
   CREATE DATABASE canal_prep_school_clone;
   CREATE USER postgres WITH PASSWORD '123';
   GRANT ALL PRIVILEGES ON DATABASE canal_prep_school_clone TO postgres;
   ```

2. **Application Properties**:
   ```properties
   app.env=dev
   ```

3. **Run Command**:
   ```bash
   java -Dapp.env=dev -jar target/school-management.jar 8081
   ```

#### Development-Specific Settings

- Database: Local PostgreSQL instance
- Email: Test SMTP settings or disabled
- CORS: Permissive settings (`*` origins)
- JWT: Short-lived tokens for testing
- Scheduler: Enabled with frequent runs for testing

### Staging Environment

#### Configuration Steps

1. **Environment Variable**:
   ```bash
   export APP_ENV=staging
   export CONFIG_SECURE_PATH=/etc/app/secure.properties
   ```

2. **Secure Configuration**:
   ```properties
   # /etc/app/secure.properties
   db.password.staging=staging_db_password
   email.password.staging=staging_email_password
   jwt.secret.staging=staging_jwt_secret_32_chars_minimum
   ```

3. **Run Command**:
   ```bash
   java -Dapp.env=staging -Dconfig.secure.path=/etc/app/secure.properties -jar school-management.jar
   ```

#### Staging-Specific Settings

- Database: Staging database server
- Email: Staging SMTP server
- CORS: Restricted to staging frontend
- JWT: Production-like token settings
- Scheduler: Production schedule timing

### Production Environment

#### Configuration Steps

1. **System Service Configuration**:
   ```bash
   # /etc/systemd/system/school-management.service
   [Unit]
   Description=School Management System
   After=network.target

   [Service]
   Type=simple
   User=app
   WorkingDirectory=/opt/app
   Environment=APP_ENV=prod
   Environment=CONFIG_SECURE_PATH=/etc/app/secure.properties
   ExecStart=/usr/bin/java -Dapp.env=prod -Dconfig.secure.path=/etc/app/secure.properties -jar school-management.jar
   Restart=always

   [Install]
   WantedBy=multi-user.target
   ```

2. **Secure Configuration**:
   ```properties
   # /etc/app/secure.properties
   db.password.prod=complex_production_database_password
   email.password.prod=production_email_app_password
   jwt.secret.prod=very_long_and_secure_jwt_secret_key_for_production_use_minimum_32_characters
   admin.secret_code.prod=complex_admin_secret_code_for_production
   ```

3. **Security Hardening**:
   ```bash
   # Set secure permissions
   chmod 600 /etc/app/secure.properties
   chown app:app /etc/app/secure.properties
   
   # Restrict application directory
   chmod 755 /opt/app
   chown app:app /opt/app
   ```

#### Production-Specific Settings

- Database: Production database cluster
- Email: Production SMTP service
- CORS: Restricted to production frontend only
- JWT: Secure, long-lived tokens
- Scheduler: Production schedule timing
- Logging: Enhanced security and audit logging

## Deployment Configurations

### Docker Deployment

#### Dockerfile Configuration

```dockerfile
FROM openjdk:11-jre-slim

# Create application user
RUN groupadd -r app && useradd -r -g app app

# Create directories
RUN mkdir -p /opt/app /etc/app
RUN chown app:app /opt/app /etc/app

# Copy application
COPY target/school-management.jar /opt/app/
COPY config/secure.properties /etc/app/

# Set permissions
RUN chmod 600 /etc/app/secure.properties
RUN chown app:app /etc/app/secure.properties

USER app
WORKDIR /opt/app

EXPOSE 8081

CMD ["java", "-Dapp.env=prod", "-Dconfig.secure.path=/etc/app/secure.properties", "-jar", "school-management.jar"]
```

#### Docker Compose Configuration

```yaml
version: '3.8'
services:
  school-management:
    build: .
    ports:
      - "8081:8081"
    environment:
      - APP_ENV=prod
      - CONFIG_SECURE_PATH=/etc/app/secure.properties
    volumes:
      - ./config/secure.properties:/etc/app/secure.properties:ro
    depends_on:
      - database
    restart: unless-stopped

  database:
    image: postgres:13
    environment:
      - POSTGRES_DB=school
      - POSTGRES_USER=school_user
      - POSTGRES_PASSWORD_FILE=/run/secrets/db_password
    secrets:
      - db_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    restart: unless-stopped

secrets:
  db_password:
    file: ./secrets/db_password.txt

volumes:
  postgres_data:
```

### Kubernetes Deployment

#### ConfigMap for Application Properties

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: school-management-config
data:
  app.env: "prod"
  server.port: "8081"
  db.url: "jdbc:postgresql://postgres-service:5432/school"
  db.username: "school_user"
```

#### Secret for Sensitive Configuration

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: school-management-secrets
type: Opaque
stringData:
  secure.properties: |
    db.password.prod=production_database_password
    email.password.prod=production_email_password
    jwt.secret.prod=production_jwt_secret_minimum_32_characters
    admin.secret_code.prod=production_admin_secret
```

#### Deployment Configuration

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: school-management
spec:
  replicas: 3
  selector:
    matchLabels:
      app: school-management
  template:
    metadata:
      labels:
        app: school-management
    spec:
      containers:
      - name: school-management
        image: school-management:latest
        ports:
        - containerPort: 8081
        env:
        - name: APP_ENV
          valueFrom:
            configMapKeyRef:
              name: school-management-config
              key: app.env
        - name: CONFIG_SECURE_PATH
          value: "/etc/app/secure.properties"
        volumeMounts:
        - name: secure-config
          mountPath: /etc/app
          readOnly: true
        livenessProbe:
          httpGet:
            path: /api/health
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /api/ready
            port: 8081
          initialDelaySeconds: 5
          periodSeconds: 5
      volumes:
      - name: secure-config
        secret:
          secretName: school-management-secrets
          items:
          - key: secure.properties
            path: secure.properties
            mode: 0600
```

## Configuration Validation

### Startup Validation

The application performs configuration validation on startup:

1. **Required Properties Check**: Validates that essential properties are present
2. **Database Connection Test**: Verifies database connectivity
3. **Email Configuration Test**: Validates SMTP settings (if enabled)
4. **JWT Secret Validation**: Ensures JWT secret meets minimum requirements
5. **File Path Validation**: Checks that required files exist and are accessible

### Configuration Health Checks

#### Health Check Endpoint

```http
GET /api/health
```

Response includes configuration status:

```json
{
  "status": "healthy",
  "database": "connected",
  "email": "configured",
  "jwt": "valid",
  "scheduler": "running",
  "license": "valid"
}
```

#### Configuration Validation Script

```bash
#!/bin/bash
# validate-config.sh

echo "Validating School Management System Configuration..."

# Check required files
if [ ! -f "application.properties" ]; then
    echo "ERROR: application.properties not found"
    exit 1
fi

if [ ! -f "$CONFIG_SECURE_PATH" ]; then
    echo "WARNING: secure.properties not found at $CONFIG_SECURE_PATH"
fi

# Check database connectivity
java -cp "target/school-management.jar" com.canalprep.util.ConfigValidator

echo "Configuration validation complete"
```

## Troubleshooting

### Common Configuration Issues

#### 1. Database Connection Failures

**Symptoms**:
- Application fails to start
- "Connection refused" errors
- "Authentication failed" errors

**Solutions**:
```properties
# Check database URL format
db.url=jdbc:postgresql://hostname:port/database_name

# Verify credentials in secure.properties
db.password.prod=correct_password

# Test connection manually
psql -h hostname -p port -U username -d database_name
```

#### 2. Email Configuration Issues

**Symptoms**:
- OTP emails not sent
- SMTP authentication failures
- Connection timeout errors

**Solutions**:
```properties
# Verify SMTP settings
email.host=smtp.gmail.com
email.port=587
email.tls.enable=true
email.auth.enable=true

# Check app password (not regular password)
email.password.prod=app_specific_password
```

#### 3. JWT Token Issues

**Symptoms**:
- Authentication failures
- "Invalid token" errors
- Token validation errors

**Solutions**:
```properties
# Ensure JWT secret is long enough (minimum 32 characters)
jwt.secret.prod=your_very_long_jwt_secret_key_minimum_32_characters

# Check token expiration
jwt.expiration_ms=1800000  # 30 minutes
```

#### 4. CORS Configuration Issues

**Symptoms**:
- Frontend cannot connect to API
- "CORS policy" errors in browser
- Preflight request failures

**Solutions**:
```properties
# Match exact frontend origin
cors.allowed_origins.prod=https://your-frontend-domain.com

# Include required headers
cors.allowed_headers=Accept,Authorization,Content-Length,Content-Type,Origin,X-Requested-With

# Allow credentials if needed
cors.allow_credentials=true
```

#### 5. Scheduler Configuration Issues

**Symptoms**:
- Attendance scheduler not running
- Incorrect schedule timing
- Timezone-related issues

**Solutions**:
```properties
# Enable scheduler
attendance.schedule.enabled=true

# Use 24-hour format
attendance.schedule.time=16:30

# Set correct timezone
attendance.schedule.timezone=America/New_York
```

### Configuration Debugging

#### Enable Debug Logging

```properties
# Add to application.properties or secure.properties
logging.level.com.canalprep.config=DEBUG
logging.level.com.canalprep.scheduler=DEBUG
```

#### Configuration Dump

Add this to your application for debugging:

```java
// Debug configuration loading
ConfigLoader.getAllProperties().forEach((key, value) -> {
    if (!key.toString().toLowerCase().contains("password") && 
        !key.toString().toLowerCase().contains("secret")) {
        System.out.println(key + "=" + value);
    }
});
```

### Log Analysis

#### Configuration-Related Log Messages

```
INFO: Configuration loaded successfully. Environment: prod
INFO: Loaded secure configuration from: /etc/app/secure.properties
INFO: Database connection successful!
INFO: AttendanceScheduler initialized - Enabled: true, Daily run time: 16:30
WARNING: No external secure configuration file found. Using internal configuration only.
ERROR: Failed to load secure config from: /etc/app/secure.properties
```

## Best Practices

### Security Best Practices

1. **Separate Sensitive Data**: Never commit passwords or secrets to version control
2. **Use Strong Secrets**: Generate cryptographically secure random secrets
3. **Restrict File Permissions**: Set secure.properties to 600 permissions
4. **Environment Isolation**: Use different secrets for each environment
5. **Regular Rotation**: Rotate secrets and passwords regularly
6. **Audit Access**: Monitor access to configuration files

### Configuration Management Best Practices

1. **Environment Parity**: Keep environments as similar as possible
2. **Configuration Validation**: Validate configuration on startup
3. **Default Values**: Provide sensible defaults for non-critical settings
4. **Documentation**: Document all configuration options and their purposes
5. **Version Control**: Track configuration changes in version control
6. **Backup**: Backup configuration files regularly

### Deployment Best Practices

1. **Automated Deployment**: Use automated deployment pipelines
2. **Configuration Templates**: Use templates for environment-specific configs
3. **Health Checks**: Implement comprehensive health checks
4. **Rollback Strategy**: Have a rollback plan for configuration changes
5. **Monitoring**: Monitor configuration-related metrics and alerts
6. **Testing**: Test configuration changes in staging before production

### Maintenance Best Practices

1. **Regular Reviews**: Review configuration settings regularly
2. **Security Audits**: Conduct security audits of configuration
3. **Performance Tuning**: Monitor and tune performance-related settings
4. **Capacity Planning**: Plan for growth in configuration requirements
5. **Documentation Updates**: Keep documentation current with changes
6. **Training**: Train team members on configuration management

---

*Last updated: January 2025*
*Version: 1.0*
*For technical support, refer to the troubleshooting section or contact the development team.*
# School Management System - Master Configuration Guide

## Document Information

- **Version**: 1.0
- **Last Updated**: January 2025
- **Maintained By**: Development Team
- **Review Cycle**: Quarterly

## Table of Contents

1. [Overview](#overview)
2. [Quick Start Guide](#quick-start-guide)
3. [Configuration Architecture](#configuration-architecture)
4. [Configuration Files Reference](#configuration-files-reference)
   - 4.1. [application.properties](#applicationproperties)
   - 4.2. [secure.properties](#secureproperties)
   - 4.3. [Environment-Specific Overrides](#environment-specific-overrides)
5. [Environment Configuration](#environment-configuration)
   - 5.1. [Development Environment](#development-environment)
   - 5.2. [Staging Environment](#staging-environment)
   - 5.3. [Production Environment](#production-environment)
6. [Property Categories](#property-categories)
   - 6.1. [Server Configuration](#server-configuration)
   - 6.2. [Database Configuration](#database-configuration)
   - 6.3. [Security Configuration](#security-configuration)
   - 6.4. [Email/SMTP Configuration](#emailsmtp-configuration)
   - 6.5. [Authentication Configuration](#authentication-configuration)
   - 6.6. [Attendance Configuration](#attendance-configuration)
   - 6.7. [API Configuration](#api-configuration)
   - 6.8. [UI Configuration](#ui-configuration)
7. [Configuration Loading Process](#configuration-loading-process)
8. [Property Override Rules](#property-override-rules)
9. [Security Best Practices](#security-best-practices)
10. [Deployment Configurations](#deployment-configurations)
11. [Troubleshooting](#troubleshooting)
12. [Cross-References](#cross-references)
13. [Version History](#version-history)

## Overview

The School Management System uses a hierarchical configuration system that supports environment-specific overrides, secure property handling, and runtime configuration updates. This master guide provides comprehensive documentation for all configuration aspects of the system.

### Key Features

- **Hierarchical Configuration**: Properties can be overridden at multiple levels
- **Environment-Specific Settings**: Different configurations for dev, staging, and production
- **Secure Property Handling**: Sensitive data stored separately from main configuration
- **Runtime Updates**: Some properties can be updated without system restart
- **Validation**: Built-in configuration validation and error reporting

### Configuration Philosophy

The system follows these configuration principles:

1. **Security First**: Sensitive data never stored in version control
2. **Environment Parity**: Consistent configuration structure across environments
3. **Explicit Defaults**: All properties have documented default values
4. **Fail Fast**: Invalid configurations cause startup failures
5. **Auditability**: Configuration changes are logged and tracked

## Quick Start Guide

### Minimum Required Configuration

For a basic development setup, you need:

```properties
# application.properties (minimum)
server.port=8081
db.url=jdbc:postgresql://localhost:5432/canal_prep_school_clone
db.username=postgres
db.password=123
```

### Production Setup Checklist

- [ ] Create secure.properties with production credentials
- [ ] Set appropriate file permissions (600)
- [ ] Configure environment-specific overrides
- [ ] Enable security features (CSRF, JWT, etc.)
- [ ] Set up monitoring and logging
- [ ] Configure backup and recovery

### Common Configuration Tasks

1. **Change Database Connection**:
   ```properties
   db.url=jdbc:postgresql://new-host:5432/database
   db.username=new_user
   # Add password to secure.properties
   ```

2. **Enable Email Notifications**:
   ```properties
   email.enabled=true
   email.smtp.host=smtp.gmail.com
   email.smtp.port=587
   # Add credentials to secure.properties
   ```

3. **Configure Attendance Scheduler**:
   ```properties
   attendance.schedule.enabled=true
   attendance.schedule.time=16:30
   attendance.notifications.parents=true
   ```

## Configuration Architecture

### Configuration Loading Hierarchy

```
┌─────────────────────────────────────────────────────────────────┐
│                    Configuration Loading Order                   │
│                         (Highest Priority)                      │
├─────────────────────────────────────────────────────────────────┤
│  1. System Properties (-Dproperty=value)                       │
├─────────────────────────────────────────────────────────────────┤
│  2. Environment Variables                                       │
├─────────────────────────────────────────────────────────────────┤
│  3. External Secure Configuration (secure.properties)          │
├─────────────────────────────────────────────────────────────────┤
│  4. Environment-Specific Properties (.dev, .staging, .prod)    │
├─────────────────────────────────────────────────────────────────┤
│  5. Base Application Properties (application.properties)       │
├─────────────────────────────────────────────────────────────────┤
│  6. Default Values (hardcoded in application)                  │
│                         (Lowest Priority)                       │
└─────────────────────────────────────────────────────────────────┘
```

### Configuration Components

- **ConfigLoader**: Central configuration management class
- **PropertyValidator**: Validates configuration values and types
- **EnvironmentDetector**: Determines current environment
- **SecurePropertyHandler**: Manages sensitive configuration data

### File Locations

| File Type | Development | Staging | Production |
|-----------|-------------|---------|------------|
| application.properties | `./application.properties` | `/opt/app/config/application.properties` | `/opt/app/config/application.properties` |
| secure.properties | `./config/secure.properties` | `/etc/app/secure.properties` | `/etc/app/secure.properties` |
| Log Files | `./logs/` | `/var/log/app/` | `/var/log/app/` |
| Upload Directory | `./uploads/` | `/opt/app/uploads/` | `/opt/app/uploads/` |

## Configuration Files Reference

### application.properties

**Purpose**: Main configuration file containing all non-sensitive application settings.

**Location**: 
- Development: `./application.properties`
- Production: `/opt/app/config/application.properties`

**Key Sections**:
- Server and network settings
- Database connection parameters (non-sensitive)
- Feature toggles and operational settings
- UI and API configuration
- Scheduler settings

**Cross-Reference**: See [Configuration Guide](06-Configuration/CONFIGURATION_GUIDE.md) for detailed property descriptions.

### secure.properties

**Purpose**: Contains sensitive configuration data that should never be committed to version control.

**Location Search Order**:
1. System property: `config.secure.path`
2. Environment variable: `CONFIG_SECURE_PATH`
3. `./config/secure.properties`
4. `/etc/app/secure.properties`
5. `config/secure.properties`

**Required Properties**:
```properties
# Database credentials
db.password=your_secure_password

# JWT secret
jwt.secret=your_jwt_secret_key_minimum_256_bits

# Email credentials
email.smtp.username=your_email@example.com
email.smtp.password=your_email_password

# External API keys
sms.api.key=your_sms_api_key
```

**Security Requirements**:
- File permissions: 600 (owner read/write only)
- Never commit to version control
- Regular rotation of secrets
- Encrypted storage in production

**Cross-Reference**: See [Security Configuration](06-Configuration/SECURITY_CONFIGURATION.md) for complete security setup.

### Environment-Specific Overrides

**Pattern**: `property.name.environment`

**Supported Environments**:
- `dev` - Development
- `staging` - Staging/Testing
- `prod` - Production

**Examples**:
```properties
# Base property
db.url=jdbc:postgresql://localhost:5432/canal_prep_school_clone

# Environment overrides
db.url.dev=jdbc:postgresql://localhost:5432/canal_prep_school_dev
db.url.staging=jdbc:postgresql://staging-db:5432/canal_prep_school_staging
db.url.prod=jdbc:postgresql://prod-db:5432/canal_prep_school_prod
```

## Environment Configuration

### Development Environment

**Purpose**: Local development and testing

**Key Characteristics**:
- Relaxed security settings
- Debug logging enabled
- Local database connections
- Permissive CORS settings
- Disabled external integrations

**Configuration Example**:
```properties
# Environment detection
app.env=dev

# Server settings
server.port=8081
server.bind_address=localhost

# Database (local)
db.url=jdbc:postgresql://localhost:5432/canal_prep_school_dev
db.username=postgres

# Security (relaxed)
csrf.enabled=false
cors.allowed_origins=*
jwt.expiration_ms=3600000

# Features (debug mode)
debug.enabled=true
logging.level=DEBUG
email.enabled=false
```

**Setup Instructions**:
1. Install PostgreSQL locally
2. Create development database
3. Copy `application.properties.example` to `application.properties`
4. Set development-specific values
5. Run: `java -Dapp.env=dev -jar target/school-management.jar`

**Cross-Reference**: See [Environment Deployment Guide](06-Configuration/ENVIRONMENT_DEPLOYMENT_GUIDE.md#development-environment) for detailed setup.

### Staging Environment

**Purpose**: Pre-production testing and validation

**Key Characteristics**:
- Production-like security
- Enhanced logging
- Staging database connections
- Restricted CORS settings
- Test mode for external integrations

**Configuration Example**:
```properties
# Environment detection
app.env=staging

# Server settings
server.port=8080
server.bind_address=0.0.0.0

# Database (staging)
db.url=jdbc:postgresql://staging-db:5432/canal_prep_school_staging
db.username=app_user

# Security (production-like)
csrf.enabled=true
cors.allowed_origins=https://staging.canalprep.edu
jwt.expiration_ms=1800000

# Features (test mode)
debug.enabled=false
logging.level=INFO
email.enabled=true
email.test_mode=true
```

**Setup Instructions**:
1. Deploy to staging server
2. Configure staging database
3. Set up secure.properties with staging credentials
4. Configure reverse proxy (nginx)
5. Run: `java -Dapp.env=staging -jar school-management.jar`

**Cross-Reference**: See [Environment Deployment Guide](06-Configuration/ENVIRONMENT_DEPLOYMENT_GUIDE.md#staging-environment) for detailed setup.

### Production Environment

**Purpose**: Live production system

**Key Characteristics**:
- Maximum security settings
- Audit logging
- Production database connections
- Strict CORS settings
- Full external integrations

**Configuration Example**:
```properties
# Environment detection
app.env=prod

# Server settings
server.port=8080
server.bind_address=0.0.0.0

# Database (production)
db.url=jdbc:postgresql://prod-db:5432/canal_prep_school_prod
db.username=app_user

# Security (maximum)
csrf.enabled=true
cors.allowed_origins=https://canalprep.edu
jwt.expiration_ms=1800000
auth.max_login_attempts=3

# Features (production)
debug.enabled=false
logging.level=WARN
email.enabled=true
audit.enabled=true
```

**Setup Instructions**:
1. Deploy to production server
2. Configure production database with replication
3. Set up secure.properties with production credentials
4. Configure SSL/TLS certificates
5. Set up monitoring and alerting
6. Run: `systemctl start school-management`

**Cross-Reference**: See [Environment Deployment Guide](06-Configuration/ENVIRONMENT_DEPLOYMENT_GUIDE.md#production-environment) for detailed setup.

## Property Categories

### Server Configuration

**Properties**:
- `server.port` - HTTP server port (default: 8081)
- `server.bind_address` - Server bind address (default: 0.0.0.0)
- `server.max_threads` - Maximum thread pool size (default: 200)
- `server.timeout_ms` - Request timeout in milliseconds (default: 30000)

**Environment Variations**:
```properties
# Development
server.port=8081
server.bind_address=localhost

# Staging
server.port=8080
server.bind_address=0.0.0.0

# Production
server.port=8080
server.bind_address=0.0.0.0
server.max_threads=500
```

**Cross-Reference**: [Applications Guide](07-Applications/APPLICATIONS_GUIDE.md#mainapp---application-bootstrap)

### Database Configuration

**Properties**:
- `db.driver` - JDBC driver class (default: org.postgresql.Driver)
- `db.url` - Database connection URL
- `db.username` - Database username
- `db.password` - Database password (secure.properties)
- `db.pool.max_size` - Connection pool size (default: 20)

**Environment Variations**:
```properties
# Development
db.url=jdbc:postgresql://localhost:5432/canal_prep_school_dev
db.username=postgres
db.pool.max_size=5

# Staging
db.url=jdbc:postgresql://staging-db:5432/canal_prep_school_staging
db.username=app_user
db.pool.max_size=10

# Production
db.url=jdbc:postgresql://prod-db:5432/canal_prep_school_prod
db.username=app_user
db.pool.max_size=50
```

**Security Notes**:
- Never store passwords in application.properties
- Use connection pooling in production
- Enable SSL for database connections

**Cross-Reference**: [Technical Documentation](TECHNICAL_DOCUMENTATION.md#data-access-layer)

### Security Configuration

**Properties**:
- `csrf.enabled` - Enable CSRF protection (default: true)
- `cors.enabled` - Enable CORS (default: true)
- `cors.allowed_origins` - Allowed CORS origins
- `jwt.secret` - JWT signing secret (secure.properties)
- `jwt.expiration_ms` - JWT token expiration (default: 1800000)

**Environment Variations**:
```properties
# Development
csrf.enabled=false
cors.allowed_origins=*
jwt.expiration_ms=3600000

# Staging
csrf.enabled=true
cors.allowed_origins=https://staging.canalprep.edu
jwt.expiration_ms=1800000

# Production
csrf.enabled=true
cors.allowed_origins=https://canalprep.edu
jwt.expiration_ms=1800000
```

**Cross-Reference**: [Security Configuration](06-Configuration/SECURITY_CONFIGURATION.md)

### Email/SMTP Configuration

**Properties**:
- `email.enabled` - Enable email functionality (default: false)
- `email.smtp.host` - SMTP server host
- `email.smtp.port` - SMTP server port (default: 587)
- `email.smtp.username` - SMTP username (secure.properties)
- `email.smtp.password` - SMTP password (secure.properties)
- `email.smtp.tls` - Enable TLS (default: true)

**Environment Variations**:
```properties
# Development
email.enabled=false

# Staging
email.enabled=true
email.smtp.host=smtp.gmail.com
email.smtp.port=587
email.test_mode=true

# Production
email.enabled=true
email.smtp.host=smtp.canalprep.edu
email.smtp.port=587
email.test_mode=false
```

**Cross-Reference**: [Applications Guide](07-Applications/APPLICATIONS_GUIDE.md#email-configuration)

### Authentication Configuration

**Properties**:
- `auth.max_login_attempts` - Maximum login attempts (default: 5)
- `auth.lockout_duration_minutes` - Account lockout duration (default: 15)
- `session.timeout_minutes` - Session timeout (default: 30)
- `otp.enabled` - Enable OTP functionality (default: true)
- `otp.expiry_minutes` - OTP expiration time (default: 10)

**Environment Variations**:
```properties
# Development
auth.max_login_attempts=10
auth.lockout_duration_minutes=5
session.timeout_minutes=60

# Staging
auth.max_login_attempts=5
auth.lockout_duration_minutes=15
session.timeout_minutes=30

# Production
auth.max_login_attempts=3
auth.lockout_duration_minutes=30
session.timeout_minutes=30
```

**Cross-Reference**: [Applications Guide](07-Applications/APPLICATIONS_GUIDE.md#authentication--authorization)

### Attendance Configuration

**Properties**:
- `attendance.schedule.enabled` - Enable attendance scheduler (default: true)
- `attendance.schedule.time` - Daily processing time (default: 16:30)
- `attendance.auto_mark_absent` - Auto-mark absent students (default: true)
- `attendance.notifications.parents` - Send parent notifications (default: true)

**Environment Variations**:
```properties
# Development
attendance.schedule.enabled=true
attendance.schedule.time=*/5 * * * *  # Every 5 minutes for testing
attendance.notifications.parents=false

# Staging
attendance.schedule.enabled=true
attendance.schedule.time=16:30
attendance.notifications.parents=true
attendance.notifications.test_mode=true

# Production
attendance.schedule.enabled=true
attendance.schedule.time=16:30
attendance.notifications.parents=true
```

**Cross-Reference**: [Applications Guide](07-Applications/APPLICATIONS_GUIDE.md#attendance-management)

### API Configuration

**Properties**:
- `api.enabled` - Enable API endpoints (default: true)
- `api.rate_limit.enabled` - Enable rate limiting (default: true)
- `api.rate_limit.requests_per_minute` - Rate limit (default: 100)
- `api.documentation.enabled` - Enable API docs (default: false)

**Environment Variations**:
```properties
# Development
api.enabled=true
api.rate_limit.enabled=false
api.documentation.enabled=true

# Staging
api.enabled=true
api.rate_limit.enabled=true
api.rate_limit.requests_per_minute=200
api.documentation.enabled=true

# Production
api.enabled=true
api.rate_limit.enabled=true
api.rate_limit.requests_per_minute=100
api.documentation.enabled=false
```

### UI Configuration

**Properties**:
- `ui.enabled` - Enable web UI (default: true)
- `ui.theme` - Default theme (default: default)
- `ui.language` - Default language (default: en)
- `ui.features.enabled` - Enabled UI features

**Environment Variations**:
```properties
# Development
ui.enabled=true
ui.theme=default
ui.debug_mode=true

# Staging
ui.enabled=true
ui.theme=default
ui.debug_mode=false

# Production
ui.enabled=true
ui.theme=default
ui.debug_mode=false
ui.minified=true
```

## Configuration Loading Process

### Startup Sequence

1. **Initialize ConfigLoader**
   - Load default properties
   - Detect environment (dev/staging/prod)

2. **Load Base Configuration**
   - Read application.properties
   - Apply property validation

3. **Load Secure Configuration**
   - Search for secure.properties in predefined locations
   - Merge with base configuration

4. **Apply Environment Overrides**
   - Look for environment-specific properties
   - Override base values with environment-specific ones

5. **System Property Overrides**
   - Apply command-line system properties
   - Apply environment variables

6. **Validation and Finalization**
   - Validate all required properties
   - Log configuration summary
   - Make configuration available to application

### Configuration Validation

**Validation Rules**:
- Required properties must be present
- Numeric properties must be valid numbers
- Boolean properties must be true/false
- URL properties must be valid URLs
- File path properties must be accessible

**Validation Errors**:
```
Configuration validation failed:
- Missing required property: db.password
- Invalid numeric value for server.port: 'abc'
- Invalid URL format for db.url: 'invalid-url'
```

## Property Override Rules

### Override Priority (Highest to Lowest)

1. **System Properties** (`-Dproperty=value`)
2. **Environment Variables** (`PROPERTY_NAME=value`)
3. **Secure Properties** (`secure.properties`)
4. **Environment-Specific Properties** (`property.name.env`)
5. **Base Properties** (`application.properties`)
6. **Default Values** (hardcoded)

### Override Examples

**Base Configuration**:
```properties
# application.properties
server.port=8081
db.url=jdbc:postgresql://localhost:5432/canal_prep_school_clone
```

**Environment Override**:
```properties
# application.properties
server.port.prod=8080
db.url.prod=jdbc:postgresql://prod-db:5432/canal_prep_school_prod
```

**System Property Override**:
```bash
java -Dserver.port=9090 -jar school-management.jar
```

**Final Result** (in production):
- `server.port` = 9090 (system property wins)
- `db.url` = jdbc:postgresql://prod-db:5432/canal_prep_school_prod (environment override)

### Environment Variable Mapping

**Convention**: Convert property names to uppercase and replace dots with underscores.

**Examples**:
- `server.port` → `SERVER_PORT`
- `db.url` → `DB_URL`
- `jwt.expiration_ms` → `JWT_EXPIRATION_MS`

**Usage**:
```bash
export SERVER_PORT=8080
export DB_URL="jdbc:postgresql://prod-db:5432/canal_prep_school_prod"
java -jar school-management.jar
```

## Security Best Practices

### Secure Property Management

1. **Never Commit Secrets**
   - Add secure.properties to .gitignore
   - Use environment variables in CI/CD
   - Rotate secrets regularly

2. **File Permissions**
   ```bash
   chmod 600 /etc/app/secure.properties
   chown app:app /etc/app/secure.properties
   ```

3. **Secret Rotation**
   - JWT secrets: Every 90 days
   - Database passwords: Every 180 days
   - API keys: As required by provider

4. **Encryption at Rest**
   - Use encrypted storage for secure.properties
   - Consider using secret management systems (HashiCorp Vault, AWS Secrets Manager)

### Configuration Security Checklist

- [ ] Secure properties file has correct permissions (600)
- [ ] No secrets in version control
- [ ] JWT secret is at least 256 bits
- [ ] Database connections use SSL
- [ ] CSRF protection enabled in production
- [ ] CORS origins restricted in production
- [ ] Rate limiting enabled
- [ ] Audit logging configured

### Environment-Specific Security

**Development**:
- Relaxed settings for ease of development
- Local-only access
- Debug information enabled

**Staging**:
- Production-like security
- Test mode for external services
- Enhanced logging for debugging

**Production**:
- Maximum security settings
- Audit logging
- Monitoring and alerting
- Regular security updates

## Deployment Configurations

### Docker Configuration

**Dockerfile Example**:
```dockerfile
FROM openjdk:11-jre-slim

# Create app user
RUN useradd -r -s /bin/false app

# Copy application
COPY target/school-management.jar /opt/app/
COPY application.properties /opt/app/config/

# Set permissions
RUN chown -R app:app /opt/app
RUN chmod 600 /opt/app/config/application.properties

USER app
WORKDIR /opt/app

EXPOSE 8080
CMD ["java", "-jar", "school-management.jar"]
```

**Docker Compose Example**:
```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - APP_ENV=prod
      - CONFIG_SECURE_PATH=/etc/app/secure.properties
    volumes:
      - ./secure.properties:/etc/app/secure.properties:ro
      - ./logs:/var/log/app
    depends_on:
      - database
  
  database:
    image: postgres:13
    environment:
      - POSTGRES_DB=canal_prep_school_prod
      - POSTGRES_USER=app_user
      - POSTGRES_PASSWORD_FILE=/run/secrets/db_password
    secrets:
      - db_password
    volumes:
      - postgres_data:/var/lib/postgresql/data

secrets:
  db_password:
    file: ./secrets/db_password.txt

volumes:
  postgres_data:
```

### Kubernetes Configuration

**ConfigMap Example**:
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
data:
  application.properties: |
    server.port=8080
    server.bind_address=0.0.0.0
    db.url=jdbc:postgresql://postgres-service:5432/canal_prep_school_prod
    db.username=app_user
    app.env=prod
```

**Secret Example**:
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: app-secrets
type: Opaque
data:
  db.password: <base64-encoded-password>
  jwt.secret: <base64-encoded-jwt-secret>
```

**Deployment Example**:
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
      - name: app
        image: school-management:latest
        ports:
        - containerPort: 8080
        env:
        - name: CONFIG_SECURE_PATH
          value: "/etc/app/secure.properties"
        volumeMounts:
        - name: config
          mountPath: /opt/app/config
        - name: secrets
          mountPath: /etc/app
          readOnly: true
      volumes:
      - name: config
        configMap:
          name: app-config
      - name: secrets
        secret:
          secretName: app-secrets
          defaultMode: 0600
```

### Systemd Service Configuration

**Service File** (`/etc/systemd/system/school-management.service`):
```ini
[Unit]
Description=School Management System
After=network.target postgresql.service

[Service]
Type=simple
User=app
Group=app
WorkingDirectory=/opt/app
ExecStart=/usr/bin/java -Xmx4g -Dapp.env=prod -Dconfig.secure.path=/etc/app/secure.properties -jar school-management.jar
Restart=always
RestartSec=10
Environment=JAVA_HOME=/usr/lib/jvm/java-11-openjdk

# Security settings
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true
ReadWritePaths=/opt/app/logs /opt/app/uploads

[Install]
WantedBy=multi-user.target
```

**Service Management**:
```bash
# Enable and start service
sudo systemctl enable school-management
sudo systemctl start school-management

# Check status
sudo systemctl status school-management

# View logs
sudo journalctl -u school-management -f
```

## Troubleshooting

### Common Configuration Issues

#### 1. Configuration File Not Found

**Symptoms**:
- Application fails to start
- "Configuration file not found" error

**Diagnosis**:
```bash
# Check file existence
ls -la /opt/app/config/application.properties
ls -la /etc/app/secure.properties

# Check file permissions
stat /etc/app/secure.properties
```

**Solutions**:
- Verify file paths are correct
- Check file permissions (should be 600 for secure.properties)
- Ensure files are readable by application user

#### 2. Property Override Not Working

**Symptoms**:
- Environment-specific properties not applied
- System properties ignored

**Diagnosis**:
```bash
# Check environment detection
grep "Environment detected" /var/log/app/application.log

# Verify property loading order
grep "Loading configuration" /var/log/app/application.log
```

**Solutions**:
- Verify environment detection is working
- Check property naming convention (property.name.env)
- Ensure system properties are passed correctly

#### 3. Database Connection Issues

**Symptoms**:
- "Connection refused" errors
- Authentication failures

**Diagnosis**:
```bash
# Test database connectivity
psql -h database-host -U username -d database_name

# Check configuration
grep -i "db\." /opt/app/config/application.properties
grep -i "db\." /etc/app/secure.properties
```

**Solutions**:
- Verify database server is running
- Check connection parameters
- Validate credentials in secure.properties

#### 4. JWT Token Issues

**Symptoms**:
- Authentication failures
- "Invalid token" errors

**Diagnosis**:
```bash
# Check JWT configuration
grep -i "jwt" /etc/app/secure.properties

# Verify token format
echo "JWT_TOKEN" | base64 -d
```

**Solutions**:
- Ensure JWT secret is at least 256 bits
- Check token expiration settings
- Verify secret is properly configured

#### 5. Email Configuration Problems

**Symptoms**:
- Emails not being sent
- SMTP authentication failures

**Diagnosis**:
```bash
# Test SMTP connectivity
telnet smtp.gmail.com 587

# Check email configuration
grep -i "email\|smtp" /opt/app/config/application.properties
```

**Solutions**:
- Verify SMTP server settings
- Check credentials in secure.properties
- Test with email provider's settings

### Configuration Validation Tools

#### Built-in Validation

```bash
# Validate configuration before startup
java -jar school-management.jar --validate-config

# Check specific property
java -jar school-management.jar --check-property db.url
```

#### Manual Validation Scripts

**validate-config.sh**:
```bash
#!/bin/bash

CONFIG_FILE="/opt/app/config/application.properties"
SECURE_FILE="/etc/app/secure.properties"

echo "Validating configuration..."

# Check required files exist
if [ ! -f "$CONFIG_FILE" ]; then
    echo "ERROR: Configuration file not found: $CONFIG_FILE"
    exit 1
fi

if [ ! -f "$SECURE_FILE" ]; then
    echo "ERROR: Secure configuration file not found: $SECURE_FILE"
    exit 1
fi

# Check file permissions
SECURE_PERMS=$(stat -c "%a" "$SECURE_FILE")
if [ "$SECURE_PERMS" != "600" ]; then
    echo "WARNING: Secure file permissions should be 600, found: $SECURE_PERMS"
fi

# Check required properties
REQUIRED_PROPS=("db.url" "db.username" "server.port")
for prop in "${REQUIRED_PROPS[@]}"; do
    if ! grep -q "^$prop=" "$CONFIG_FILE"; then
        echo "ERROR: Required property missing: $prop"
        exit 1
    fi
done

echo "Configuration validation passed!"
```

### Monitoring Configuration Changes

#### Configuration Change Detection

```bash
# Monitor configuration files for changes
inotifywait -m /opt/app/config/application.properties /etc/app/secure.properties \
  --format '%w%f %e %T' --timefmt '%Y-%m-%d %H:%M:%S' \
  -e modify,move,create,delete
```

#### Audit Logging

Enable configuration change auditing:
```properties
# application.properties
audit.enabled=true
audit.config_changes=true
audit.log_file=/var/log/app/audit.log
```

## Cross-References

### Related Documentation

- **[Configuration Guide](06-Configuration/CONFIGURATION_GUIDE.md)**: Detailed property descriptions
- **[Security Configuration](06-Configuration/SECURITY_CONFIGURATION.md)**: Security-specific settings
- **[Environment Deployment Guide](06-Configuration/ENVIRONMENT_DEPLOYMENT_GUIDE.md)**: Environment setup procedures
- **[Applications Guide](07-Applications/APPLICATIONS_GUIDE.md)**: Application-specific configuration
- **[Technical Documentation](TECHNICAL_DOCUMENTATION.md)**: System architecture and components

### Configuration Dependencies

```
┌─────────────────────────────────────────────────────────────────┐
│                    Configuration Dependencies                    │
├─────────────────────────────────────────────────────────────────┤
│  Database Config ──→ Connection Pool ──→ Application Startup    │
│       ↓                                                         │
│  Security Config ──→ Authentication ──→ API Access             │
│       ↓                                                         │
│  Email Config ──────→ Notifications ──→ User Communications    │
│       ↓                                                         │
│  Scheduler Config ──→ Background Tasks ──→ System Operations   │
└─────────────────────────────────────────────────────────────────┘
```

### Property Relationships

| Primary Property | Dependent Properties | Impact |
|------------------|---------------------|---------|
| `db.url` | `db.username`, `db.password` | Database connectivity |
| `email.enabled` | `email.smtp.*` | Email functionality |
| `jwt.secret` | `jwt.expiration_ms` | Authentication system |
| `attendance.schedule.enabled` | `attendance.schedule.time` | Automated attendance |
| `csrf.enabled` | `cors.allowed_origins` | Web security |

### Environment Property Matrix

| Property | Development | Staging | Production | Notes |
|----------|-------------|---------|------------|-------|
| `debug.enabled` | true | false | false | Debug logging |
| `csrf.enabled` | false | true | true | CSRF protection |
| `email.enabled` | false | true | true | Email functionality |
| `audit.enabled` | false | true | true | Audit logging |
| `rate_limit.enabled` | false | true | true | API rate limiting |

## Version History

### Version 1.0 (January 2025)
- Initial comprehensive configuration guide
- Complete property documentation
- Environment-specific configurations
- Security best practices
- Deployment configurations
- Troubleshooting guide

### Planned Updates

- **Version 1.1**: Add Kubernetes configuration examples
- **Version 1.2**: Include monitoring and alerting configuration
- **Version 1.3**: Add configuration migration tools
- **Version 1.4**: Include performance tuning guidelines

---

**Document Maintenance**:
- Review quarterly for accuracy
- Update with new features and properties
- Validate examples with each release
- Gather feedback from deployment teams

**Contact Information**:
- Technical Questions: development-team@canalprep.edu
- Security Issues: security@canalprep.edu
- Documentation Updates: docs@canalprep.edu

*This document is part of the School Management System documentation suite. For the most current version, refer to the project repository.*
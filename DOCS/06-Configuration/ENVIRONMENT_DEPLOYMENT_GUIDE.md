# Environment-Specific Deployment Guide

## Table of Contents

1. [Overview](#overview)
2. [Development Environment](#development-environment)
3. [Staging Environment](#staging-environment)
4. [Production Environment](#production-environment)
5. [Environment Comparison](#environment-comparison)
6. [Migration Between Environments](#migration-between-environments)
7. [Troubleshooting](#troubleshooting)

## Overview

This guide provides detailed instructions for deploying the School Management System across different environments. Each environment has specific requirements, security considerations, and deployment procedures.

### Environment Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Development   │    │     Staging     │    │   Production    │
│                 │    │                 │    │                 │
│ • Local DB      │    │ • Staging DB    │    │ • Prod DB       │
│ • Test SMTP     │    │ • Staging SMTP  │    │ • Prod SMTP     │
│ • Permissive    │    │ • Restricted    │    │ • Secure        │
│ • Debug Logs    │    │ • Audit Logs    │    │ • Audit Logs    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Development Environment

### Prerequisites

#### System Requirements
- **Java**: OpenJDK 11 or higher
- **Database**: PostgreSQL 12+ (local instance)
- **Memory**: Minimum 2GB RAM
- **Storage**: 5GB free space
- **Network**: Internet access for dependencies

#### Software Installation

**Windows:**
```powershell
# Install Java (using Chocolatey)
choco install openjdk11

# Install PostgreSQL
choco install postgresql

# Verify installations
java -version
psql --version
```

**Linux/macOS:**
```bash
# Install Java
sudo apt-get install openjdk-11-jdk  # Ubuntu/Debian
brew install openjdk@11             # macOS

# Install PostgreSQL
sudo apt-get install postgresql postgresql-contrib  # Ubuntu/Debian
brew install postgresql                             # macOS

# Verify installations
java -version
psql --version
```

### Database Setup

#### 1. Create Development Database

```sql
-- Connect as postgres user
sudo -u postgres psql

-- Create database and user
CREATE DATABASE canal_prep_school_clone;
CREATE USER postgres WITH PASSWORD '123';
GRANT ALL PRIVILEGES ON DATABASE canal_prep_school_clone TO postgres;

-- Exit psql
\q
```

#### 2. Initialize Database Schema

```bash
# Run database initialization scripts (if available)
psql -h localhost -U postgres -d canal_prep_school_clone -f scripts/init-db.sql

# Or let the application create tables on first run
```

### Configuration Setup

#### 1. Application Properties

Create or verify `src/main/resources/application.properties`:

```properties
# Development Environment Configuration
app.env=dev

# API Configuration
api.base_url=http://localhost:8081/api

# Admin Configuration
admin.email=dev-admin@example.com
admin.secret_code=dev123

# Database Configuration
db.driver=org.postgresql.Driver
db.url=jdbc:postgresql://localhost:5432/canal_prep_school_clone
db.username=postgres
db.password=123

# Email Configuration (Development - Optional)
email.host=smtp.gmail.com
email.port=587
email.username=dev-test@gmail.com
email.password=
email.from_address=dev-test@gmail.com
email.auth.enable=true
email.tls.enable=true

# JWT Configuration
jwt.secret=development-jwt-secret-key-for-testing-only-not-for-production-use-must-be-at-least-32-chars
jwt.expiration_ms=1800000

# Server Configuration
server.bind_address=0.0.0.0
server.port=8081

# CORS Configuration (Permissive for development)
cors.allow_credentials=true
cors.allowed_headers=Accept,Authorization,Content-Length,Content-Type,Origin,X-Requested-With,ngrok-skip-browser-warning
cors.allowed_methods=DELETE,GET,OPTIONS,POST,PUT
cors.allowed_origins=*

# CSRF Configuration
security.csrf.allowed_origin=http://localhost:5173

# UI Configuration
ui.base_url=http://localhost:5173

# Attendance Scheduler
attendance.schedule.time=16:30
attendance.schedule.enabled=true
attendance.schedule.timezone=UTC

# Bulk Upload
bulk_upload.template_path=student_batch_template.xlsx

# OTP Configuration
otp.operations.allowed=ADMIN_CREATION,PASSWORD_RESET,REMOVE,RENEW

# NGROK Support
ngrok.allowed_header=ngrok-skip-browser-warning
```

#### 2. Optional: Secure Properties for Development

Create `config/secure.properties` (optional for development):

```properties
# Development Secure Configuration (Optional)
email.password.dev=your_gmail_app_password
```

### Build and Deployment

#### 1. Build Application

```bash
# Clean and build
mvn clean compile package -DskipTests

# Or with tests
mvn clean compile package
```

#### 2. Run Application

```bash
# Method 1: Direct JAR execution
java -Dapp.env=dev -jar target/school-management.jar 8081

# Method 2: With external secure config
java -Dapp.env=dev -Dconfig.secure.path=config/secure.properties -jar target/school-management.jar 8081

# Method 3: Using Maven (for development)
mvn exec:java -Dexec.mainClass="com.canalprep.MainApp" -Dexec.args="8081"
```

#### 3. Verify Deployment

```bash
# Check application health
curl http://localhost:8081/api/health

# Check specific endpoints
curl http://localhost:8081/api/auth/status
```

### Development Workflow

#### 1. Code Changes

```bash
# Make code changes
# Rebuild application
mvn compile package -DskipTests

# Restart application
# Kill existing process (Ctrl+C)
java -Dapp.env=dev -jar target/school-management.jar 8081
```

#### 2. Database Changes

```bash
# Apply database migrations
psql -h localhost -U postgres -d canal_prep_school_clone -f migrations/001_new_feature.sql

# Or use application-managed migrations
```

#### 3. Frontend Integration

```bash
# Start frontend development server (if applicable)
cd frontend
npm install
npm run dev

# Frontend typically runs on http://localhost:5173
# Backend API available at http://localhost:8081/api
```

### Development Tools

#### 1. Database Management

```bash
# Connect to development database
psql -h localhost -U postgres -d canal_prep_school_clone

# Common queries
\dt                    # List tables
\d table_name         # Describe table
SELECT * FROM users;  # Query data
```

#### 2. Log Monitoring

```bash
# View application logs (if logging to file)
tail -f logs/application.log

# Or monitor console output
```

#### 3. API Testing

```bash
# Test authentication
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'

# Test with authentication token
curl -X GET http://localhost:8081/api/students \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Staging Environment

### Prerequisites

#### System Requirements
- **Java**: OpenJDK 11 or higher
- **Database**: PostgreSQL 12+ (dedicated staging server)
- **Memory**: Minimum 4GB RAM
- **Storage**: 20GB free space
- **Network**: Secure network access, SSL certificates

#### Infrastructure Setup

**Linux Server (Ubuntu/CentOS):**
```bash
# Update system
sudo apt-get update && sudo apt-get upgrade -y

# Install Java
sudo apt-get install openjdk-11-jdk -y

# Install PostgreSQL client (server should be separate)
sudo apt-get install postgresql-client -y

# Create application user
sudo useradd -r -m -s /bin/bash app
sudo mkdir -p /opt/app /etc/app
sudo chown app:app /opt/app /etc/app
```

### Database Setup

#### 1. Staging Database Server

```sql
-- On staging database server
CREATE DATABASE school_staging;
CREATE USER school_staging WITH PASSWORD 'staging_secure_password';
GRANT ALL PRIVILEGES ON DATABASE school_staging TO school_staging;

-- Configure connection limits and security
ALTER USER school_staging CONNECTION LIMIT 20;
```

#### 2. Database Configuration

```bash
# Test database connectivity from application server
psql -h staging-db.example.com -U school_staging -d school_staging

# Import production-like data (sanitized)
pg_restore -h staging-db.example.com -U school_staging -d school_staging staging_backup.sql
```

### Configuration Setup

#### 1. Application Properties

Update `application.properties` for staging:

```properties
# Staging Environment Configuration
app.env=staging

# API Configuration
api.base_url=https://staging-api.example.com/api

# Admin Configuration
admin.email=staging-admin@example.com
admin.secret_code=

# Database Configuration
db.driver=org.postgresql.Driver
db.url=jdbc:postgresql://staging-db.example.com:5432/school_staging
db.username=school_staging
db.password=

# Email Configuration
email.host=smtp.staging.example.com
email.port=587
email.username=staging-noreply@example.com
email.password=
email.from_address=staging-noreply@example.com
email.auth.enable=true
email.tls.enable=true

# JWT Configuration
jwt.secret=
jwt.expiration_ms=1800000

# Server Configuration
server.bind_address=0.0.0.0
server.port=8081

# CORS Configuration (Restricted)
cors.allow_credentials=true
cors.allowed_headers=Accept,Authorization,Content-Length,Content-Type,Origin,X-Requested-With
cors.allowed_methods=DELETE,GET,OPTIONS,POST,PUT
cors.allowed_origins=https://staging-frontend.example.com

# CSRF Configuration
security.csrf.allowed_origin=https://staging-frontend.example.com

# UI Configuration
ui.base_url=https://staging-frontend.example.com

# Attendance Scheduler
attendance.schedule.time=17:00
attendance.schedule.enabled=true
attendance.schedule.timezone=America/New_York

# Bulk Upload
bulk_upload.template_path=/opt/app/templates/student_batch_template.xlsx
```

#### 2. Secure Properties

Create `/etc/app/secure.properties`:

```properties
# Staging Secure Configuration
db.password.staging=staging_secure_database_password
email.password.staging=staging_email_app_password
jwt.secret.staging=staging_jwt_secret_key_minimum_32_characters_long_and_secure
admin.secret_code.staging=staging_admin_secret_code_complex
```

Set secure permissions:
```bash
sudo chmod 600 /etc/app/secure.properties
sudo chown app:app /etc/app/secure.properties
```

### Deployment Process

#### 1. Automated Deployment Script

Create `/opt/app/deploy.sh`:

```bash
#!/bin/bash
set -e

APP_DIR="/opt/app"
BACKUP_DIR="/opt/app/backups"
JAR_NAME="school-management.jar"
SERVICE_NAME="school-management"

echo "Starting staging deployment..."

# Create backup
mkdir -p $BACKUP_DIR
if [ -f "$APP_DIR/$JAR_NAME" ]; then
    cp "$APP_DIR/$JAR_NAME" "$BACKUP_DIR/$JAR_NAME.$(date +%Y%m%d_%H%M%S)"
fi

# Stop service
sudo systemctl stop $SERVICE_NAME

# Deploy new version
cp target/$JAR_NAME $APP_DIR/
chown app:app $APP_DIR/$JAR_NAME

# Start service
sudo systemctl start $SERVICE_NAME

# Verify deployment
sleep 10
if curl -f http://localhost:8081/api/health; then
    echo "Deployment successful!"
else
    echo "Deployment failed! Rolling back..."
    sudo systemctl stop $SERVICE_NAME
    cp "$BACKUP_DIR/$JAR_NAME.$(ls -t $BACKUP_DIR | head -1)" "$APP_DIR/$JAR_NAME"
    sudo systemctl start $SERVICE_NAME
    exit 1
fi
```

#### 2. System Service Configuration

Create `/etc/systemd/system/school-management.service`:

```ini
[Unit]
Description=School Management System - Staging
After=network.target

[Service]
Type=simple
User=app
Group=app
WorkingDirectory=/opt/app
Environment=APP_ENV=staging
Environment=CONFIG_SECURE_PATH=/etc/app/secure.properties
ExecStart=/usr/bin/java -Xmx2g -Dapp.env=staging -Dconfig.secure.path=/etc/app/secure.properties -jar school-management.jar
Restart=always
RestartSec=10

# Security settings
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true
ReadWritePaths=/opt/app/logs

[Install]
WantedBy=multi-user.target
```

Enable and start service:
```bash
sudo systemctl daemon-reload
sudo systemctl enable school-management
sudo systemctl start school-management
```

#### 3. SSL/TLS Configuration

Configure reverse proxy (Nginx):

```nginx
# /etc/nginx/sites-available/staging-api.example.com
server {
    listen 443 ssl http2;
    server_name staging-api.example.com;

    ssl_certificate /etc/ssl/certs/staging-api.example.com.crt;
    ssl_certificate_key /etc/ssl/private/staging-api.example.com.key;

    location / {
        proxy_pass http://localhost:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### Staging Testing

#### 1. Automated Testing

```bash
#!/bin/bash
# staging-tests.sh

BASE_URL="https://staging-api.example.com/api"

echo "Running staging environment tests..."

# Health check
curl -f "$BASE_URL/health" || exit 1

# Authentication test
TOKEN=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}' | jq -r '.token')

if [ "$TOKEN" = "null" ]; then
    echo "Authentication test failed"
    exit 1
fi

# API functionality tests
curl -f -H "Authorization: Bearer $TOKEN" "$BASE_URL/students" || exit 1

echo "All staging tests passed!"
```

#### 2. Performance Testing

```bash
# Load testing with Apache Bench
ab -n 1000 -c 10 https://staging-api.example.com/api/health

# Database performance testing
psql -h staging-db.example.com -U school_staging -d school_staging -c "EXPLAIN ANALYZE SELECT * FROM students LIMIT 100;"
```

## Production Environment

### Prerequisites

#### System Requirements
- **Java**: OpenJDK 11 or higher
- **Database**: PostgreSQL 12+ (high-availability cluster)
- **Memory**: Minimum 8GB RAM
- **Storage**: 100GB free space (with backup storage)
- **Network**: Load balancer, SSL certificates, firewall configuration
- **Monitoring**: Application and infrastructure monitoring

#### Infrastructure Setup

**Production Server Cluster:**
```bash
# On each production server
sudo apt-get update && sudo apt-get upgrade -y
sudo apt-get install openjdk-11-jdk postgresql-client nginx -y

# Create application user with restricted permissions
sudo useradd -r -m -s /bin/false app
sudo mkdir -p /opt/app /etc/app /var/log/app
sudo chown app:app /opt/app /etc/app /var/log/app
```

### Database Setup

#### 1. Production Database Cluster

```sql
-- On production database cluster
CREATE DATABASE school_production;
CREATE USER school_production WITH PASSWORD 'very_secure_production_password';
GRANT ALL PRIVILEGES ON DATABASE school_production TO school_production;

-- Configure connection pooling and limits
ALTER USER school_production CONNECTION LIMIT 50;

-- Set up read replicas for reporting
CREATE USER school_readonly WITH PASSWORD 'readonly_secure_password';
GRANT SELECT ON ALL TABLES IN SCHEMA public TO school_readonly;
```

#### 2. Database Security

```bash
# Configure PostgreSQL for production
sudo nano /etc/postgresql/12/main/postgresql.conf

# Key settings:
# max_connections = 200
# shared_buffers = 2GB
# effective_cache_size = 6GB
# maintenance_work_mem = 512MB
# checkpoint_completion_target = 0.9
# wal_buffers = 16MB
# default_statistics_target = 100

# Configure authentication
sudo nano /etc/postgresql/12/main/pg_hba.conf

# Add production server IPs
host    school_production    school_production    10.0.1.0/24    md5
```

### Configuration Setup

#### 1. Production Application Properties

```properties
# Production Environment Configuration
app.env=prod

# API Configuration
api.base_url=https://api.example.com/api

# Admin Configuration
admin.email=admin@example.com
admin.secret_code=

# Database Configuration
db.driver=org.postgresql.Driver
db.url=jdbc:postgresql://prod-db-cluster.example.com:5432/school_production
db.username=school_production
db.password=

# Email Configuration
email.host=smtp.example.com
email.port=587
email.username=noreply@example.com
email.password=
email.from_address=noreply@example.com
email.auth.enable=true
email.tls.enable=true

# JWT Configuration
jwt.secret=
jwt.expiration_ms=1800000

# Server Configuration
server.bind_address=127.0.0.1
server.port=8081

# CORS Configuration (Strict)
cors.allow_credentials=true
cors.allowed_headers=Accept,Authorization,Content-Length,Content-Type,Origin,X-Requested-With
cors.allowed_methods=DELETE,GET,OPTIONS,POST,PUT
cors.allowed_origins=https://app.example.com

# CSRF Configuration
security.csrf.allowed_origin=https://app.example.com

# UI Configuration
ui.base_url=https://app.example.com

# Attendance Scheduler
attendance.schedule.time=16:30
attendance.schedule.enabled=true
attendance.schedule.timezone=America/New_York

# Bulk Upload
bulk_upload.template_path=/opt/app/templates/student_batch_template.xlsx
```

#### 2. Production Secure Properties

Create `/etc/app/secure.properties`:

```properties
# Production Secure Configuration
db.password.prod=very_secure_production_database_password_with_special_chars_123!@#
email.password.prod=production_email_app_specific_password_from_provider
jwt.secret.prod=extremely_long_and_secure_jwt_secret_key_for_production_use_minimum_64_characters_recommended
admin.secret_code.prod=complex_admin_secret_code_for_production_environment_123!@#
```

Set maximum security permissions:
```bash
sudo chmod 600 /etc/app/secure.properties
sudo chown app:app /etc/app/secure.properties
sudo chattr +i /etc/app/secure.properties  # Make immutable
```

### Production Deployment

#### 1. Blue-Green Deployment Script

Create `/opt/app/blue-green-deploy.sh`:

```bash
#!/bin/bash
set -e

BLUE_PORT=8081
GREEN_PORT=8082
HEALTH_CHECK_URL="http://localhost"
NGINX_CONFIG="/etc/nginx/sites-available/api.example.com"
SERVICE_NAME="school-management"

# Determine current active environment
CURRENT_PORT=$(grep "proxy_pass" $NGINX_CONFIG | grep -o "808[12]")
if [ "$CURRENT_PORT" = "$BLUE_PORT" ]; then
    DEPLOY_PORT=$GREEN_PORT
    DEPLOY_ENV="green"
    CURRENT_ENV="blue"
else
    DEPLOY_PORT=$BLUE_PORT
    DEPLOY_ENV="blue"
    CURRENT_ENV="green"
fi

echo "Deploying to $DEPLOY_ENV environment (port $DEPLOY_PORT)"

# Deploy to inactive environment
sudo systemctl stop school-management-$DEPLOY_ENV || true
cp target/school-management.jar /opt/app/school-management-$DEPLOY_ENV.jar
chown app:app /opt/app/school-management-$DEPLOY_ENV.jar

# Start new environment
sudo systemctl start school-management-$DEPLOY_ENV

# Health check
sleep 30
for i in {1..10}; do
    if curl -f "$HEALTH_CHECK_URL:$DEPLOY_PORT/api/health"; then
        echo "Health check passed"
        break
    fi
    if [ $i -eq 10 ]; then
        echo "Health check failed after 10 attempts"
        sudo systemctl stop school-management-$DEPLOY_ENV
        exit 1
    fi
    sleep 10
done

# Switch traffic
sed -i "s/proxy_pass http:\/\/localhost:$CURRENT_PORT/proxy_pass http:\/\/localhost:$DEPLOY_PORT/" $NGINX_CONFIG
sudo nginx -t && sudo systemctl reload nginx

# Stop old environment
sleep 30
sudo systemctl stop school-management-$CURRENT_ENV

echo "Deployment to $DEPLOY_ENV completed successfully"
```

#### 2. Production System Services

Create service files for blue-green deployment:

`/etc/systemd/system/school-management-blue.service`:
```ini
[Unit]
Description=School Management System - Production Blue
After=network.target

[Service]
Type=simple
User=app
Group=app
WorkingDirectory=/opt/app
Environment=APP_ENV=prod
Environment=CONFIG_SECURE_PATH=/etc/app/secure.properties
Environment=SERVER_PORT=8081
ExecStart=/usr/bin/java -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Dapp.env=prod -Dconfig.secure.path=/etc/app/secure.properties -Dserver.port=8081 -jar school-management-blue.jar
Restart=always
RestartSec=30

# Security hardening
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true
ReadWritePaths=/opt/app/logs /var/log/app
CapabilityBoundingSet=
AmbientCapabilities=
ProtectKernelTunables=true
ProtectKernelModules=true
ProtectControlGroups=true

[Install]
WantedBy=multi-user.target
```

`/etc/systemd/system/school-management-green.service`:
```ini
[Unit]
Description=School Management System - Production Green
After=network.target

[Service]
Type=simple
User=app
Group=app
WorkingDirectory=/opt/app
Environment=APP_ENV=prod
Environment=CONFIG_SECURE_PATH=/etc/app/secure.properties
Environment=SERVER_PORT=8082
ExecStart=/usr/bin/java -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Dapp.env=prod -Dconfig.secure.path=/etc/app/secure.properties -Dserver.port=8082 -jar school-management-green.jar
Restart=always
RestartSec=30

# Security hardening
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true
ReadWritePaths=/opt/app/logs /var/log/app
CapabilityBoundingSet=
AmbientCapabilities=
ProtectKernelTunables=true
ProtectKernelModules=true
ProtectControlGroups=true

[Install]
WantedBy=multi-user.target
```

#### 3. Load Balancer Configuration

Nginx configuration for production:

```nginx
# /etc/nginx/sites-available/api.example.com
upstream school_management {
    server localhost:8081 max_fails=3 fail_timeout=30s;
    # server localhost:8082 backup;  # Uncomment for active-passive setup
}

server {
    listen 443 ssl http2;
    server_name api.example.com;

    # SSL Configuration
    ssl_certificate /etc/ssl/certs/api.example.com.crt;
    ssl_certificate_key /etc/ssl/private/api.example.com.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;

    # Security Headers
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Frame-Options DENY always;
    add_header X-Content-Type-Options nosniff always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;

    # Rate Limiting
    limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;
    limit_req zone=api burst=20 nodelay;

    location / {
        proxy_pass http://school_management;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Timeouts
        proxy_connect_timeout 5s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        
        # Health check
        proxy_next_upstream error timeout http_500 http_502 http_503 http_504;
    }

    # Health check endpoint (bypass rate limiting)
    location /api/health {
        limit_req off;
        proxy_pass http://school_management;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# Redirect HTTP to HTTPS
server {
    listen 80;
    server_name api.example.com;
    return 301 https://$server_name$request_uri;
}
```

### Production Security

#### 1. Firewall Configuration

```bash
# UFW firewall rules
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow ssh
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow from 10.0.1.0/24 to any port 8081  # Internal network only
sudo ufw enable
```

#### 2. Application Security

```bash
# Set up log rotation
sudo nano /etc/logrotate.d/school-management

/var/log/app/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    create 644 app app
    postrotate
        systemctl reload school-management-blue school-management-green
    endscript
}
```

#### 3. Monitoring and Alerting

```bash
# Install monitoring agent (example: Prometheus node exporter)
wget https://github.com/prometheus/node_exporter/releases/download/v1.3.1/node_exporter-1.3.1.linux-amd64.tar.gz
tar xvfz node_exporter-1.3.1.linux-amd64.tar.gz
sudo cp node_exporter-1.3.1.linux-amd64/node_exporter /usr/local/bin/
sudo chown app:app /usr/local/bin/node_exporter

# Create monitoring service
sudo nano /etc/systemd/system/node_exporter.service
```

## Environment Comparison

| Aspect | Development | Staging | Production |
|--------|-------------|---------|------------|
| **Database** | Local PostgreSQL | Dedicated staging DB | HA cluster |
| **Security** | Permissive | Moderate | Strict |
| **SSL/TLS** | Optional | Required | Required |
| **Monitoring** | Basic | Enhanced | Comprehensive |
| **Backup** | Optional | Daily | Real-time + Daily |
| **Load Balancing** | None | Optional | Required |
| **Caching** | None | Optional | Required |
| **Log Retention** | 7 days | 30 days | 90+ days |
| **Resource Limits** | 2GB RAM | 4GB RAM | 8GB+ RAM |
| **Deployment** | Manual | Semi-automated | Automated |
| **Testing** | Unit tests | Integration tests | Full test suite |

## Migration Between Environments

### Development to Staging

```bash
#!/bin/bash
# migrate-dev-to-staging.sh

echo "Migrating from Development to Staging..."

# 1. Export development database (sanitized)
pg_dump -h localhost -U postgres canal_prep_school_clone \
  --exclude-table=audit_logs \
  --exclude-table=session_tokens > dev_export.sql

# 2. Sanitize sensitive data
sed -i 's/real_email@example.com/test_email@staging.com/g' dev_export.sql
sed -i 's/real_phone_number/555-0000/g' dev_export.sql

# 3. Import to staging
psql -h staging-db.example.com -U school_staging -d school_staging < dev_export.sql

# 4. Update configuration for staging
cp application.properties application.properties.staging
sed -i 's/app.env=dev/app.env=staging/g' application.properties.staging

echo "Migration to staging completed"
```

### Staging to Production

```bash
#!/bin/bash
# migrate-staging-to-prod.sh

echo "Migrating from Staging to Production..."

# 1. Create production database backup point
pg_dump -h prod-db-cluster.example.com -U school_production school_production > prod_backup_$(date +%Y%m%d).sql

# 2. Export staging data
pg_dump -h staging-db.example.com -U school_staging school_staging > staging_export.sql

# 3. Validate data integrity
psql -h staging-db.example.com -U school_staging -d school_staging -c "SELECT COUNT(*) FROM students;"

# 4. Import to production (during maintenance window)
psql -h prod-db-cluster.example.com -U school_production -d school_production < staging_export.sql

# 5. Verify production data
psql -h prod-db-cluster.example.com -U school_production -d school_production -c "SELECT COUNT(*) FROM students;"

echo "Migration to production completed"
```

## Troubleshooting

### Common Deployment Issues

#### 1. Service Won't Start

**Symptoms:**
- Service fails to start
- "Failed to start" in systemctl status
- Application exits immediately

**Diagnosis:**
```bash
# Check service status
sudo systemctl status school-management

# Check application logs
sudo journalctl -u school-management -f

# Check Java process
ps aux | grep java

# Check port availability
netstat -tlnp | grep 8081
```

**Solutions:**
```bash
# Check Java installation
java -version

# Verify JAR file
java -jar school-management.jar --help

# Check permissions
ls -la /opt/app/
ls -la /etc/app/

# Test configuration
java -Dapp.env=prod -Dconfig.secure.path=/etc/app/secure.properties -jar school-management.jar --validate-config
```

#### 2. Database Connection Issues

**Symptoms:**
- "Connection refused" errors
- "Authentication failed" errors
- Slow application startup

**Diagnosis:**
```bash
# Test database connectivity
psql -h database-host -U username -d database_name

# Check network connectivity
telnet database-host 5432

# Verify credentials
grep -i password /etc/app/secure.properties
```

**Solutions:**
```bash
# Update database configuration
nano /etc/app/secure.properties

# Restart application
sudo systemctl restart school-management

# Check database server status
sudo systemctl status postgresql  # If local database
```

#### 3. SSL/TLS Certificate Issues

**Symptoms:**
- "Certificate expired" errors
- "SSL handshake failed" errors
- Browser security warnings

**Diagnosis:**
```bash
# Check certificate expiration
openssl x509 -in /etc/ssl/certs/api.example.com.crt -text -noout | grep "Not After"

# Test SSL configuration
openssl s_client -connect api.example.com:443

# Check Nginx configuration
sudo nginx -t
```

**Solutions:**
```bash
# Renew certificate (Let's Encrypt example)
sudo certbot renew

# Update Nginx configuration
sudo systemctl reload nginx

# Verify SSL setup
curl -I https://api.example.com/api/health
```

#### 4. Performance Issues

**Symptoms:**
- Slow response times
- High CPU/memory usage
- Database connection pool exhaustion

**Diagnosis:**
```bash
# Check system resources
top
htop
free -h
df -h

# Check application metrics
curl http://localhost:8081/api/metrics

# Check database performance
psql -h database-host -U username -d database_name -c "SELECT * FROM pg_stat_activity;"
```

**Solutions:**
```bash
# Adjust JVM settings
# Edit service file to increase heap size
-Xmx8g -XX:+UseG1GC

# Optimize database connections
# Update application.properties
db.pool.max_connections=50
db.pool.min_connections=10

# Enable caching
# Add Redis or in-memory caching
```

### Environment-Specific Troubleshooting

#### Development Environment

```bash
# Reset development database
dropdb -h localhost -U postgres canal_prep_school_clone
createdb -h localhost -U postgres canal_prep_school_clone

# Clear application cache
rm -rf target/
mvn clean

# Reset configuration
git checkout -- src/main/resources/application.properties
```

#### Staging Environment

```bash
# Check staging-specific logs
tail -f /var/log/app/staging.log

# Verify staging configuration
diff application.properties.dev application.properties.staging

# Test staging endpoints
curl -k https://staging-api.example.com/api/health
```

#### Production Environment

```bash
# Check production logs (with rotation)
zcat /var/log/app/application.log.*.gz | grep ERROR

# Monitor production metrics
curl -s http://localhost:8081/api/metrics | jq '.database.connections'

# Verify load balancer health
curl -I https://api.example.com/api/health
```

---

*Last updated: January 2025*
*Version: 1.0*
*For emergency support, contact the DevOps team or refer to the incident response procedures.*
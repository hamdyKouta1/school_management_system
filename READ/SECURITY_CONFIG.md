# Configuration Security Guide

## 🔒 Securing Application Properties

This guide explains how to secure sensitive configuration data in the School Management System.

## 1. Environment Variables (Recommended for Production)

### Setup
```bash
# Set environment variables
export EMAIL_PASSWORD="your-secure-email-password"
export DB_PASSWORD="your-secure-db-password"
export JWT_SECRET="your-256-bit-jwt-secret"

# Run application
java -jar school-management.jar
```

### Configuration
```properties
# application.properties - No sensitive data
email.password=${EMAIL_PASSWORD}
db.password=${DB_PASSWORD}
jwt.secret=${JWT_SECRET}
```

## 2. External Configuration Files

### Create Secure Config File
```bash
# Create secure directory
mkdir -p /etc/app

# Create secure config file
cat > /etc/app/secure.properties << EOF
email.password=your-secure-email-password
db.password=your-secure-db-password
jwt.secret=your-256-bit-jwt-secret
EOF

# Set restrictive permissions
chmod 600 /etc/app/secure.properties
chown app:app /etc/app/secure.properties
```

### Usage
```bash
# Specify secure config location
java -Dconfig.secure.path=/etc/app/secure.properties -jar school-management.jar

# Or use environment variable
export CONFIG_SECURE_PATH=/etc/app/secure.properties
java -jar school-management.jar
```

## 3. File Permissions

### Recommended Permissions
```bash
# Secure config files (owner read/write only)
chmod 600 secure.properties

# Config directories (owner read/write/execute, group read/execute)
chmod 750 /etc/app

# Application properties (readable by all, writable by owner)
chmod 644 application.properties
```

## 4. Environment-Specific Configuration

### Development
```properties
# application.properties
email.password.dev=development-password
```

### Production
```bash
# Use environment variables
export EMAIL_PASSWORD="production-secure-password"
export APP_ENV="prod"
```

## 5. Security Best Practices

### ✅ DO
- Use environment variables for production secrets
- Store sensitive config files outside the application directory
- Set restrictive file permissions (600 for sensitive files)
- Use different secrets for each environment
- Regularly rotate passwords and secrets
- Use secure secret management systems (HashiCorp Vault, AWS Secrets Manager)

### ❌ DON'T
- Commit sensitive data to version control
- Use default or weak passwords
- Store secrets in application.properties for production
- Share configuration files via email or chat
- Use the same secrets across environments

## 6. Secret Management Tools

### HashiCorp Vault
```bash
# Store secret in Vault
vault kv put secret/app email_password="secure-password"

# Retrieve in application
EMAIL_PASSWORD=$(vault kv get -field=email_password secret/app)
```

### AWS Secrets Manager
```bash
# Store secret
aws secretsmanager create-secret --name "app/email-password" --secret-string "secure-password"

# Retrieve in application
EMAIL_PASSWORD=$(aws secretsmanager get-secret-value --secret-id "app/email-password" --query SecretString --output text)
```

## 7. Monitoring and Auditing

### Log Security Events
- Configuration file access
- Failed authentication attempts
- Secret rotation events
- Unauthorized access attempts

### Regular Security Checks
- Review file permissions monthly
- Audit configuration access logs
- Rotate secrets quarterly
- Update dependencies regularly

## 8. Emergency Procedures

### If Secrets Are Compromised
1. Immediately rotate all affected secrets
2. Update all environments with new secrets
3. Review access logs for unauthorized usage
4. Notify security team and stakeholders
5. Document the incident and lessons learned

## 9. Configuration Validation

### Startup Checks
The application validates configuration on startup:
- Checks for required properties
- Validates secret strength (JWT keys must be 256-bit minimum)
- Logs configuration source (internal/external)
- Warns about missing secure configuration

### Health Checks
- Monitor configuration file integrity
- Check secret expiration dates
- Validate environment variable availability
- Alert on configuration drift

## 10. Compliance

### Data Protection
- Encrypt sensitive configuration files at rest
- Use TLS for configuration transmission
- Implement access controls and audit trails
- Follow GDPR/CCPA requirements for data handling

### Industry Standards
- Follow OWASP security guidelines
- Implement NIST cybersecurity framework
- Comply with SOC 2 Type II requirements
- Adhere to ISO 27001 standards
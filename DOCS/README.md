# School Management System Documentation

This documentation is organized into 7 main categories for easy navigation and maintenance.

## 📁 Documentation Structure

### 01-Installation
Complete installation and setup guides
- **INSTALLATION_GUIDE.md** - Step-by-step installation instructions

### 02-Documentation
Core system documentation and user guides
- **README.md** - Main project overview
- **TECHNICAL_DOCUMENTATION.md** - Technical specifications and architecture
- **USER_GUIDE.md** - End-user manual and instructions
- **FIELD_MAPPING.md** - Database field mappings and relationships

### 03-Troubleshooting
Debugging guides and problem resolution
- **TROUBLESHOOTING_GUIDE.md** - Common issues and solutions
- **REGISTRATION_BUG_FIX.md** - Registration system fixes
- **SECURITY_WEAKNESS_ANALYSIS_REPORT.md** - Security analysis and recommendations
- **NGROK_BROWSER_WARNING_SOLUTION.md** - NGROK browser warning fixes
- **NGROK_SOLUTION_COMPLETE.md** - Complete NGROK setup and solutions

### 04-License-Renewal
License system documentation and renewal processes
- **LICENSE_SYSTEM_DOCUMENTATION.md** - License system overview and functionality
- **LICENSE_ROLE_FILTER_DOCUMENTATION.md** - Role-based license filtering

### 05-API-Reference
API documentation, requests, and responses
- **API_DOCUMENTATION.md** - Complete API reference
- **AUTHENTICATION_SOLUTION.md** - Authentication implementation
- **FRONTEND_API_CONFIGURATION_SOLUTION.md** - Frontend API configuration
- **STUDENT_API_SOLUTION.md** - Student-specific API endpoints

### 06-Configuration
Comprehensive configuration and deployment documentation
- **CONFIGURATION_GUIDE.md** - Complete application.properties reference
- **SECURITY_CONFIGURATION.md** - Secure properties and security settings
- **ENVIRONMENT_DEPLOYMENT_GUIDE.md** - Environment-specific deployment procedures

### 07-Applications
Complete applications and features documentation
- **APPLICATIONS_GUIDE.md** - All system applications, servlets, and components

## 🚀 Quick Start

1. **Installation**: Start with `01-Installation/INSTALLATION_GUIDE.md`
2. **Configuration**: Review `MASTER_CONFIGURATION_GUIDE.md` for complete configuration overview
3. **Environment Setup**: Use `06-Configuration/ENVIRONMENT_DEPLOYMENT_GUIDE.md` for environment-specific setup
4. **Understanding the System**: Read `02-Documentation/README.md` and `02-Documentation/TECHNICAL_DOCUMENTATION.md`
5. **Applications Overview**: Check `07-Applications/APPLICATIONS_GUIDE.md` for all system components
6. **API Integration**: Refer to `05-API-Reference/API_DOCUMENTATION.md`
7. **License Management**: Check `04-License-Renewal/LICENSE_SYSTEM_DOCUMENTATION.md`
8. **Troubleshooting**: Use `03-Troubleshooting/TROUBLESHOOTING_GUIDE.md` for common issues

## 📋 Master Configuration Guide

For comprehensive configuration management, refer to **MASTER_CONFIGURATION_GUIDE.md** which includes:

### Configuration Files
- **application.properties**: Main configuration file with all non-sensitive settings
- **secure.properties**: Sensitive configuration data (passwords, secrets, API keys)
- **Environment Overrides**: Environment-specific property overrides (.dev, .staging, .prod)

### Environment Support
- **Development**: Local development with relaxed security settings
- **Staging**: Pre-production testing with production-like security
- **Production**: Live system with maximum security and audit logging

### Key Configuration Categories
- **Server Configuration**: Port, binding, threading, timeouts
- **Database Configuration**: Connection settings, pooling, SSL
- **Security Configuration**: JWT, CSRF, CORS, authentication
- **Email/SMTP Configuration**: Email notifications and delivery
- **Attendance Configuration**: Automated scheduling and notifications
- **API Configuration**: Rate limiting, documentation, versioning
- **UI Configuration**: Themes, features, localization

### Property Override Hierarchy
1. System Properties (-Dproperty=value)
2. Environment Variables
3. External Secure Configuration (secure.properties)
4. Environment-Specific Properties (.dev, .staging, .prod)
5. Base Application Properties (application.properties)
6. Default Values (hardcoded)

## ⚙️ Current System Configuration

### Server Settings
- **Port**: 8081 (configurable via `server.port`)
- **Bind Address**: 0.0.0.0 (configurable via `server.bind_address`)
- **Environment**: Auto-detected or set via `app.env` property

### Database Configuration
- **Type**: PostgreSQL
- **Default URL**: `jdbc:postgresql://localhost:5432/canal_prep_school_clone`
- **Connection Pooling**: Enabled with configurable pool size
- **SSL Support**: Available for production environments

### Authentication & Security
- **Authentication**: JWT-based with configurable expiration
- **Roles**: USER, ADMIN, DEVELOPER with role-based access control
- **Password Recovery**: OTP-based with configurable expiry (default: 10 minutes)
- **CSRF Protection**: Configurable (enabled in production)
- **CORS**: Configurable origins and methods

### Attendance System
- **Scheduler**: Configurable daily processing time (default: 16:30)
- **Auto-marking**: Automatic absence marking for unrecorded attendance
- **Notifications**: Parent/guardian notifications via email
- **QR Code Integration**: Student QR codes for quick attendance

### License System
- **Validation**: Automatic license validation with configurable intervals
- **Renewal**: OTP-based renewal process
- **Monitoring**: Expiration warnings and usage tracking
- **API Access**: Complete REST API for license management

### Email System
- **SMTP Support**: Configurable SMTP settings
- **Templates**: HTML email templates for notifications
- **Delivery**: Reliable email delivery with retry mechanisms
- **Security**: TLS/SSL support for secure email transmission

## 📋 Documentation Maintenance

### Version Control
- **Documentation Version**: 2.0.0 (Updated with comprehensive configuration guides)
- **Last Updated**: December 2024
- **Compatibility**: School Management System v1.0+
- **Configuration Schema Version**: 1.0

### Cross-References and Dependencies
- **Configuration Files**: See `MASTER_CONFIGURATION_GUIDE.md` for complete property reference
- **Environment Setup**: `06-Configuration/ENVIRONMENT_DEPLOYMENT_GUIDE.md` depends on `application.properties` and `secure.properties`
- **Security Configuration**: `06-Configuration/SECURITY_CONFIGURATION.md` references JWT, CSRF, and CORS settings
- **Application Components**: `07-Applications/APPLICATIONS_GUIDE.md` details servlet configurations and dependencies
- **API Documentation**: `05-API-Reference/API_DOCUMENTATION.md` includes configuration-dependent endpoints
- **Troubleshooting**: `03-Troubleshooting/TROUBLESHOOTING_GUIDE.md` includes configuration-related issues

### Property Override Rules and Inheritance
1. **System Properties** (-Dproperty=value) - Highest priority
2. **Environment Variables** - Second priority
3. **External Secure Configuration** (secure.properties) - Third priority
4. **Environment-Specific Properties** (.dev, .staging, .prod) - Fourth priority
5. **Base Application Properties** (application.properties) - Fifth priority
6. **Default Values** (hardcoded in application) - Lowest priority

### Configuration File Relationships
- `application.properties` ← Base configuration for all environments
- `secure.properties` ← Overrides sensitive properties in application.properties
- `application-dev.properties` ← Development-specific overrides
- `application-staging.properties` ← Staging-specific overrides
- `application-prod.properties` ← Production-specific overrides

### Documentation Structure Updates
- Added **06-Configuration** section with comprehensive configuration guides
- Added **07-Applications** section with detailed application documentation
- Enhanced cross-referencing between configuration and deployment guides
- Integrated security best practices throughout all documentation

This documentation structure follows a logical organization:
- **Numbered folders** ensure proper ordering
- **Descriptive names** make content easily identifiable
- **Categorized content** reduces search time
- **Centralized location** improves maintenance

## 🔐 License System Status

The system includes a comprehensive license management system:
- **License Validation**: Automatic validation on startup
- **Role-Based Access**: DEVELOPER role required for license operations
- **OTP Security**: Separate OTP endpoints for developers and admins
- **API Endpoints**: Complete REST API for license management

---

*Last updated: January 2025*
*System Status: Fully operational with license validation enabled*
*For technical support, refer to the troubleshooting section or contact the development team.*
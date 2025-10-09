# School Management System Documentation

This documentation is organized into 5 main categories for easy navigation and maintenance.

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

## 🚀 Quick Start

1. **Installation**: Start with `01-Installation/INSTALLATION_GUIDE.md`
2. **Understanding the System**: Read `02-Documentation/README.md` and `02-Documentation/TECHNICAL_DOCUMENTATION.md`
3. **API Integration**: Refer to `05-API-Reference/API_DOCUMENTATION.md`
4. **License Management**: Check `04-License-Renewal/LICENSE_SYSTEM_DOCUMENTATION.md`
5. **Troubleshooting**: Use `03-Troubleshooting/TROUBLESHOOTING_GUIDE.md` for common issues

## ⚙️ Current System Configuration

- **Server**: Runs on port 8081, binds to all interfaces (0.0.0.0:8081)
- **Database**: PostgreSQL on localhost:5432, database `school_management_system`
- **Authentication**: JWT-based with role-based access control (USER, ADMIN, DEVELOPER)
- **License System**: Active with OTP-based renewal and management
- **CORS**: Enabled for frontend integration

## 📋 Documentation Maintenance

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
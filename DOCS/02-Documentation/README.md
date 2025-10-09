# School Management System Documentation

## Overview

The School Management System is a comprehensive web application designed for managing students, attendance, and other school-related data. It provides a user-friendly interface for administrators and teachers to manage student records, track attendance, and generate reports.

## Table of Contents

1. [System Requirements](#system-requirements)
2. [Installation](#installation)
3. [Configuration](#configuration)
4. [Running the Application](#running-the-application)
5. [API Documentation](#api-documentation)
6. [Frontend Usage](#frontend-usage)
7. [Database Schema](#database-schema)
8. [Authentication and Security](#authentication-and-security)
9. [Debugging](#debugging)
10. [Troubleshooting](#troubleshooting)
11. [Additional Documentation](#additional-documentation)

## System Requirements

- Java 11 or higher
- PostgreSQL 12 or higher
- Maven 3.6 or higher
- Modern web browser (Chrome, Firefox, Edge, etc.)

## Installation

### Clone the Repository

```bash
git clone <repository-url>
cd School_Management_System
```

### Build the Project

```bash
mvn clean package
```

This will create an executable JAR file in the `target` directory named `school-management.jar`.

## Configuration

### Database Configuration

The application requires a PostgreSQL database. Set the following environment variables before running the application:

```bash
# Windows
set DB_URL=jdbc:postgresql://localhost:5432/school_db
set DB_USER=postgres
set DB_PASSWORD=your_password

# Linux/macOS
export DB_URL=jdbc:postgresql://localhost:5432/school_db
export DB_USER=postgres
export DB_PASSWORD=your_password
```

Alternatively, you can modify the `DBConnection.java` file to hardcode these values (not recommended for production):

```java
private static final String JDBC_URL = "jdbc:postgresql://localhost:5432/school_db";
private static final String USERNAME = "postgres";
private static final String PASSWORD = "your_password";
```

### JWT Configuration

Set the JWT secret key as an environment variable:

```bash
# Windows
set JWT_SECRET=your_secure_jwt_secret_key

# Linux/macOS
export JWT_SECRET=your_secure_jwt_secret_key
```

If not set, the application will generate a temporary key, which is not secure for production use.

## Running the Application

### From JAR File

```bash
java -jar target/school-management.jar [port]
```

The default port is 8081. You can specify a different port as a command-line argument.

### From IDE

Run the `MainApp.java` class as a Java application. Make sure to set the required environment variables in your IDE's run configuration.

### Accessing the Application

Once the application is running, open a web browser and navigate to:

```
http://localhost:8081
```

## API Documentation

The application provides a RESTful API for interacting with the system programmatically. A Postman collection and environment are included in the project for testing the API.

### Postman Setup

1. Import the `School_Management_System_Postman_Collection.json` file into Postman
2. Import the `School_Management_System_Postman_Environment.json` file into Postman
3. Set the `base_url` variable to match your server (default: http://localhost:8081)

### Authentication

All protected endpoints require a JWT token, which can be obtained by logging in:

```
POST /api/auth/login
Body: {"username": "admin", "password": "password123"}
```

The token should be included in the `Authorization` header as `Bearer <token>`.

### API Endpoints

See the `Notes.txt` file for a complete list of API endpoints and their usage.

## Frontend Usage

The application provides a web interface for managing the school system. The main features include:

### Student Management

- View all students
- Add new students
- Edit student information
- Delete students
- Add notes, phone numbers, and medical history for students

### Attendance Management

- Record daily attendance
- View attendance records by date
- Generate attendance reports

### Dashboard

- View summary statistics
- Monitor student counts by grade and class

## Database Schema

The application uses a PostgreSQL database with the following main tables:

- `students`: Stores student information
- `attendance`: Records student attendance
- `users`: Stores user authentication information
- `student_notes`: Stores notes about students
- `student_phone`: Stores student phone numbers
- `medical_history`: Stores student medical information
- `additional_qualifications`: Stores student qualifications

## Authentication and Security

The application uses JWT (JSON Web Tokens) for authentication. The token contains the user's ID, username, email, and role.

Security features include:

- Password hashing using BCrypt
- Role-based access control (USER and ADMIN roles)
- CORS configuration for frontend access
- Security headers to prevent common web vulnerabilities

## Debugging

### Logging

The application uses Java's built-in logging system. Logs are output to the console by default.

### Common Issues

#### Database Connection Issues

If the application fails to start with a database connection error:

1. Verify that PostgreSQL is running
2. Check the database connection environment variables
3. Ensure the database exists and is accessible
4. Check for network issues if using a remote database

#### Authentication Issues

If you're having trouble logging in:

1. Verify that the username and password are correct
2. Check if the JWT_SECRET environment variable is set
3. Ensure the token is being sent correctly in the Authorization header

#### API Request Issues

If API requests are failing:

1. Check the browser console for error messages
2. Verify that the token is valid and not expired
3. Ensure the request format matches the API documentation

## Troubleshooting

### License Issues

The application uses a license system that generates a license file (`license.dat`) on first run. The license is valid for 365 days from installation. If you encounter license issues:

1. Delete the `license.dat` file to generate a new license
2. Check the system date to ensure it's correct

### Server Startup Issues

If the server fails to start:

1. Check if another application is using the same port
2. Verify that all required environment variables are set
3. Ensure Java 11 or higher is installed and set as the default Java version

### Frontend Issues

If the frontend is not loading correctly:

1. Clear your browser cache
2. Check the browser console for JavaScript errors
3. Verify that the static resources are being served correctly

---

## Additional Documentation

Detailed documentation has been created to help you better understand, use, and maintain the system:

1. [INSTALLATION_GUIDE.md](./INSTALLATION_GUIDE.md) - Comprehensive installation instructions including prerequisites, database setup, configuration, and deployment options.

2. [USER_GUIDE.md](./USER_GUIDE.md) - End-user focused guide explaining how to use all features of the system, including student management, attendance tracking, and reporting.

3. [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) - Detailed API reference with all endpoints, request/response formats, authentication requirements, and testing instructions.

4. [TECHNICAL_DOCUMENTATION.md](./TECHNICAL_DOCUMENTATION.md) - In-depth technical details about the system architecture, components, implementation details, and performance considerations.

5. [TROUBLESHOOTING_GUIDE.md](./TROUBLESHOOTING_GUIDE.md) - Solutions for common issues, debugging techniques, and advanced troubleshooting for server, database, authentication, and frontend problems.

---

For additional support or to report issues, please contact the system administrator or create an issue in the project repository.
# School Management System - Troubleshooting Guide

## Introduction

This troubleshooting guide provides solutions for common issues that may arise when using, deploying, or maintaining the School Management System. It is intended for system administrators, developers, and technical support staff.

## Server Issues

### Server Won't Start

#### Symptoms
- Error messages when running the JAR file
- The application doesn't respond on the configured port
- Java exceptions in the console output

#### Possible Causes and Solutions

1. **Port Conflict**
   - **Symptom**: Error message containing "Address already in use" or "Port 8081 already in use"
   - **Solution**: 
     - Check if another application is using port 8081
     - Kill the process using the port: `netstat -ano | findstr 8081` then `taskkill /PID [PID] /F`
     - **Note**: Port 8081 is hardcoded in MainApp.java and cannot be changed via configuration

2. **Insufficient Permissions**
   - **Symptom**: Access denied errors
   - **Solution**: 
     - Run the application with administrator privileges
     - Check file and directory permissions

3. **Java Version Mismatch**
   - **Symptom**: Unsupported class version error
   - **Solution**: 
     - Verify that you're using Java 11 or higher
     - Run `java -version` to check the installed version
     - Install the correct Java version if needed

4. **Memory Issues**
   - **Symptom**: OutOfMemoryError in logs
   - **Solution**: 
     - Increase Java heap size with `-Xmx` parameter
     - Example: `java -Xmx1024m -jar school-management-system.jar`

5. **License Issues**
   - **Symptom**: Error message about invalid or expired license
   - **Solution**: 
     - Check the `licenses` table in the database for active licenses
     - Use the license renewal API with developer role and OTP
     - Verify system date and time are correct
     - Check license validation logs in the application output

## Database Connection Issues

### Unable to Connect to Database

#### Symptoms
- Error messages containing "Connection refused" or "Could not connect to database"
- Application starts but operations fail with database errors

#### Possible Causes and Solutions

1. **Database Server Not Running**
   - **Solution**: 
     - Verify PostgreSQL service is running
     - Start the service if it's stopped
     - Windows: `net start postgresql-x64-14` (adjust version as needed)

2. **Incorrect Connection Parameters**
   - **Solution**: 
     - The application uses hardcoded database configuration in `DBConnection.java`:
       - Database: `school_management_system`
       - Username: `postgres`
       - Password: `admin`
       - URL: `jdbc:postgresql://localhost:5432/school_management_system`
     - Verify PostgreSQL is running on localhost:5432
     - Ensure the database `school_management_system` exists
     - Test connection: `psql -U postgres -d school_management_system`

3. **Network Issues**
   - **Solution**: 
     - Check if the database server is reachable (ping the host)
     - Verify firewall settings allow connections to the database port
     - Check if the database is configured to accept remote connections

4. **Missing JDBC Driver**
   - **Symptom**: ClassNotFoundException for PostgreSQL driver
   - **Solution**: 
     - Verify the PostgreSQL JDBC driver is included in the classpath
     - Check the pom.xml for the correct dependency

5. **Database Schema Issues**
   - **Symptom**: Table or column not found errors
   - **Solution**: 
     - Run database migration scripts if available
     - Check if the database schema matches the expected structure
     - Verify table and column names match what the application expects

### Debugging Database Issues

1. **Enable Detailed SQL Logging**
   - Add the following to your logging configuration:
   ```
   logging.level.org.hibernate.SQL=DEBUG
   logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
   ```

2. **Test Direct Connection**
   - Use the following code to test database connection directly:
   ```java
   try {
       Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
       System.out.println("Database connection successful");
       conn.close();
   } catch (SQLException e) {
       System.err.println("Database connection failed: " + e.getMessage());
       e.printStackTrace();
   }
   ```

3. **Check Database Logs**
   - Review PostgreSQL logs for errors or connection issues
   - Default log location: PostgreSQL data directory/log

## Authentication Issues

### Login Failures

#### Symptoms
- Users cannot log in despite using correct credentials
- "Invalid username or password" errors
- JWT token validation failures

#### Possible Causes and Solutions

1. **Incorrect Credentials**
   - **Solution**: 
     - Reset the user's password in the database
     - Verify the username exists in the database

2. **JWT Secret Key Issues**
   - **Symptom**: Token validation errors
   - **Solution**: 
     - Check if the `JWT_SECRET` environment variable is set
     - Ensure the same secret key is used for token generation and validation
     - If the secret key has changed, all existing tokens will be invalid

3. **Token Expiration**
   - **Symptom**: "Token expired" error messages
   - **Solution**: 
     - Verify system clocks are synchronized
     - Check token expiration time in JwtUtil.java (default is 30 minutes)
     - Increase token expiration time if needed

4. **JWT Initialization Errors**
   - **Symptom**: Error messages like "NoClassDefFoundError: Could not initialize class com.canalprep.auth.utilities.JwtUtil" or "Unable to invoke class method io.jsonwebtoken.impl.crypto.MacProvider#generateKey"
   - **Solution**: 
     - Ensure all required JJWT dependencies are included in the pom.xml:
       - jjwt-api
       - jjwt-impl
       - jjwt-jackson
     - Rebuild the application with `mvn clean package`
     - Verify the JWT_SECRET environment variable is properly set

5. **CORS Issues**
   - **Symptom**: Authentication works in some browsers but not others
   - **Solution**: 
     - Check CORS configuration in MainApp.java
     - Ensure the client origin is allowed in the CORS filter

### Debugging Authentication Issues

1. **Inspect JWT Token**
   - Use [jwt.io](https://jwt.io/) to decode and verify tokens
   - Check if claims (userId, username, role) are correct
   - Verify the token signature with your secret key

2. **Enable Debug Logging**
   - Add debug logging statements in AuthenticationFilter.java
   - Log token extraction, parsing, and validation steps

3. **Test Authentication API Directly**
   - Use Postman to test the `/api/auth/login` endpoint
   - Verify the response contains a valid JWT token

## API Request Issues

### API Requests Failing

#### Symptoms
- HTTP error codes (400, 401, 403, 404, 500)
- Empty or unexpected responses
- Client-side error messages

#### Possible Causes and Solutions

1. **Authentication Issues**
   - **Symptom**: 401 Unauthorized or 403 Forbidden responses
   - **Solution**: 
     - Check if the Authorization header is included with the Bearer token
     - Verify the token is valid and not expired
     - Check if the user has the required role for the endpoint

2. **Invalid Request Format**
   - **Symptom**: 400 Bad Request responses
   - **Solution**: 
     - Verify the request body matches the expected format
     - Check for missing required fields
     - Ensure date formats are correct (ISO 8601 format recommended)

3. **Resource Not Found**
   - **Symptom**: 404 Not Found responses
   - **Solution**: 
     - Verify the URL path is correct
     - Check if the requested resource (student, attendance record) exists
     - Ensure IDs are valid and in the correct format

4. **Server Errors**
   - **Symptom**: 500 Internal Server Error responses
   - **Solution**: 
     - Check server logs for exceptions
     - Look for database connection issues
     - Verify business logic is handling edge cases correctly

### Debugging API Issues

1. **Use Postman for Testing**
   - Import the provided Postman collection
   - Test each endpoint with valid and invalid inputs
   - Examine response headers, body, and status codes

2. **Check Network Requests in Browser**
   - Use browser developer tools (F12) to inspect network requests
   - Look for errors in the Console tab
   - Verify request and response formats

3. **Enable CORS Logging**
   - Add logging to the CORS filter to debug cross-origin issues
   - Check if preflight OPTIONS requests are being handled correctly

## Frontend Issues

### UI Rendering Problems

#### Symptoms
- Elements not displaying correctly
- JavaScript errors in the console
- Forms not submitting properly

#### Possible Causes and Solutions

1. **Browser Compatibility**
   - **Solution**: 
     - Test in different browsers (Chrome, Firefox, Edge)
     - Check for browser-specific CSS or JavaScript issues
     - Use polyfills for older browsers if needed

2. **JavaScript Errors**
   - **Solution**: 
     - Check browser console (F12) for error messages
     - Fix syntax errors or undefined variables
     - Verify that required libraries are loaded

3. **CSS Issues**
   - **Solution**: 
     - Inspect elements to identify CSS problems
     - Check for conflicting styles or specificity issues
     - Verify that CSS files are being loaded correctly

4. **Data Loading Issues**
   - **Solution**: 
     - Check if API requests are completing successfully
     - Verify data format matches what the UI expects
     - Implement error handling for failed requests

### Debugging Frontend Issues

1. **Browser Developer Tools**
   - Use the Elements tab to inspect the DOM
   - Use the Network tab to monitor API requests
   - Use the Console tab to view JavaScript errors
   - Use the Sources tab to debug JavaScript code

2. **Add Console Logging**
   - Add `console.log()` statements to track execution flow
   - Log API responses to verify data structure
   - Log form values before submission

3. **Test with Minimal Configuration**
   - Disable browser extensions that might interfere
   - Try in incognito/private browsing mode
   - Clear browser cache and cookies

## Performance Issues

### Slow Response Times

#### Symptoms
- Pages take a long time to load
- API requests are slow to complete
- System becomes unresponsive under load

#### Possible Causes and Solutions

1. **Database Performance**
   - **Solution**: 
     - Optimize SQL queries in DAO classes
     - Add indexes to frequently queried columns
     - Implement database connection pooling

2. **Memory Leaks**
   - **Symptom**: Performance degrades over time
   - **Solution**: 
     - Monitor memory usage with tools like VisualVM
     - Check for objects that aren't being garbage collected
     - Restart the application periodically if needed

3. **Inefficient Algorithms**
   - **Solution**: 
     - Profile the application to identify bottlenecks
     - Optimize data processing logic
     - Consider caching frequently accessed data

4. **Network Latency**
   - **Solution**: 
     - Minimize the number of API requests
     - Batch operations when possible
     - Implement client-side caching

### Debugging Performance Issues

1. **Enable Performance Logging**
   - Add timing logs around critical operations
   - Track database query execution time
   - Monitor API request/response times

2. **Use Profiling Tools**
   - Java VisualVM for memory and CPU profiling
   - Browser performance tools for frontend issues
   - Database query analyzers for SQL performance

3. **Load Testing**
   - Use tools like JMeter to simulate multiple users
   - Identify breaking points under load
   - Test different configurations to optimize performance

## Advanced Debugging Techniques

### Remote Debugging

1. **Enable Remote Debugging**
   - Start the application with these JVM arguments:
   ```
   java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar school-management-system.jar
   ```

2. **Connect with IDE**
   - In IntelliJ IDEA or Eclipse, create a remote debugging configuration
   - Connect to localhost:5005 (or the configured address)
   - Set breakpoints and debug as if running locally

### Logging Configuration

1. **Increase Logging Level**
   - Set logging level to FINE, FINER, or FINEST for detailed logs
   - Focus on specific packages to avoid log flooding

2. **Log to File**
   - Configure file-based logging for persistent records
   - Rotate log files to manage disk space

3. **Structured Logging**
   - Include contextual information in logs (user ID, request ID)
   - Use a consistent format for easier parsing and analysis

## Common Error Messages and Solutions

### Database Errors

1. **"org.postgresql.util.PSQLException: ERROR: relation "students" does not exist"**
   - **Cause**: Database schema is missing or tables haven't been created
   - **Solution**: Run database initialization scripts or migrations

2. **"Connection refused. Check that the hostname and port are correct and that the postmaster is accepting TCP/IP connections."**
   - **Cause**: PostgreSQL server is not running or not accepting connections
   - **Solution**: Start PostgreSQL service and check pg_hba.conf for connection settings

### Authentication Errors

1. **"io.jsonwebtoken.SignatureException: JWT signature does not match locally computed signature"**
   - **Cause**: JWT token was signed with a different secret key
   - **Solution**: Ensure the same JWT_SECRET is used consistently

2. **"io.jsonwebtoken.ExpiredJwtException: JWT expired at..."**
   - **Cause**: Token has exceeded its expiration time
   - **Solution**: Obtain a new token by logging in again

### Password Recovery Issues

1. **"Password recovery email not received"**
   - **Cause**: Email configuration is incorrect or email service is down
   - **Solution**: 
     - Verify EMAIL_HOST, EMAIL_PORT, EMAIL_USERNAME, EMAIL_PASSWORD environment variables
     - Check email service provider settings (Gmail requires app-specific passwords)
     - Verify EMAIL_FROM address is valid
     - Check spam/junk folder

2. **"OTP has expired"**
   - **Cause**: More than 15 minutes have passed since OTP generation
   - **Solution**: Request a new password reset to generate a fresh OTP

3. **"Invalid OTP provided"**
   - **Cause**: Incorrect OTP entered or OTP has been used already
   - **Solution**: 
     - Double-check the 6-digit code from the email
     - Ensure OTP hasn't expired (15-minute limit)
     - Request a new OTP if needed

4. **"User not found for password recovery"**
   - **Cause**: Username doesn't exist in the system
   - **Solution**: Verify the correct username is being used

5. **"Email sending failed"**
   - **Cause**: SMTP configuration issues or network problems
   - **Solution**: 
     - Check email server connectivity
     - Verify SMTP credentials and settings
     - Check application logs for detailed error messages

### Application Errors

1. **"java.lang.ClassNotFoundException: org.postgresql.Driver"**
   - **Cause**: PostgreSQL JDBC driver is missing from the classpath
   - **Solution**: Add the PostgreSQL JDBC dependency to the project

2. **"java.lang.OutOfMemoryError: Java heap space"**
   - **Cause**: Application has exhausted available memory
   - **Solution**: Increase heap size with -Xmx parameter

## Preventive Maintenance

### Regular Checks

1. **Database Maintenance**
   - Run VACUUM and ANALYZE on PostgreSQL tables regularly
   - Monitor database size and growth
   - Back up the database daily

2. **Log Rotation**
   - Configure log rotation to prevent disk space issues
   - Archive old logs for historical analysis

3. **Performance Monitoring**
   - Set up monitoring for CPU, memory, and disk usage
   - Track response times and error rates
   - Establish baselines for normal operation

4. **Security Updates**
   - Keep the JVM and dependencies updated
   - Apply security patches promptly
   - Review and update CORS and security configurations

## Getting Additional Help

If you've tried the solutions in this guide and are still experiencing issues:

1. **Check Source Code**
   - Review the relevant code in the affected area
   - Look for similar issues in other parts of the application
   - Add additional logging to narrow down the problem

2. **Search for Known Issues**
   - Check project documentation for known issues
   - Search online for similar error messages or symptoms

3. **Contact Support**
   - Provide detailed information about the issue
   - Include relevant logs and error messages
   - Describe steps to reproduce the problem

---

This troubleshooting guide covers common issues and debugging techniques for the School Management System. For specific issues not covered here, please refer to the technical documentation or contact the development team.
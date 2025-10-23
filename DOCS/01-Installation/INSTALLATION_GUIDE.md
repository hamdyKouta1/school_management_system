# School Management System - Installation Guide

## System Requirements

### Hardware Requirements

- **Processor**: 2 GHz dual-core processor or better
- **Memory**: 4 GB RAM minimum (8 GB recommended)
- **Disk Space**: 1 GB for application, plus additional space for database

### Software Requirements

- **Operating System**: Windows, macOS, or Linux
- **Java**: JDK 11 or higher
- **Database**: PostgreSQL 12 or higher
- **Build Tool**: Apache Maven 3.6 or higher
- **Web Browser**: Chrome, Firefox, Edge, or Safari (latest versions)

## Prerequisites Installation

### Installing Java

1. Download JDK from [Oracle](https://www.oracle.com/java/technologies/javase-downloads.html) or [OpenJDK](https://adoptopenjdk.net/)
2. Run the installer and follow the instructions
3. Verify installation by running `java -version` in a terminal

### Installing PostgreSQL

1. Download PostgreSQL from [postgresql.org](https://www.postgresql.org/download/)
2. Run the installer and follow the instructions
3. Remember the password you set for the postgres user
4. Verify installation by running `psql -V` in a terminal

### Installing Maven

1. Download Maven from [maven.apache.org](https://maven.apache.org/download.cgi)
2. Extract the archive to a directory of your choice
3. Add the `bin` directory to your PATH environment variable
4. Verify installation by running `mvn -v` in a terminal

## Database Setup

### Creating the Database

1. Open a terminal or command prompt
2. Log in to PostgreSQL:
   ```
   psql -U postgres
   ```
3. Create a new database:
   ```sql
   CREATE DATABASE school_management_system;
   ```
4. Create a dedicated user for the application:
   ```sql
   CREATE USER postgres WITH ENCRYPTED PASSWORD 'admin';
   ```
5. Grant privileges to the user:
   ```sql
   GRANT ALL PRIVILEGES ON DATABASE school_management_system TO postgres;
   ```
6. Exit PostgreSQL:
   ```
   \q
   ```

### Initializing the Database Schema

The application will automatically create the necessary tables on first run, but you can also initialize the schema manually:

1. Connect to the database:
   ```
   psql -U postgres -d school_management_system
   ```
2. Run the SQL scripts from the `database` directory (if available):
   ```
   \i path/to/schema.sql
   ```

## Application Installation

### Method 1: Building from Source

1. Clone the repository or download the source code:
   ```
   git clone https://github.com/your-organization/school-management-system.git
   ```
   or extract the provided ZIP file

2. Navigate to the project directory:
   ```
   cd school-management-system
   ```

3. Build the application using Maven:
   ```
   mvn clean package
   ```
   This will create a JAR file in the `target` directory

### Method 2: Using Pre-built JAR

1. Download the pre-built JAR file from the provided source
2. Place it in a directory of your choice

## Configuration

### Environment Variables

The application uses environment variables for configuration. Set the following variables before running the application:

#### Database Configuration

The application uses the following hardcoded database configuration (as defined in `DBConnection.java`):

- **Database URL**: `jdbc:postgresql://localhost:5432/school_management_system`
- **Username**: `postgres`
- **Password**: `admin`
- **Driver**: PostgreSQL JDBC Driver (`org.postgresql.Driver`)

**Note**: The database configuration is currently hardcoded in the application. To change these settings, modify the `DBConnection.java` file.

#### Security Configuration

- `JWT_SECRET`: Secret key for JWT token signing
  - Should be a strong, random string of at least 32 characters
  - Example: `8f7a9d6e2c4b1a3f5e7d9c8b6a3f5e2d1c4b7a9e6d3c8f5a2e7d9c4b6a3f5e2d`

#### Email Configuration (for Password Recovery)

- `EMAIL_HOST`: SMTP server hostname (e.g., `smtp.gmail.com`)
- `EMAIL_PORT`: SMTP server port (e.g., `587` for TLS)
- `EMAIL_USERNAME`: Email account username
- `EMAIL_PASSWORD`: Email account password or app-specific password
- `EMAIL_FROM`: Sender email address for system notifications

**Note**: For Gmail, you may need to use an app-specific password instead of your regular password.

#### Server Configuration

The application runs on **port 8081** and binds to all network interfaces (0.0.0.0:8081) as configured in `MainApp.java`. The server configuration includes:

- **Port**: 8081 (hardcoded)
- **Host**: 0.0.0.0 (binds to all interfaces)
- **CORS**: Enabled for frontend integration
- **Authentication**: JWT-based with role-based access control

### Setting Environment Variables

#### Windows

1. Open Command Prompt as Administrator
2. Set environment variables:
   ```
   setx DB_URL "jdbc:postgresql://localhost:5432/school_management_system" /M
   setx DB_USER "postgres" /M
   setx DB_PASSWORD "admin" /M
   setx JWT_SECRET "your_secret_key" /M
   setx EMAIL_HOST "smtp.gmail.com" /M
   setx EMAIL_PORT "587" /M
   setx EMAIL_USERNAME "your_email@gmail.com" /M
   setx EMAIL_PASSWORD "your_app_password" /M
   setx EMAIL_FROM "noreply@yourschool.com" /M
   ```

#### macOS/Linux

1. Edit your shell profile file (`~/.bash_profile`, `~/.bashrc`, or `~/.zshrc`)
2. Add the following lines:
   ```
   export DB_URL="jdbc:postgresql://localhost:5432/school_management_system"
   export DB_USER="postgres"
   export DB_PASSWORD="admin"
   export JWT_SECRET="your_secret_key"
   export EMAIL_HOST="smtp.gmail.com"
   export EMAIL_PORT="587"
   export EMAIL_USERNAME="your_email@gmail.com"
   export EMAIL_PASSWORD="your_app_password"
   export EMAIL_FROM="noreply@yourschool.com"
   ```
3. Save the file and reload it:
   ```
   source ~/.bashrc
   ```
   (or the appropriate profile file)

## Running the Application

### Running from JAR

1. Open a terminal or command prompt
2. Navigate to the directory containing the JAR file
3. Run the application:
   ```
   java -jar school-management-system.jar
   ```
   
   **Note**: The application runs on port 8081 by default as configured in the code.
   ```

### Running in Development Mode

1. Navigate to the project directory
2. Run the application using Maven:
   ```
   mvn exec:java
   ```

## Verifying Installation

1. Once the application is running, you should see output indicating that the server has started
2. Open a web browser and navigate to:
   ```
   http://localhost:8081
   ```
3. You should see the login page of the School Management System

## Creating the First User

The first user must be created directly in the database:

1. Connect to the database:
   ```
   psql -U postgres -d school_management_system
   ```

2. Insert an admin user (password will be hashed automatically on first login):
   ```sql
   INSERT INTO users (username, email, password_hash, salt, created_at, role) 
   VALUES ('admin', 'admin@example.com', 'temporary', 'temporary', CURRENT_TIMESTAMP, 'ADMIN');
   ```

3. Log in with username `admin` and password `temporary`
4. You will be prompted to change your password on first login

## Deployment Options

### Running as a Service

#### Windows

1. Install [NSSM (Non-Sucking Service Manager)](https://nssm.cc/download)
2. Open Command Prompt as Administrator
3. Navigate to the NSSM directory
4. Install the service:
   ```
   nssm install SchoolManagementSystem
   ```
5. In the dialog that appears:
   - Set the path to the Java executable
   - Set the arguments to `-jar path\to\school-management-system.jar`
   - Configure environment variables in the Environment tab
   - Set startup directory to the location of the JAR file
6. Start the service:
   ```
   nssm start SchoolManagementSystem
   ```

#### Linux (systemd)

1. Create a service file:
   ```
   sudo nano /etc/systemd/system/school-management.service
   ```

2. Add the following content:
   ```
   [Unit]
   Description=School Management System
   After=network.target postgresql.service

   [Service]
   User=your_user
   WorkingDirectory=/path/to/application
   ExecStart=/usr/bin/java -jar /path/to/school-management-system.jar
   Environment="DB_URL=jdbc:postgresql://localhost:5432/school_management"
   Environment="DB_USER=school_app"
   Environment="DB_PASSWORD=your_password"
   Environment="JWT_SECRET=your_secret_key"
   Restart=always

   [Install]
   WantedBy=multi-user.target
   ```

3. Enable and start the service:
   ```
   sudo systemctl enable school-management
   sudo systemctl start school-management
   ```

### Deploying with Docker

If you prefer to use Docker:

1. Create a `Dockerfile` in the project directory:
   ```dockerfile
   FROM openjdk:11-jre-slim
   
   WORKDIR /app
   
   COPY target/school-management-system.jar /app/
   
   EXPOSE 8080
   
   CMD ["java", "-jar", "school-management-system.jar"]
   ```

2. Build the Docker image:
   ```
   docker build -t school-management-system .
   ```

3. Create a `docker-compose.yml` file:
   ```yaml
   version: '3'
   
   services:
     app:
       image: school-management-system
       ports:
         - "8080:8080"
       environment:
         - DB_URL=jdbc:postgresql://db:5432/school_management
         - DB_USER=school_app
         - DB_PASSWORD=your_password
         - JWT_SECRET=your_secret_key
       depends_on:
         - db
   
     db:
       image: postgres:12
       environment:
         - POSTGRES_DB=school_management
         - POSTGRES_USER=school_app
         - POSTGRES_PASSWORD=your_password
       volumes:
         - postgres_data:/var/lib/postgresql/data
   
   volumes:
     postgres_data:
   ```

4. Start the containers:
   ```
   docker-compose up -d
   ```

## Upgrading

To upgrade to a newer version of the application:

1. Stop the running application
2. Back up the database:
   ```
   pg_dump -U school_app -d school_management > backup.sql
   ```
3. Replace the JAR file with the new version
4. Start the application
5. The application will automatically update the database schema if needed

## Backup and Restore

### Database Backup

1. Create a backup of the PostgreSQL database:
   ```
   pg_dump -U school_app -d school_management > school_management_backup.sql
   ```

2. For scheduled backups, create a script and add it to cron (Linux) or Task Scheduler (Windows)

### Database Restore

1. Create a new database if needed:
   ```sql
   CREATE DATABASE school_management_new;
   GRANT ALL PRIVILEGES ON DATABASE school_management_new TO school_app;
   ```

2. Restore from backup:
   ```
   psql -U school_app -d school_management_new < school_management_backup.sql
   ```

3. Update the `DB_URL` environment variable to point to the new database if needed

## Troubleshooting Installation Issues

### Common Problems

1. **Database Connection Failure**
   - Verify PostgreSQL is running: `pg_isready`
   - Check connection parameters (hostname, port, database name, username, password)
   - Ensure the database user has appropriate permissions
   - Check if PostgreSQL is configured to accept connections from the application host

2. **Port Already in Use**
   - Check if another application is using port 8080 (or your custom port)
   - Change the port by setting the `PORT` environment variable

3. **Java Version Issues**
   - Verify Java version: `java -version`
   - Ensure you're using JDK 11 or higher

4. **Build Failures**
   - Check Maven configuration: `mvn -v`
   - Ensure all dependencies are available
   - Check network connectivity if downloading dependencies
   - For JWT-related errors, verify that the pom.xml includes all required JWT dependencies:
     ```xml
     <!-- JWT Dependencies -->
     <dependency>
         <groupId>io.jsonwebtoken</groupId>
         <artifactId>jjwt-api</artifactId>
         <version>0.11.5</version>
     </dependency>
     <dependency>
         <groupId>io.jsonwebtoken</groupId>
         <artifactId>jjwt-impl</artifactId>
         <version>0.11.5</version>
         <scope>runtime</scope>
     </dependency>
     <dependency>
         <groupId>io.jsonwebtoken</groupId>
         <artifactId>jjwt-jackson</artifactId>
         <version>0.11.5</version>
         <scope>runtime</scope>
     </dependency>
     ```

5. **License Issues**
   - If you encounter license validation errors, check system date and time
   - Delete the `license.dat` file to generate a new license

### Logs

Check the application logs for detailed error messages:

- Console output when running the application
- Application log files (if configured)
- PostgreSQL logs (typically in the PostgreSQL data directory)

## Support

If you encounter issues not covered in this guide, please contact the system administrator or development team for assistance.

---

This installation guide provides comprehensive instructions for setting up the School Management System. Follow each step carefully to ensure a successful installation.
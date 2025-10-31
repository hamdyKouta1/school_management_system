package com.canalprep.main;

import com.canalprep.auth.filter.AuthenticationFilter;
import com.canalprep.auth.servlets.AuthServlet;
import com.canalprep.dao.DBConnection;
import com.canalprep.license.scheduler.LicenseScheduler;
import com.canalprep.license.servlet.LicenseServlet;
import com.canalprep.scheduler.AttendanceScheduler;
import com.canalprep.otp.servlet.StandaloneOTPServlet;
import com.canalprep.license.service.LicenseService;
import com.canalprep.license.model.License;
import com.canalprep.exception.DataAccessException;
import com.canalprep.servlet.AddMedicalHistoryServlet;
import com.canalprep.servlet.AddQualificationsServlet;
import com.canalprep.servlet.AddStudentNoteServlet;
import com.canalprep.servlet.AddStudentPhoneServlet;
import com.canalprep.servlet.AttendanceServlet;
import com.canalprep.servlet.DashboardServlet;
import com.canalprep.servlet.InsertFullStudentServlet;
import com.canalprep.servlet.SchoolConfigServlet;
import com.canalprep.servlet.StudentServlet;
import com.canalprep.servlet.UserServlet;
import com.canalprep.utilities.LoggerUtil;
import com.canalprep.servlet.BulkStudentBatchServlet;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.MultipartConfigElement;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import java.sql.Connection;
import java.util.EnumSet;
import java.util.logging.LogManager;
import java.io.InputStream;

public class MainApp {
    private static AttendanceScheduler attendanceScheduler;
    
    public static void main(String[] args) throws Exception {
        // Initialize logging system first
        initializeLogging();
        LoggerUtil.logInfo("MainApp", "Starting School Management System...");
        
        // Test database connection
        testDatabaseConnection();

        // Validate license using new license service
        try {
            LicenseService licenseService = new LicenseService();
            License currentLicense = licenseService.getCurrentLicense();
            
            if (currentLicense == null || !currentLicense.isValid()) {
                LoggerUtil.logSecurity("LICENSE_STARTUP_WARNING", "system", 
                    "Starting system with invalid/expired license. Only license management endpoints will be accessible.");
                LoggerUtil.logInfo("MainApp", "System starting in license-restricted mode. Please renew license to access all features.");
            } else {
                LoggerUtil.logInfo("MainApp", "License validation successful. License expires on: " + currentLicense.getEndDate());
            }
            
        } catch (DataAccessException e) {
            LoggerUtil.logError("MainApp", "Failed to validate license during startup: " + e.getMessage(), e);
            LoggerUtil.logInfo("MainApp", "Continuing startup in license-restricted mode due to validation error.");
        }
        
        int port = 8081;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
 testDatabaseConnection();
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        connector.setHost("0.0.0.0");  // Bind to all network interfaces
        server.addConnector(connector);
        
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        
        // Pure backend API server - no static resources
        
        // Add CORS filter for frontend access
        FilterHolder corsFilter = new FilterHolder(CrossOriginFilter.class);
        corsFilter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        corsFilter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,POST,PUT,DELETE,OPTIONS");
        corsFilter.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin,ngrok-skip-browser-warning");
        corsFilter.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, "true");
        context.addFilter(corsFilter, "/*", EnumSet.of(DispatcherType.REQUEST));
        
        // Add Authentication Filter
        FilterHolder authFilterHolder = new FilterHolder(AuthenticationFilter.class);
        context.addFilter(authFilterHolder, "/api/protected/*", EnumSet.of(DispatcherType.REQUEST));
        context.addFilter(authFilterHolder, "/api/admin/*", EnumSet.of(DispatcherType.REQUEST));
        
        // Register servlets
        context.addServlet(new ServletHolder(new AuthServlet()), "/api/auth/*");
        // Add API servlets with correct URL patterns
        context.addServlet(new ServletHolder(new StudentServlet()), "/api/protected/students/*");
        context.addServlet(new ServletHolder(new AddStudentNoteServlet()), "/api/protected/student/note/*");
        context.addServlet(new ServletHolder(new AddStudentPhoneServlet()), "/api/protected/student/phone/*");

        context.addServlet(new ServletHolder(new AddMedicalHistoryServlet()), "/api/protected/student/medical/*");
        context.addServlet(new ServletHolder(new InsertFullStudentServlet()), "/api/protected/insertStudent/*");
        // Configure multipart for BulkStudentBatchServlet explicitly (Jetty requires holder registration)
        ServletHolder bulkHolder = new ServletHolder(new BulkStudentBatchServlet());
        context.addServlet(bulkHolder, "/api/protected/bulk-action/student-batch/*");
        bulkHolder.getRegistration().setMultipartConfig(new MultipartConfigElement(System.getProperty("java.io.tmpdir"), 50 * 1024 * 1024, 50 * 1024 * 1024, 0));
        context.addServlet(new ServletHolder(new DashboardServlet()), "/api/protected/dashboard/*");
        context.addServlet(new ServletHolder(new AddQualificationsServlet()), "/api/protected/addQ/*");
        context.addServlet(new ServletHolder(new SchoolConfigServlet()), "/api/protected/schoolConfig/*");
        
        // Attendance servlet - mixed protected/unprotected endpoints
        context.addServlet(new ServletHolder(new AttendanceServlet()), "/api/attendance/*");
        context.addServlet(new ServletHolder(new AttendanceServlet()), "/api/protected/attendance/*");
        
        // License management servlets
        context.addServlet(new ServletHolder(new LicenseServlet()), "/api/protected/licence");
        context.addServlet(new ServletHolder(new LicenseServlet()), "/api/protected/licence/renew");
        context.addServlet(new ServletHolder(new LicenseServlet()), "/api/protected/licence/remove");
        
        // Standalone OTP servlets
        context.addServlet(new ServletHolder(new StandaloneOTPServlet()), "/api/protected/otp/*");
        
        // User management servlet
        context.addServlet(new ServletHolder(new UserServlet()), "/api/protected/users/*");
        
        // Pure backend API - no default servlet needed
        
        server.setHandler(context);
        
        // Add shutdown hook for graceful cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                LoggerUtil.logInfo("MainApp", "Server shutdown initiated");
                
                // Stop license scheduler
                try {
                    LicenseScheduler.getInstance().stop();
                    LoggerUtil.logInfo("MainApp", "License scheduler stopped");
                } catch (Exception e) {
                    LoggerUtil.logError("MainApp", "Error stopping license scheduler", e);
                }
                
                // Stop attendance scheduler
                try {
                    if (attendanceScheduler != null) {
                        attendanceScheduler.stop();
                        LoggerUtil.logInfo("MainApp", "Attendance scheduler stopped");
                    }
                } catch (Exception e) {
                    LoggerUtil.logError("MainApp", "Error stopping attendance scheduler", e);
                }
                
                LoggerUtil.shutdown();
                if (server != null) {
                    server.stop();
                }
            } catch (Exception e) {
    
            }
        }));
        
        // Start the server
        server.start();
        
        // Start license scheduler after server is running
        try {
            LicenseScheduler.getInstance().start();
            LoggerUtil.logInfo("MainApp", "License scheduler started successfully");
        } catch (Exception e) {
            LoggerUtil.logError("MainApp", "Failed to start license scheduler", e);
        }
        
        // Start attendance scheduler after server is running
        try {
            attendanceScheduler = new AttendanceScheduler();
            attendanceScheduler.start();
            LoggerUtil.logInfo("MainApp", "Attendance scheduler started successfully - daily attendance updates at 12:00 PM");
        } catch (Exception e) {
            LoggerUtil.logError("MainApp", "Failed to start attendance scheduler", e);
        }
        
        LoggerUtil.logInfo("MainApp", "Backend API Server started on port " + port);
        LoggerUtil.logInfo("MainApp", "API Base URL: http://localhost:" + port + "/api");
        LoggerUtil.logInfo("MainApp", "Server endpoints registered successfully");
        LoggerUtil.logInfo("MainApp", "Log rotation is active - logs will be automatically rotated when they exceed 10MB");
        
        // Also log to console for immediate feedback
       
        server.join();
    }
    
    private static void testDatabaseConnection() {
        try (Connection conn = DBConnection.getConnection()) {
            LoggerUtil.logInfo("MainApp", "Database connection successful!");
            LoggerUtil.logDatabase("CONNECTION_TEST", "ALL", "Database connectivity verified");

        } catch (Exception e) {
            LoggerUtil.logError("MainApp", "Database connection failed! Continuing startup in degraded mode.", e);
        }
    }
    
    /**
     * Initialize the logging system by loading logging.properties
     */
    private static void initializeLogging() {
        try {
            // Load logging configuration from resources
            InputStream configStream = MainApp.class.getClassLoader().getResourceAsStream("logging.properties");
            if (configStream != null) {
                LogManager.getLogManager().readConfiguration(configStream);
                configStream.close();
            }
            
            // Initialize our custom logger utility
            LoggerUtil.initialize();
            
        } catch (Exception e) {

            // Continue without custom logging configuration
            LoggerUtil.initialize();
        }
    }
}
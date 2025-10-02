package com.canalprep.main;

import com.canalprep.auth.filter.AuthenticationFilter;
import com.canalprep.auth.servlets.AuthServlet;
import com.canalprep.dao.DBConnection;
import com.canalprep.servlet.AddMedicalHistoryServlet;
import com.canalprep.servlet.AddQualificationsServlet;
import com.canalprep.servlet.AddStudentNoteServlet;
import com.canalprep.servlet.AddStudentPhoneServlet;
import com.canalprep.servlet.AttendanceServlet;
import com.canalprep.servlet.DashboardServlet;
import com.canalprep.servlet.InsertFullStudentServlet;
import com.canalprep.servlet.StudentServlet;
import com.canalprep.utilities.LoggerUtil;
import jakarta.servlet.DispatcherType;
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
    public static void main(String[] args) throws Exception {
        // Initialize logging system first
        initializeLogging();
        LoggerUtil.logInfo("MainApp", "Starting School Management System...");
        
        // Test database connection
        testDatabaseConnection();

        // Validate license
        if (!com.canalprep.utilities.LicenseManager.isLicenseValid()) {
            LoggerUtil.logError("MainApp", "License is not valid. Exiting application.", null);
            System.exit(1);
        }
        LoggerUtil.logInfo("MainApp", "License validation successful");
        
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
        context.addServlet(new ServletHolder(new AttendanceServlet()), "/api/protected/attendance/*");
        context.addServlet(new ServletHolder(new AddMedicalHistoryServlet()), "/api/protected/student/medical/*");
        context.addServlet(new ServletHolder(new InsertFullStudentServlet()), "/api/protected/insertStudent/*");
        context.addServlet(new ServletHolder(new DashboardServlet()), "/api/protected/dashboard/*");
        context.addServlet(new ServletHolder(new AddQualificationsServlet()), "/api/protected/addQ/*");
        
        // Pure backend API - no default servlet needed
        
        server.setHandler(context);
        
        // Add shutdown hook for graceful cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                LoggerUtil.logInfo("MainApp", "Server shutdown initiated");
                LoggerUtil.shutdown();
                if (server != null) {
                    server.stop();
                }
            } catch (Exception e) {
                System.err.println("Error during shutdown: " + e.getMessage());
            }
        }));
        
        // Start the server
        server.start();
        LoggerUtil.logInfo("MainApp", "Backend API Server started on port " + port);
        LoggerUtil.logInfo("MainApp", "API Base URL: http://localhost:" + port + "/api");
        LoggerUtil.logInfo("MainApp", "Server endpoints registered successfully");
        LoggerUtil.logInfo("MainApp", "Log rotation is active - logs will be automatically rotated when they exceed 10MB");
        
        // Also log to console for immediate feedback
        System.out.println("✅ Backend API Server started on port " + port);
        System.out.println("📁 Logs are being written to: " + LoggerUtil.getLogDirectory());
        System.out.println("🔄 Log rotation is active - check logs/archive for rotated files");
       
        server.join();
    }
    
    private static void testDatabaseConnection() {
        try (Connection conn = DBConnection.getConnection()) {
            LoggerUtil.logInfo("MainApp", "Database connection successful!");
            LoggerUtil.logDatabase("CONNECTION_TEST", "ALL", "Database connectivity verified");
            System.out.println("✅ Database connection successful!");
        } catch (Exception e) {
            LoggerUtil.logError("MainApp", "Database connection failed!", e);
            System.err.println("❌ Database connection failed!");
            e.printStackTrace();
            System.exit(1);
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
            System.err.println("Warning: Could not initialize logging configuration: " + e.getMessage());
            // Continue without custom logging configuration
            LoggerUtil.initialize();
        }
    }
}
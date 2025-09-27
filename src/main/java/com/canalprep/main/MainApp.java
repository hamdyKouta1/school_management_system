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
import jakarta.servlet.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import java.sql.Connection;
import java.util.EnumSet;

public class MainApp {
    public static void main(String[] args) throws Exception {
        // Test database connection
     

        // Validate license
        if (!com.canalprep.utilities.LicenseManager.isLicenseValid()) {
            System.err.println("License is not valid. Exiting application.");
            System.exit(1);
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
        context.addServlet(new ServletHolder(new AttendanceServlet()), "/api/protected/attendance/*");
        context.addServlet(new ServletHolder(new AddMedicalHistoryServlet()), "/api/protected/student/medical/*");
        context.addServlet(new ServletHolder(new InsertFullStudentServlet()), "/api/protected/insertStudent/*");
        context.addServlet(new ServletHolder(new DashboardServlet()), "/api/protected/dashboard/*");
        context.addServlet(new ServletHolder(new AddQualificationsServlet()), "/api/protected/addQ/*");
        
        // Pure backend API - no default servlet needed
        
        server.setHandler(context);
        
        // Start the server
        server.start();
        System.out.println("Backend API Server started on port " + port);
        System.out.println("API Base URL: http://localhost:" + port + "/api");
       
        server.join();
    }
    
    private static void testDatabaseConnection() {
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("✅ Database connection successful!");
        } catch (Exception e) {
            System.err.println("❌ Database connection failed!");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
package com.canalprep.servlet;

import com.canalprep.auth.utilities.JwtUtil;
import com.canalprep.dao.StudentDAO;
import com.canalprep.model.Student;
import com.canalprep.service.QRCodeService;
import com.canalprep.service.QRCodeService.StudentQRData;
import com.canalprep.utilities.LoggerUtil;
import io.jsonwebtoken.Claims;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Servlet for generating and downloading QR codes for all students
 * Restricted to ADMIN and DEVELOPER roles only
 */
@WebServlet("/api/protected/students/qrcodes")
public class StudentQRCodeServlet extends HttpServlet {
    
    private final StudentDAO studentDAO = new StudentDAO();
    private final QRCodeService qrCodeService = new QRCodeService();
    
    private static final String TEMP_DIR_PREFIX = "student_qrcodes_";
    private static final String ZIP_FILENAME = "student_qrcodes.zip";
    private static final int BUFFER_SIZE = 4096;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // Validate authentication and authorization
            String userRole = validateUserAccess(request, response);
            if (userRole == null) {
                return; // Response already sent by validation method
            }
            
            LoggerUtil.logSecurity("QR_CODE_GENERATION_REQUEST", userRole, 
                "QR code generation requested by " + userRole + " from IP: " + getClientIP(request));
            
            // Get all students
            List<Student> students = studentDAO.getAllStudents();
            
            if (students == null || students.isEmpty()) {
                LoggerUtil.logInfo("StudentQRCodeServlet", "No students found for QR code generation");
                sendErrorResponse(response, "No students found in the system", HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            LoggerUtil.logInfo("StudentQRCodeServlet", "Generating QR codes for " + students.size() + " students");
            
            // Create temporary directory for QR code files
            Path tempDir = createTempDirectory();
            
            try {
                // Generate QR codes for all students
                generateQRCodesForStudents(students, tempDir);
                
                // Create ZIP file and send response
                sendZipResponse(response, tempDir);
                
                LoggerUtil.logSecurity("QR_CODE_GENERATION_SUCCESS", userRole, 
                    "Successfully generated QR codes for " + students.size() + " students");
                
            } finally {
                // Clean up temporary files
                cleanupTempDirectory(tempDir);
            }
            
        } catch (Exception e) {
            LoggerUtil.logError("StudentQRCodeServlet", "Error generating QR codes", e);
            sendErrorResponse(response, "Internal server error: " + e.getMessage(), 
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Validate user authentication and authorization
     * @param request HTTP request
     * @param response HTTP response
     * @return User role if valid, null if invalid
     */
    private String validateUserAccess(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            LoggerUtil.logSecurity("QR_CODE_ACCESS_DENIED", "UNKNOWN", 
                "QR code access denied - missing token from IP: " + getClientIP(request));
            sendErrorResponse(response, "Authorization token required", HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        
        try {
            String token = authHeader.substring(7);
            Claims claims = JwtUtil.parseToken(token);
            String userRole = claims.get("role", String.class);
            
            // Check if user has required role (ADMIN or DEVELOPER)
            if (!"ADMIN".equals(userRole) && !"DEVELOPER".equals(userRole)) {
                LoggerUtil.logSecurity("QR_CODE_ACCESS_DENIED", userRole, 
                    "QR code access denied - insufficient privileges for role: " + userRole + " from IP: " + getClientIP(request));
                sendErrorResponse(response, "Access denied. ADMIN or DEVELOPER role required", 
                    HttpServletResponse.SC_FORBIDDEN);
                return null;
            }
            
            return userRole;
            
        } catch (Exception e) {
            LoggerUtil.logSecurity("QR_CODE_ACCESS_DENIED", "UNKNOWN", 
                "QR code access denied - invalid token from IP: " + getClientIP(request));
            sendErrorResponse(response, "Invalid authorization token", HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
    }
    
    /**
     * Create temporary directory for QR code files
     * @return Path to temporary directory
     * @throws IOException if directory creation fails
     */
    private Path createTempDirectory() throws IOException {
        Path tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX);
        LoggerUtil.logInfo("StudentQRCodeServlet", "Created temporary directory: " + tempDir.toString());
        return tempDir;
    }
    
    /**
     * Generate QR codes for all students and save to temporary directory with hierarchical structure
     * @param students List of students
     * @param tempDir Temporary directory path
     * @throws Exception if QR code generation fails
     */
    private void generateQRCodesForStudents(List<Student> students, Path tempDir) throws Exception {
        int successCount = 0;
        int errorCount = 0;
        
        // Group students by grade and class
        Map<String, Map<String, List<Student>>> gradeClassMap = groupStudentsByGradeAndClass(students);
        
        for (Map.Entry<String, Map<String, List<Student>>> gradeEntry : gradeClassMap.entrySet()) {
            String gradeName = gradeEntry.getKey();
            Map<String, List<Student>> classMap = gradeEntry.getValue();
            
            // Create grade directory
            String sanitizedGradeName = QRCodeService.sanitizeFilename(gradeName);
            Path gradeDir = tempDir.resolve(sanitizedGradeName);
            
            try {
                Files.createDirectories(gradeDir);
                LoggerUtil.logInfo("StudentQRCodeServlet", "Created grade directory: " + sanitizedGradeName);
            } catch (IOException e) {
                LoggerUtil.logError("StudentQRCodeServlet", "Failed to create grade directory: " + sanitizedGradeName, e);
                continue;
            }
            
            for (Map.Entry<String, List<Student>> classEntry : classMap.entrySet()) {
                String className = classEntry.getKey();
                List<Student> classStudents = classEntry.getValue();
                
                // Create class directory under grade
                String sanitizedClassName = QRCodeService.sanitizeFilename(className);
                Path classDir = gradeDir.resolve(sanitizedClassName);
                
                try {
                    Files.createDirectories(classDir);
                    LoggerUtil.logInfo("StudentQRCodeServlet", "Created class directory: " + sanitizedGradeName + "/" + sanitizedClassName);
                } catch (IOException e) {
                    LoggerUtil.logError("StudentQRCodeServlet", "Failed to create class directory: " + sanitizedClassName, e);
                    continue;
                }
                
                // Generate QR codes for students in this class
                for (Student student : classStudents) {
                    try {
                        // Validate student data
                        if (student.getStudentName() == null || student.getStudentName().trim().isEmpty()) {
                            LoggerUtil.logInfo("StudentQRCodeServlet", "Skipping student with missing name, ID: " + student.getStudentId());
                            errorCount++;
                            continue;
                        }
                        
                        // Get primary phone number (first phone if available)
                        String phoneNumber = "";
                        if (student.getStudentPhones() != null && !student.getStudentPhones().isEmpty()) {
                            phoneNumber = student.getStudentPhones().get(0);
                        }
                        
                        // Create student QR data
                        StudentQRData qrData = new StudentQRData(
                            student.getStudentName(),
                            String.valueOf(student.getStudentId()),
                            phoneNumber
                        );
                        
                        // Sanitize filename
                        String sanitizedName = QRCodeService.sanitizeFilename(student.getStudentName());
                        String filename = sanitizedName + ".png";
                        Path qrFilePath = classDir.resolve(filename);
                        
                        // Generate QR code file
                        qrCodeService.generateQRCodeFile(qrData, qrFilePath);
                        successCount++;
                        
                    } catch (Exception e) {
                        LoggerUtil.logError("StudentQRCodeServlet", 
                            "Failed to generate QR code for student: " + student.getStudentName() + " (ID: " + student.getStudentId() + ")", e);
                        errorCount++;
                    }
                }
            }
        }
        
        LoggerUtil.logInfo("StudentQRCodeServlet", 
            "QR code generation completed. Success: " + successCount + ", Errors: " + errorCount);
        
        if (successCount == 0) {
            throw new Exception("Failed to generate any QR codes");
        }
    }
    
    /**
     * Group students by grade and then by class
     * @param students List of students to group
     * @return Map with grade as key, containing map of class as key and list of students as value
     */
    private Map<String, Map<String, List<Student>>> groupStudentsByGradeAndClass(List<Student> students) {
        Map<String, Map<String, List<Student>>> gradeClassMap = new LinkedHashMap<>();
        
        for (Student student : students) {
            // Use default values for missing grade/class information
            String gradeName = (student.getGradeName() != null && !student.getGradeName().trim().isEmpty()) 
                ? student.getGradeName().trim() 
                : "Unknown_Grade";
            String className = (student.getClassName() != null && !student.getClassName().trim().isEmpty()) 
                ? student.getClassName().trim() 
                : "Unknown_Class";
            
            // Get or create grade map
            Map<String, List<Student>> classMap = gradeClassMap.computeIfAbsent(gradeName, k -> new LinkedHashMap<>());
            
            // Get or create class list
            List<Student> classStudents = classMap.computeIfAbsent(className, k -> new ArrayList<>());
            
            // Add student to the appropriate class
            classStudents.add(student);
        }
        
        return gradeClassMap;
    }
    
    /**
     * Create ZIP file from temporary directory and send as response
     * @param response HTTP response
     * @param tempDir Temporary directory containing QR code files
     * @throws IOException if ZIP creation or response writing fails
     */
    private void sendZipResponse(HttpServletResponse response, Path tempDir) throws IOException {
        // Set response headers
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + ZIP_FILENAME + "\"");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        
        // Create ZIP file and stream to response
        try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
            
            Files.walk(tempDir)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        // Get relative path from temp directory to preserve hierarchy
                        Path relativePath = tempDir.relativize(file);
                        String entryName = relativePath.toString().replace('\\', '/'); // Ensure forward slashes for ZIP compatibility
                        
                        ZipEntry zipEntry = new ZipEntry(entryName);
                        zipOut.putNextEntry(zipEntry);
                        
                        Files.copy(file, zipOut);
                        zipOut.closeEntry();
                        
                        LoggerUtil.logInfo("StudentQRCodeServlet", "Added to ZIP: " + entryName);
                        
                    } catch (IOException e) {
                        LoggerUtil.logError("StudentQRCodeServlet", "Error adding file to ZIP: " + file.toString(), e);
                    }
                });
            
            zipOut.finish();
        }
        
        LoggerUtil.logInfo("StudentQRCodeServlet", "ZIP file sent successfully");
    }
    
    /**
     * Clean up temporary directory and all files
     * @param tempDir Temporary directory to clean up
     */
    private void cleanupTempDirectory(Path tempDir) {
        try {
            Files.walk(tempDir)
                .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        LoggerUtil.logError("StudentQRCodeServlet", "Failed to delete temp file: " + path.toString(), e);
                    }
                });
            
            LoggerUtil.logInfo("StudentQRCodeServlet", "Temporary directory cleaned up: " + tempDir.toString());
            
        } catch (IOException e) {
            LoggerUtil.logError("StudentQRCodeServlet", "Error cleaning up temporary directory: " + tempDir.toString(), e);
        }
    }
    
    /**
     * Send error response with JSON format
     * @param response HTTP response
     * @param message Error message
     * @param statusCode HTTP status code
     * @throws IOException if response writing fails
     */
    private void sendErrorResponse(HttpServletResponse response, String message, int statusCode) 
            throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String jsonResponse = String.format("{\"error\": \"%s\", \"status\": %d}", 
            message.replace("\"", "\\\""), statusCode);
        
        response.getWriter().write(jsonResponse);
    }
    
    /**
     * Get client IP address from request
     * @param request HTTP request
     * @return Client IP address
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP.trim();
        }
        
        return request.getRemoteAddr();
    }
}
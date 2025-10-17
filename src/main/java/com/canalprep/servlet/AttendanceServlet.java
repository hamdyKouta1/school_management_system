package com.canalprep.servlet;

import com.canalprep.dao.AttendanceDAO;
import com.canalprep.model.StudentAttendanceDetails;
import com.canalprep.exception.DataAccessException;
import com.canalprep.utilities.LoggerUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.canalprep.auth.utilities.JwtUtil;
import io.jsonwebtoken.Claims;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class AttendanceServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(AttendanceServlet.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        PrintWriter out = response.getWriter();
        
        try {
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/all")) {
                // Get all attendance records (protected endpoint)
                if (!isAuthenticated(request, response)) {
                    return;
                }
                List<Map<String, Object>> attendanceList = attendanceDAO.getAllAttendanceWithStudentDetails();
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(objectMapper.writeValueAsString(attendanceList));
                LoggerUtil.logInfo("AttendanceServlet", "Retrieved all attendance records");
                
            } else if (pathInfo.startsWith("/student/")) {
                // Get attendance for specific student by ID or name (protected endpoint)
                if (!isAuthenticated(request, response)) {
                    return;
                }
                String identifier = pathInfo.substring("/student/".length());
                
                try {
                    // Try to parse as student ID first
                    int studentId = Integer.parseInt(identifier);
                    Map<String, Object> studentAttendance = attendanceDAO.getStudentAttendanceById(studentId);
                    
                    if (studentAttendance != null) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.print(objectMapper.writeValueAsString(studentAttendance));
                        LoggerUtil.logInfo("AttendanceServlet", "Retrieved attendance for student ID: " + studentId);
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\":\"Student not found\"}");
                    }
                } catch (NumberFormatException e) {
                    // If not a number, treat as student name
                    Map<String, Object> studentAttendance = attendanceDAO.getStudentAttendanceByName(identifier);
                    
                    if (studentAttendance != null) {
                        response.setStatus(HttpServletResponse.SC_OK);
                        out.print(objectMapper.writeValueAsString(studentAttendance));
                        LoggerUtil.logInfo("AttendanceServlet", "Retrieved attendance for student name: " + identifier);
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\":\"Student not found\"}");
                    }
                }
                
            } else if (pathInfo.startsWith("/date/")) {
                // Get today's attendance by date (unprotected endpoint)
                String date = pathInfo.substring("/date/".length());
                List<Map<String, Object>> attendanceList = attendanceDAO.getAttendanceByDate(date);
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(objectMapper.writeValueAsString(attendanceList));
                LoggerUtil.logInfo("AttendanceServlet", "Retrieved attendance for date: " + date);
                
            } else if (pathInfo.equals("/grouped")) {
                // Get all students with their attendance records grouped (protected endpoint)
                if (!isAuthenticated(request, response)) {
                    return;
                }
                List<Map<String, Object>> groupedAttendance = attendanceDAO.getAllStudentsAttendanceGrouped();
                response.setStatus(HttpServletResponse.SC_OK);
                out.print(objectMapper.writeValueAsString(groupedAttendance));
                LoggerUtil.logInfo("AttendanceServlet", "Retrieved grouped attendance for all students");
                
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid endpoint\"}");
            }
            
        } catch (DataAccessException e) {
            LoggerUtil.logError("AttendanceServlet", "Database error in doGet: " + e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Database error: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            LoggerUtil.logError("AttendanceServlet", "Unexpected error in doGet: " + e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Internal server error\"}");
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Check authentication for protected endpoint
        if (!isAuthenticated(request, response)) {
            return;
        }
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // Read request body
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> attendanceData = objectMapper.readValue(sb.toString(), Map.class);
            
            // Create new attendance record
            boolean success = attendanceDAO.createAttendance(attendanceData);
            
            if (success) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print("{\"message\":\"Attendance record created successfully\"}");
                LoggerUtil.logInfo("AttendanceServlet", "Created new attendance record");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Failed to create attendance record\"}");
            }
            
        } catch (DataAccessException e) {
            LoggerUtil.logError("AttendanceServlet", "Database error in doPost: " + e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Database error: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            LoggerUtil.logError("AttendanceServlet", "Unexpected error in doPost: " + e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid request data\"}");
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Check authentication for protected endpoint
        if (!isAuthenticated(request, response)) {
            return;
        }
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // Read request body
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> updateData = objectMapper.readValue(sb.toString(), Map.class);
            
            // Update attendance record
            boolean success = attendanceDAO.updateAttendance(updateData);
            
            if (success) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"message\":\"Attendance record updated successfully\"}");
                LoggerUtil.logInfo("AttendanceServlet", "Updated attendance record");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Attendance record not found or update failed\"}");
            }
            
        } catch (DataAccessException e) {
            LoggerUtil.logError("AttendanceServlet", "Database error in doPut: " + e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Database error: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            LoggerUtil.logError("AttendanceServlet", "Unexpected error in doPut: " + e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid request data\"}");
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Check authentication for protected endpoint
        if (!isAuthenticated(request, response)) {
            return;
        }
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // Get student ID and date from request parameters
            String studentIdParam = request.getParameter("student_id");
            String attendanceDate = request.getParameter("attendance_date");
            
            if (studentIdParam == null || attendanceDate == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Missing required parameters: student_id and attendance_date\"}");
                return;
            }
            
            int studentId = Integer.parseInt(studentIdParam);
            
            // Delete attendance record
            boolean success = attendanceDAO.deleteAttendance(studentId, attendanceDate);
            
            if (success) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"message\":\"Attendance record deleted successfully\"}");
                LoggerUtil.logInfo("AttendanceServlet", "Deleted attendance record for student ID: " + studentId + ", date: " + attendanceDate);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Attendance record not found\"}");
            }
            
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid student ID format\"}");
        } catch (DataAccessException e) {
            LoggerUtil.logError("AttendanceServlet", "Database error in doDelete: " + e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Database error: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            LoggerUtil.logError("AttendanceServlet", "Unexpected error in doDelete: " + e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Internal server error\"}");
        } finally {
            out.flush();
        }
    }

    /**
     * Check if the request is authenticated
     */
    private boolean isAuthenticated(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // For protected endpoints, authentication is handled by AuthenticationFilter
        // Check if user attributes are set by the filter
        String userId = (String) request.getAttribute("userId");
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Authorization header missing\"}");
            return false;
        }
        return true;
    }
}
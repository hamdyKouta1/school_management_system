package com.canalprep.servlet;

import com.canalprep.dao.StudentDAO;
import com.canalprep.model.Student;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.stream.Collectors;
import org.json.JSONException;
import org.json.JSONObject;

public class InsertFullStudentServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("I'm Hereere");
        try (BufferedReader reader = request.getReader()) {
            // Efficiently read entire request body
            String requestBody = reader.lines().collect(Collectors.joining());
            JSONObject json = new JSONObject(requestBody); // Parse JSON
            Student student2 = new StudentDAO().buildStudentFromJson(json);
            // Create StudentDetails object from JSON
            
            int studentId = new StudentDAO().insertFullStudent(student2);

            // Return success response
            response.setContentType("application/json");
            response.getWriter().write("{\"student_id\": " + studentId + "}");

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        try {
            // Extract student ID from URL
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.split("/").length != 2) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Invalid path format. Use /students/{id}\"}");
                return;
            }

            int studentId = Integer.parseInt(pathInfo.split("/")[1]);

            // Read JSON body
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

            JSONObject json = new JSONObject(sb.toString());

            // Build Student object from JSON (same as insert)
            Student student = new StudentDAO().buildStudentFromJson(json);
            student.setStudentId(studentId); // Set ID for update

            // Perform update
            boolean success = new StudentDAO().updateFullStudent(student);

            if (success) {
                response.getWriter()
                        .write("{\"message\": \"Student updated successfully\", \"student_id\": " + studentId + "}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\": \"Update operation failed\"}");
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Invalid student ID format\"}");
        } catch (JSONException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Invalid JSON format\"}");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Database error: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Unexpected error: " + e.getMessage() + "\"}");
        }
    }


}
   
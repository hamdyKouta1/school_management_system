package com.canalprep.servlet;

import com.canalprep.dao.StudentDAO;
import com.canalprep.model.Student;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StudentServlet extends HttpServlet {
    private StudentDAO studentDAO = new StudentDAO();
    private ObjectMapper objectMapper = new ObjectMapper();

  
    @Override
protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");
    resp.setHeader("Access-Control-Allow-Origin", "*");

    try (PrintWriter out = resp.getWriter()) {
        String pathInfo = req.getPathInfo(); // Example: /123 or /count/grade/2
        List<String> pathParts = pathInfo == null ? new ArrayList<>() :
                Arrays.stream(pathInfo.split("/"))
                      .filter(part -> !part.isEmpty())
                      .collect(Collectors.toList());

        // Case 1: /students or /students/
        if (pathParts.isEmpty()) {
            List<Student> students = studentDAO.getAllStudents();
            out.print(objectMapper.writeValueAsString(students));
            return;
        }

        // Case 2: /students/count or /students/count/grade/1 or /students/count/class/2
        if ("count".equalsIgnoreCase(pathParts.get(0))) {
            int count = 0;
            if (pathParts.size() == 1) {
                count = studentDAO.countAllStudents();
            } else if (pathParts.size() == 3) {
                String type = pathParts.get(1).toLowerCase();
                String value = pathParts.get(2);
                try {
                    int id = Integer.parseInt(value);
                    if ("grade".equals(type)) {
                        count = studentDAO.countStudentsByGrade(id);
                    } else if ("class".equals(type)) {
                        count = studentDAO.countStudentsByClass(id);
                    } else {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\": \"Invalid filter type. Use 'grade' or 'class'.\"}");
                        return;
                    }
                } catch (NumberFormatException e) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"Invalid ID format for count filter.\"}");
                    return;
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid count endpoint format.\"}");
                return;
            }

            out.print("{\"count\": " + count + "}");
            return;
        }

        // Case 3: /students/GetByName?name=Ahmed
        if ("GetByName".equalsIgnoreCase(pathParts.get(0))) {
            String name = req.getParameter("name");
            if (name == null || name.isBlank()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Missing 'name' parameter\"}");
                return;
            }
            name = name.trim().replaceAll("\\s+", " ");
            List<Integer> studentIds = studentDAO.getStudentsByName(name);
            List<Student> students = studentDAO.getStudentByRangeOfId(studentIds);
            if (students != null && !students.isEmpty()) {
                out.print(objectMapper.writeValueAsString(students));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"No students found with that name\"}");
            }
            return;
        }

        // Case 4: /students/{id}
        if (pathParts.size() == 1) {
            try {
                int studentId = Integer.parseInt(pathParts.get(0));
                Student student = studentDAO.getStudentById(studentId);
                if (student != null) {
                    out.print(objectMapper.writeValueAsString(student));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\": \"Student not found\"}");
                }
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid student ID format\"}");
            }
            return;
        }

        // If path is not valid
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        out.print("{\"error\": \"Invalid request path\"}");

    } catch (Exception e) {
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.getWriter().print("{\"error\": \"" + e.getMessage() + "\"}");
    }
}

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        try {
            // Read JSON body
            BufferedReader reader = req.getReader();
            Student student = objectMapper.readValue(reader, Student.class);

            // Save student
            if (studentDAO.addStudent(student)) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().print(objectMapper.writeValueAsString(student));
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("{\"error\": \"Failed to create student\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo != null && pathInfo.split("/").length == 2) {
                int studentId = Integer.parseInt(pathInfo.split("/")[1]);

                // Read JSON body
                BufferedReader reader = req.getReader();
                Student student = objectMapper.readValue(reader, Student.class);
                student.setStudentId(studentId);

                // Update student
                if (studentDAO.updateStudent(student)) {
                    resp.getWriter().print(objectMapper.writeValueAsString(student));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().print("{\"error\": \"Student not found\"}");
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("{\"error\": \"Invalid request\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        try {
            // Get role from request attributes (set by JWT filter)
            String role = (String) req.getAttribute("role");

            // Check for admin role
            if (!"ADMIN".equals(role)) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write("{\"error\":\"Admin privileges required\"}");
                return;
            }

            String pathInfo = req.getPathInfo();
            if (pathInfo != null) {
                String[] parts = pathInfo.split("/");
                if (parts.length == 2) {
                    int studentId = Integer.parseInt(parts[1]);

                    if (studentDAO.deleteStudent(studentId)) {
                        resp.getWriter().print("{\"message\": \"Student and related data deleted successfully\"}");
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        resp.getWriter().print("{\"error\": \"Student not found\"}");
                    }
                    return;
                }
            }
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"error\": \"Invalid request format. Use /students/{id}\"}");

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"error\": \"Invalid student ID format\"}");

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("{\"error\": \"Server error: " + e.getMessage() + "\"}");
        }
    }
}
package com.canalprep.servlet;

import com.canalprep.dao.StudentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class DashboardServlet extends HttpServlet {

    private StudentDAO studentDAO = new StudentDAO();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        try (PrintWriter out = resp.getWriter()) {
            String pathInfo = req.getPathInfo(); // Example: /123 or /count/grade/2
            List<String> pathParts = pathInfo == null ? new ArrayList<>()
                    : Arrays.stream(pathInfo.split("/"))
                            .filter(part -> !part.isEmpty())
                            .collect(Collectors.toList());

            // Case 1: /students or /students/
            if (pathParts.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid filter type. Use 'countStudents' or 'countEmployees'.\"}");
                return;
            }

            // Case 2: /students/count or /students/count/grade/1 or /students/count/class/2
            if ("countStudents".equalsIgnoreCase(pathParts.get(0))) {
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
            if ("countEmployees".equalsIgnoreCase(pathParts.get(0))) {
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


            // If path is not valid
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Invalid request path\"}");

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

}
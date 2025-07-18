package com.canalprep.servlet;

import com.canalprep.dao.AttendanceDAO;
import com.canalprep.dao.StudentDAO;
import com.canalprep.model.Attendance;
import com.canalprep.model.Student;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

public class AttendanceServlet extends HttpServlet {
    private AttendanceDAO attendanceDAO = new AttendanceDAO();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        try (PrintWriter out = resp.getWriter()) {
            String pathInfo = req.getPathInfo(); // e.g., /getById/5 or /getByDate/2025-07-15

            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isEmpty()) {
                // GET ALL
                List<Attendance> attendanceList = attendanceDAO.getAllAttendance();
                out.print(objectMapper.writeValueAsString(attendanceList));

            } else {
                String[] parts = pathInfo.split("/");
                if (parts.length == 3) {
                    String command = parts[1];
                    String value = parts[2];

                    switch (command) {
                        case "getById":
                            try {
                                int attendanceId = Integer.parseInt(value);
                                List<Attendance> attendanceList = attendanceDAO.getAttendanceById(attendanceId);
System.out.println("attendanceList: " + attendanceList.size());
                                if (!attendanceList.isEmpty()) {
                                    out.print(objectMapper.writeValueAsString(attendanceList));
                                } else {
                                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                                    out.print("{\"error\": \"Attendance record not found\"}");
                                }
                            } catch (NumberFormatException e) {
                                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                                out.print("{\"error\": \"Invalid ID format\"}");
                            }

                            break;

                        case "getByDate":
                            try {
                                List<Attendance> records = attendanceDAO
                                        .getTodayAttendances(java.sql.Date.valueOf(value)); // assume DAO takes a String
                                                                                            // like "2025-07-15"
                                out.print(objectMapper.writeValueAsString(records));
                            } catch (Exception e) {
                                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                                out.print("{\"error\": \"Invalid date or no records found\"}");
                            }
                            break;

                        default:
                            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            out.print("{\"error\": \"Unsupported endpoint\"}");
                            break;
                    }
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"Invalid path format\"}");
                }
            }

            out.flush();
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
            Attendance attendance = objectMapper.readValue(reader, Attendance.class);
            Student student = new StudentDAO().getStudentById(attendance.getStudentId());

            attendance.setAttendanceDate(attendance.getAttendanceDate());
            attendance.setTodayDate(java.sql.Date.valueOf(LocalDate.now()));
            attendance.setStdClass(student.getClassName());
            attendance.setGrade(student.getGradeId());
            // Save attendance record
            if (attendanceDAO.addAttendance(attendance)) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().print(objectMapper.writeValueAsString(attendance));
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().print("{\"error\": \"Failed to create attendance record\"}");
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
                int attendanceId = Integer.parseInt(pathInfo.split("/")[1]);

                // Read JSON body
                BufferedReader reader = req.getReader();
                Attendance attendance = objectMapper.readValue(reader, Attendance.class);
                attendance.setAttendanceId(attendanceId);

                // Update attendance record
                if (attendanceDAO.updateAttendance(attendance)) {
                    resp.getWriter().print(objectMapper.writeValueAsString(attendance));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().print("{\"error\": \"Attendance record not found\"}");
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
            String pathInfo = req.getPathInfo();
            if (pathInfo != null && pathInfo.split("/").length == 2) {
                int attendanceId = Integer.parseInt(pathInfo.split("/")[1]);

                if (attendanceDAO.deleteAttendance(attendanceId)) {
                    resp.getWriter().print("{\"message\": \"Attendance record deleted\"}");
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().print("{\"error\": \"Attendance record not found\"}");
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
}
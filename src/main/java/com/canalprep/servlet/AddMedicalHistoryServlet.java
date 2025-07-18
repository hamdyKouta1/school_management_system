package com.canalprep.servlet;

import com.canalprep.dao.StudentDAO;
import com.canalprep.model.Student;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

import org.json.JSONObject;


public class AddMedicalHistoryServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        Student student = new Student();
        try {
            JSONObject json = new JSONObject(req.getReader().lines()
                .collect(Collectors.joining()));
            
            student.setStudentId(json.getInt("student_id"));
            System.out.println(json);
            student.setMedicalDescriptions(json.getString("description"));
            System.out.println(json.getString("description"));
            
            boolean success = new StudentDAO().addMedicalHistory(student);
            
            if (success) {
                resp.getWriter().write("{\"message\": \"Medical history added successfully\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\": \"Failed to add medical history\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }


    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.setHeader("Access-Control-Allow-Origin", "*");
            Student student = new Student();
            try {
                JSONObject json = new JSONObject(req.getReader().lines()
                    .collect(Collectors.joining()));
                
                student.setStudentId(json.getInt("student_id"));
                student.setMedicalDescriptions(json.getString("description"));
                
                
                boolean success = new StudentDAO().deleteMedicalHistory(student);
                
                if (success) {
                    resp.getWriter().write("{\"message\": \"Medical history deleted successfully\"}");
                } else {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write("{\"error\": \"Failed to deleted medical history\"}");
                }
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
            }
        }


}
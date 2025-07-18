package com.canalprep.servlet;

import com.canalprep.dao.StudentDAO;

import com.canalprep.model.Student;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;



public class AddStudentPhoneServlet extends HttpServlet {
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
             JSONArray phoneArray = json.getJSONArray("student_phones");
            List<String> phones = new ArrayList<>();
            for (int i = 0; i < phoneArray.length(); i++) {
                phones.add(phoneArray.getString(i));
            }
            student.setStudentPhones(phones);
            
            boolean success = new StudentDAO().addStudentPhone(student);
            
            if (success) {
                resp.getWriter().write("{\"message\": \"Phone added successfully\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\": \"Failed to add phone\"}");
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
            
            List<String> phones = new ArrayList<>();
            
            phones.add(json.getString("phone_number"));
     
            student.setStudentPhones(phones);
            student.setStudentId(json.getInt("student_id"));
                        
            boolean success = new StudentDAO().deleteStudentPhone(student);
            
            if (success) {
                resp.getWriter().write("{\"message\": \"Phone deleted successfully\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\": \"Failed to delete phone\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
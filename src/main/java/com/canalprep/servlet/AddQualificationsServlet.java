package com.canalprep.servlet;

import com.canalprep.dao.StudentDAO;
import com.canalprep.model.AdditionalQualification;
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

public class AddQualificationsServlet extends HttpServlet {

    @Override
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

            JSONArray qualArray = json.getJSONArray("qualifications");
            List<AdditionalQualification> qualificationList = new ArrayList<>();

            for (int i = 0; i < qualArray.length(); i++) {
                JSONObject qualObj = qualArray.getJSONObject(i);
                AdditionalQualification qualification = new AdditionalQualification();
                qualification.setDescription(qualObj.getString("description"));
                qualificationList.add(qualification);
            }

            student.setAdditionalQualifications(qualificationList);

            boolean success = new StudentDAO().addStudentAdditionalQualification(student);

            if (success) {
                resp.getWriter().write("{\"message\": \"Qualification(s) added successfully\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\": \"Failed to add qualifications\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        Student student = new Student();

        try {
            JSONObject json = new JSONObject(req.getReader().lines()
                    .collect(Collectors.joining()));

            List<AdditionalQualification> qualificationList = new ArrayList<>();
            AdditionalQualification qualification = new AdditionalQualification();
            qualification.setQualification_id(json.getInt("qualification_id"));
            qualificationList.add(qualification);

            student.setAdditionalQualifications(qualificationList);

            boolean success = new StudentDAO().deleteStudentQualification(student);

            if (success) {
                resp.getWriter().write("{\"message\": \"Qualification deleted successfully\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\": \"Failed to delete qualification\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}

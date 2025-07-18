package com.canalprep.servlet;

import com.canalprep.dao.StudentDAO;
import com.canalprep.model.Notes;
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

public class AddStudentNoteServlet extends HttpServlet {
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

            JSONArray notArray = json.getJSONArray("student_notes");
            List<Notes> notesList = new ArrayList<>();

            for (int i = 0; i < notArray.length(); i++) {
                JSONObject noteObj = notArray.getJSONObject(i);
                Notes newNote = new Notes();
                newNote.setNoteText(noteObj.getString("note_text"));
                System.out.println(noteObj.getString("note_text"));
                newNote.setCreatedBy(noteObj.getString("created_by"));
                System.out.println(noteObj.getString("created_by")); // Make sure this matches
                notesList.add(newNote);
            }

            System.out.println(notesList);
            student.setStudentNotes(notesList);

            boolean success = new StudentDAO().addStudentNote(student);

            if (success) {
                resp.getWriter().write("{\"message\": \"Note added successfully\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\": \"Failed to add note\"}");
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

            // student.setStudentId(json.getInt("student_id"));

            List<Notes> notesList = new ArrayList<>();

            Notes newNote = new Notes();
            newNote.setNoteId(json.getInt("note_id"));// Make sure this matches
            notesList.add(newNote);

            student.setStudentNotes(notesList);

            boolean success = new StudentDAO().deleteStudentNote(student);

            if (success) {
                resp.getWriter().write("{\"message\": \"Note Deleted successfully\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\": \"Failed to Delete note\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
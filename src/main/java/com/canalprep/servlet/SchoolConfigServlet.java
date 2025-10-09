package com.canalprep.servlet;

import com.canalprep.dao.*;
import com.canalprep.model.*;
import com.canalprep.utilities.LoggerUtil;
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

public class SchoolConfigServlet extends HttpServlet {
    private SchoolClassDAO schoolClassDAO = new SchoolClassDAO();
    private GradeLevelDAO gradeLevelDAO = new GradeLevelDAO();
    private NationalityDAO nationalityDAO = new NationalityDAO();
    private ReligionDAO religionDAO = new ReligionDAO();
    private RelationshipTypeDAO relationshipTypeDAO = new RelationshipTypeDAO();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        
        try (PrintWriter out = resp.getWriter()) {
            String pathInfo = req.getPathInfo(); // Example: /class or /class/1
            List<String> pathParts = pathInfo == null ? new ArrayList<>() :
                    Arrays.stream(pathInfo.split("/"))
                          .filter(part -> !part.isEmpty())
                          .collect(Collectors.toList());
            
            if (pathParts.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Configuration type is required. Use /class, /grade, /nationality, /religion, or /relationship\"}");
                return;
            }
            
            String configType = pathParts.get(0).toLowerCase();
            
            switch (configType) {
                case "class":
                    handleClassGet(pathParts, out, resp);
                    break;
                case "grade":
                    handleGradeGet(pathParts, out, resp);
                    break;
                case "nationality":
                    handleNationalityGet(pathParts, out, resp);
                    break;
                case "religion":
                    handleReligionGet(pathParts, out, resp);
                    break;
                case "relationship":
                    handleRelationshipGet(pathParts, out, resp);
                    break;
                default:
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"Invalid configuration type. Use /class, /grade, /nationality, /religion, or /relationship\"}");
            }
            
        } catch (Exception e) {
            LoggerUtil.logError("SchoolConfigServlet", "Error in doGet: " + e.getMessage(), e);
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
            String pathInfo = req.getPathInfo();
            List<String> pathParts = pathInfo == null ? new ArrayList<>() :
                    Arrays.stream(pathInfo.split("/"))
                          .filter(part -> !part.isEmpty())
                          .collect(Collectors.toList());
            
            if (pathParts.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("{\"error\": \"Configuration type is required\"}");
                return;
            }
            
            String configType = pathParts.get(0).toLowerCase();
            BufferedReader reader = req.getReader();
            
            switch (configType) {
                case "class":
                    handleClassPost(reader, resp);
                    break;
                case "grade":
                    handleGradePost(reader, resp);
                    break;
                case "nationality":
                    handleNationalityPost(reader, resp);
                    break;
                case "religion":
                    handleReligionPost(reader, resp);
                    break;
                case "relationship":
                    handleRelationshipPost(reader, resp);
                    break;
                default:
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().print("{\"error\": \"Invalid configuration type\"}");
            }
            
        } catch (Exception e) {
            LoggerUtil.logError("SchoolConfigServlet", "Error in doPost: " + e.getMessage(), e);
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
            List<String> pathParts = pathInfo == null ? new ArrayList<>() :
                    Arrays.stream(pathInfo.split("/"))
                          .filter(part -> !part.isEmpty())
                          .collect(Collectors.toList());
            
            if (pathParts.size() < 2) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("{\"error\": \"Configuration type and ID are required\"}");
                return;
            }
            
            String configType = pathParts.get(0).toLowerCase();
            int id = Integer.parseInt(pathParts.get(1));
            BufferedReader reader = req.getReader();
            
            switch (configType) {
                case "class":
                    handleClassPut(id, reader, resp);
                    break;
                case "grade":
                    handleGradePut(id, reader, resp);
                    break;
                case "nationality":
                    handleNationalityPut(id, reader, resp);
                    break;
                case "religion":
                    handleReligionPut(id, reader, resp);
                    break;
                case "relationship":
                    handleRelationshipPut(id, reader, resp);
                    break;
                default:
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().print("{\"error\": \"Invalid configuration type\"}");
            }
            
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"error\": \"Invalid ID format\"}");
        } catch (Exception e) {
            LoggerUtil.logError("SchoolConfigServlet", "Error in doPut: " + e.getMessage(), e);
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
            List<String> pathParts = pathInfo == null ? new ArrayList<>() :
                    Arrays.stream(pathInfo.split("/"))
                          .filter(part -> !part.isEmpty())
                          .collect(Collectors.toList());
            
            if (pathParts.size() < 2) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().print("{\"error\": \"Configuration type and ID are required\"}");
                return;
            }
            
            String configType = pathParts.get(0).toLowerCase();
            int id = Integer.parseInt(pathParts.get(1));
            
            switch (configType) {
                case "class":
                    handleClassDelete(id, resp);
                    break;
                case "grade":
                    handleGradeDelete(id, resp);
                    break;
                case "nationality":
                    handleNationalityDelete(id, resp);
                    break;
                case "religion":
                    handleReligionDelete(id, resp);
                    break;
                case "relationship":
                    handleRelationshipDelete(id, resp);
                    break;
                default:
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().print("{\"error\": \"Invalid configuration type\"}");
            }
            
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().print("{\"error\": \"Invalid ID format\"}");
        } catch (Exception e) {
            LoggerUtil.logError("SchoolConfigServlet", "Error in doDelete: " + e.getMessage(), e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    // Class handlers
    private void handleClassGet(List<String> pathParts, PrintWriter out, HttpServletResponse resp) throws Exception {
        if (pathParts.size() == 1) {
            // Get all classes
            List<SchoolClass> classes = schoolClassDAO.getAllClasses();
            out.print(objectMapper.writeValueAsString(classes));
        } else if (pathParts.size() == 2) {
            // Get class by ID
            int classId = Integer.parseInt(pathParts.get(1));
            SchoolClass schoolClass = schoolClassDAO.getClassById(classId);
            if (schoolClass != null) {
                out.print(objectMapper.writeValueAsString(schoolClass));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"Class not found\"}");
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Invalid request path\"}");
        }
    }
    
    private void handleClassPost(BufferedReader reader, HttpServletResponse resp) throws Exception {
        SchoolClass schoolClass = objectMapper.readValue(reader, SchoolClass.class);
        int id = schoolClassDAO.createClass(schoolClass);
        if (id > 0) {
            LoggerUtil.logInfo("SchoolConfigServlet", "New class created: " + schoolClass.getClassName());
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().print(objectMapper.writeValueAsString(schoolClass));
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("{\"error\": \"Failed to create class\"}");
        }
    }
    
    private void handleClassPut(int id, BufferedReader reader, HttpServletResponse resp) throws Exception {
        SchoolClass schoolClass = objectMapper.readValue(reader, SchoolClass.class);
        schoolClass.setClassId(id);
        if (schoolClassDAO.updateClass(schoolClass)) {
            LoggerUtil.logInfo("SchoolConfigServlet", "Class updated: ID " + id);
            resp.getWriter().print(objectMapper.writeValueAsString(schoolClass));
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().print("{\"error\": \"Class not found\"}");
        }
    }
    
    private void handleClassDelete(int id, HttpServletResponse resp) throws Exception {
        if (schoolClassDAO.deleteClass(id)) {
            LoggerUtil.logInfo("SchoolConfigServlet", "Class deleted: ID " + id);
            resp.getWriter().print("{\"message\": \"Class deleted successfully\"}");
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().print("{\"error\": \"Class not found\"}");
        }
    }
    
    // Grade handlers
    private void handleGradeGet(List<String> pathParts, PrintWriter out, HttpServletResponse resp) throws Exception {
        if (pathParts.size() == 1) {
            List<GradeLevel> grades = gradeLevelDAO.getAllGradeLevels();
            out.print(objectMapper.writeValueAsString(grades));
        } else if (pathParts.size() == 2) {
            int gradeId = Integer.parseInt(pathParts.get(1));
            GradeLevel grade = gradeLevelDAO.getGradeLevelById(gradeId);
            if (grade != null) {
                out.print(objectMapper.writeValueAsString(grade));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"Grade not found\"}");
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Invalid request path\"}");
        }
    }
    
    private void handleGradePost(BufferedReader reader, HttpServletResponse resp) throws Exception {
        GradeLevel grade = objectMapper.readValue(reader, GradeLevel.class);
        int id = gradeLevelDAO.createGradeLevel(grade);
        if (id > 0) {
            LoggerUtil.logInfo("SchoolConfigServlet", "New grade created: " + grade.getGradeNameEn());
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().print(objectMapper.writeValueAsString(grade));
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("{\"error\": \"Failed to create grade\"}");
        }
    }
    
    private void handleGradePut(int id, BufferedReader reader, HttpServletResponse resp) throws Exception {
        GradeLevel grade = objectMapper.readValue(reader, GradeLevel.class);
        grade.setGradeId(id);
        if (gradeLevelDAO.updateGradeLevel(grade)) {
            LoggerUtil.logInfo("SchoolConfigServlet", "Grade updated: ID " + id);
            resp.getWriter().print(objectMapper.writeValueAsString(grade));
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().print("{\"error\": \"Grade not found\"}");
        }
    }
    
    private void handleGradeDelete(int id, HttpServletResponse resp) throws Exception {
        if (gradeLevelDAO.deleteGradeLevel(id)) {
            LoggerUtil.logInfo("SchoolConfigServlet", "Grade deleted: ID " + id);
            resp.getWriter().print("{\"message\": \"Grade deleted successfully\"}");
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().print("{\"error\": \"Grade not found\"}");
        }
    }
    
    // Nationality handlers
    private void handleNationalityGet(List<String> pathParts, PrintWriter out, HttpServletResponse resp) throws Exception {
        if (pathParts.size() == 1) {
            List<Nationality> nationalities = nationalityDAO.getAllNationalities();
            out.print(objectMapper.writeValueAsString(nationalities));
        } else if (pathParts.size() == 2) {
            int nationalityId = Integer.parseInt(pathParts.get(1));
            Nationality nationality = nationalityDAO.getNationalityById(nationalityId);
            if (nationality != null) {
                out.print(objectMapper.writeValueAsString(nationality));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"Nationality not found\"}");
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Invalid request path\"}");
        }
    }
    
    private void handleNationalityPost(BufferedReader reader, HttpServletResponse resp) throws Exception {
        Nationality nationality = objectMapper.readValue(reader, Nationality.class);
        int id = nationalityDAO.createNationality(nationality);
        if (id > 0) {
            LoggerUtil.logInfo("SchoolConfigServlet", "New nationality created: " + nationality.getNationalName());
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().print(objectMapper.writeValueAsString(nationality));
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("{\"error\": \"Failed to create nationality\"}");
        }
    }
    
    private void handleNationalityPut(int id, BufferedReader reader, HttpServletResponse resp) throws Exception {
        Nationality nationality = objectMapper.readValue(reader, Nationality.class);
        nationality.setNationalityId(id);
        if (nationalityDAO.updateNationality(nationality)) {
            LoggerUtil.logInfo("SchoolConfigServlet", "Nationality updated: ID " + id);
            resp.getWriter().print(objectMapper.writeValueAsString(nationality));
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().print("{\"error\": \"Nationality not found\"}");
        }
    }
    
    private void handleNationalityDelete(int id, HttpServletResponse resp) throws Exception {
        if (nationalityDAO.deleteNationality(id)) {
            LoggerUtil.logInfo("SchoolConfigServlet", "Nationality deleted: ID " + id);
            resp.getWriter().print("{\"message\": \"Nationality deleted successfully\"}");
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().print("{\"error\": \"Nationality not found\"}");
        }
    }
    
    // Religion handlers
    private void handleReligionGet(List<String> pathParts, PrintWriter out, HttpServletResponse resp) throws Exception {
        if (pathParts.size() == 1) {
            List<Religion> religions = religionDAO.getAllReligions();
            out.print(objectMapper.writeValueAsString(religions));
        } else if (pathParts.size() == 2) {
            int religionId = Integer.parseInt(pathParts.get(1));
            Religion religion = religionDAO.getReligionById(religionId);
            if (religion != null) {
                out.print(objectMapper.writeValueAsString(religion));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"Religion not found\"}");
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Invalid request path\"}");
        }
    }
    
    private void handleReligionPost(BufferedReader reader, HttpServletResponse resp) throws Exception {
        Religion religion = objectMapper.readValue(reader, Religion.class);
        int id = religionDAO.createReligion(religion);
        if (id > 0) {
            LoggerUtil.logInfo("SchoolConfigServlet", "New religion created: " + religion.getReligionName());
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().print(objectMapper.writeValueAsString(religion));
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("{\"error\": \"Failed to create religion\"}");
        }
    }
    
    private void handleReligionPut(int id, BufferedReader reader, HttpServletResponse resp) throws Exception {
        Religion religion = objectMapper.readValue(reader, Religion.class);
        religion.setReligionId(id);
        if (religionDAO.updateReligion(religion)) {
            LoggerUtil.logInfo("SchoolConfigServlet", "Religion updated: ID " + id);
            resp.getWriter().print(objectMapper.writeValueAsString(religion));
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().print("{\"error\": \"Religion not found\"}");
        }
    }
    
    private void handleReligionDelete(int id, HttpServletResponse resp) throws Exception {
        if (religionDAO.deleteReligion(id)) {
            LoggerUtil.logInfo("SchoolConfigServlet", "Religion deleted: ID " + id);
            resp.getWriter().print("{\"message\": \"Religion deleted successfully\"}");
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().print("{\"error\": \"Religion not found\"}");
        }
    }
    
    // Relationship handlers
    private void handleRelationshipGet(List<String> pathParts, PrintWriter out, HttpServletResponse resp) throws Exception {
        if (pathParts.size() == 1) {
            List<RelationshipType> relationships = relationshipTypeDAO.getAllRelationshipTypes();
            out.print(objectMapper.writeValueAsString(relationships));
        } else if (pathParts.size() == 2) {
            int typeId = Integer.parseInt(pathParts.get(1));
            RelationshipType relationship = relationshipTypeDAO.getRelationshipTypeById(typeId);
            if (relationship != null) {
                out.print(objectMapper.writeValueAsString(relationship));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"Relationship type not found\"}");
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Invalid request path\"}");
        }
    }
    
    private void handleRelationshipPost(BufferedReader reader, HttpServletResponse resp) throws Exception {
        RelationshipType relationship = objectMapper.readValue(reader, RelationshipType.class);
        int id = relationshipTypeDAO.createRelationshipType(relationship);
        if (id > 0) {
            LoggerUtil.logInfo("SchoolConfigServlet", "New relationship type created: " + relationship.getTypeName());
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().print(objectMapper.writeValueAsString(relationship));
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().print("{\"error\": \"Failed to create relationship type\"}");
        }
    }
    
    private void handleRelationshipPut(int id, BufferedReader reader, HttpServletResponse resp) throws Exception {
        RelationshipType relationship = objectMapper.readValue(reader, RelationshipType.class);
        relationship.setTypeId(id);
        if (relationshipTypeDAO.updateRelationshipType(relationship)) {
            LoggerUtil.logInfo("SchoolConfigServlet", "Relationship type updated: ID " + id);
            resp.getWriter().print(objectMapper.writeValueAsString(relationship));
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().print("{\"error\": \"Relationship type not found\"}");
        }
    }
    
    private void handleRelationshipDelete(int id, HttpServletResponse resp) throws Exception {
        if (relationshipTypeDAO.deleteRelationshipType(id)) {
            LoggerUtil.logInfo("SchoolConfigServlet", "Relationship type deleted: ID " + id);
            resp.getWriter().print("{\"message\": \"Relationship type deleted successfully\"}");
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().print("{\"error\": \"Relationship type not found\"}");
        }
    }
}
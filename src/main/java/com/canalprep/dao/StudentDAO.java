package com.canalprep.dao;

import com.canalprep.model.AdditionalQualification;
import com.canalprep.model.Notes;
import com.canalprep.model.ParentDetails;
import com.canalprep.model.Student;
import com.canalprep.staticVariables.DBConst;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.*;
import com.canalprep.exception.DataAccessException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.postgresql.util.PGobject;

public class StudentDAO {
    private static final Logger logger = Logger.getLogger(StudentDAO.class.getName());
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SELECT_ALL = DBConst.DB_SELEC_STRING + ";";
    private static final String SELECT_BY_ID = DBConst.DB_SELECT_BY_ID;
    private static final String SELECT_BY_RANGE_OF_ID = DBConst.DB_SELECT_BY_RANGE_OF_ID;
    private static final String INSERT = DBConst.DB_INSERT;
    private static final String UPDATE = DBConst.DB_UPDATE;
    private static final String DELETE_STUDENT_FUNCTION = DBConst.DB_DELETE_BY_ID;
    private static final String UPDATE_STUDENT_DATA = DBConst.DB_UPDATE_STUDENT;
    private static final String SELECT_BY_STUDENT_NAME = DBConst.DB_SELECT_STUDENT_BY_NAME;



    public int countAllStudents() {
        String sql = "SELECT COUNT(*) FROM students";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error counting all students", e);
        }
        return 0;
    }

    public int countStudentsByGrade(int gradeId) {
        String sql = "SELECT COUNT(*) FROM students WHERE grade_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, gradeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error counting students by grade", e);
        }
        return 0;
    }

    public int countStudentsByClass(int classId) {
        String sql = "SELECT COUNT(*) FROM students WHERE class_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, classId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error counting students by class", e);
        }
        return 0;
    }

    public List<Integer> getStudentsByName(String name) {
        List<Integer> ids = new ArrayList<>();

        // Validate input
        if (name == null || name.trim().isEmpty()) {
            return ids; // Return empty list for invalid input
        }

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_STUDENT_NAME)) {

            // Correct parameter setting (no manual quoting)
            pstmt.setString(1, "%" + name + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("s_id"));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting students by name", e);
        }
        return ids;
    }

    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SELECT_ALL);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                students.add(extractStudentFromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting all students", e);
        }
        return students;
    }

    public Student getStudentById(int studentId) {
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_ID)) {

            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractStudentFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting student by ID: " + studentId, e);
        }
        return null;
    }

    public List<Student> getStudentByRangeOfId(List<Integer> ListOfID) {
        List<Student> students = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_RANGE_OF_ID)) {
            Array IDs = conn.createArrayOf("integer", ListOfID.toArray());
            // System.out.println(IDs);
            /*
             * Student s1 = new Student();
             * students.add(s1);
             * 
             */
            pstmt.setArray(1, IDs);
            // System.out.println(pstmt);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    students.add(extractStudentFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting student by ID: " + ListOfID, e);
        }
        return students;
    }

    public boolean addStudent(Student student) {
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            setStudentParameters(pstmt, student);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        student.setStudentId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error adding student", e);
        }
        return false;
    }

    public boolean updateStudent(Student student) {
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(UPDATE)) {

            setStudentParameters(pstmt, student);
            pstmt.setInt(7, student.getStudentId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Error updating student: " + student.getStudentId(), e);
        }
    }

    public boolean deleteStudent(int studentId) {
        try (Connection conn = DBConnection.getConnection();
                CallableStatement cstmt = conn.prepareCall(DELETE_STUDENT_FUNCTION)) {

            // Register output parameter and set input
            cstmt.registerOutParameter(1, Types.BOOLEAN);
            cstmt.setInt(2, studentId);

            // Execute function
            cstmt.execute();
            return cstmt.getBoolean(1);

        } catch (SQLException e) {
            throw new DataAccessException("Error deleting student: " + studentId, e);
        }
    }

    private Student extractStudentFromResultSet(ResultSet rs) throws SQLException {
        Student student = new Student();

        // Basic student info
        student.setStudentId(rs.getInt("student_id"));
        student.setStudentName(rs.getString("student_name"));
        student.setNid(rs.getString("nid"));
        student.setNationalityName(rs.getString("nationality")); // Changed to match query alias
        student.setReligionName(rs.getString("religion"));
        student.setCurrentAddress(rs.getString("current_address"));
        student.setMedicalStatus(rs.getBoolean("medical_status"));

        // New fields

        student.setDateOfBirth(rs.getDate("date_of_birth"));
        student.setPlaceOfBirth(rs.getString("place_of_birth"));
        student.setGradeName(rs.getString("grade")); // Matches query alias
        student.setClassName(rs.getString("class")); // Matches query alias
        student.setCreatedDate(rs.getTimestamp("created_at"));
        student.setUpdatedDate(rs.getTimestamp("updated_at"));

        // Medical history
        student.setMedicalDescriptions(rs.getString("medical_descriptions"));

        // Phone numbers array
        Array phoneArray = rs.getArray("student_phones");
        if (phoneArray != null) {
            student.setStudentPhones(List.of((String[]) phoneArray.getArray()));
        }

        // Parent info (JSON array)
        String parentsJson = rs.getString("parents_info");
        if (parentsJson != null) {
            try {
                List<ParentDetails> parents = objectMapper.readValue(
                        parentsJson,
                        new TypeReference<List<ParentDetails>>() {
                        });
                student.setParentsInfo(parents);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error parsing parents JSON", e);
            }
        }

        // Student notes - FIXED: use correct column name
        String noteJson = rs.getString("student_notes"); // This must match SQL alias
        if (noteJson != null) {
            try {
                List<Notes> notes = objectMapper.readValue(
                        noteJson,
                        new TypeReference<List<Notes>>() {
                        });
                student.setStudentNotes(notes);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error parsing notes JSON", e);
            }
        }


        String qualificationJson = rs.getString("qualifications"); // This must match SQL alias
            if (qualificationJson != null) {
                try {
                    List<AdditionalQualification> qualifications = objectMapper.readValue(
                            qualificationJson,
                            new TypeReference<List<AdditionalQualification>>() {
                            });
                    student.setAdditionalQualifications(qualifications);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error parsing additional qualifications JSON", e);
                }
            }



        return student;
    }

    private void setStudentParameters(PreparedStatement pstmt, Student student) throws SQLException {
        pstmt.setString(1, student.getStudentName());
        pstmt.setString(2, student.getNid());
        pstmt.setInt(3, student.getNationalityId());
        pstmt.setString(4, student.getCurrentAddress());
        pstmt.setInt(5, student.getReligionId());
        pstmt.setBoolean(6, student.isMedicalStatus());
    }

    private void handleSQLException(String message, SQLException e) {
        logger.log(Level.SEVERE, message, e);
        throw new com.canalprep.exception.DataAccessException(message, e);
    }

    public int insertFullStudent(Student student) throws SQLException {
        // 1 output + 14 inputs = 15 parameters
        String sql = "{ ? = call insert_full_student(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }";

        try (Connection conn = DBConnection.getConnection();
                CallableStatement cstmt = conn.prepareCall(sql)) {



            // Register output parameter
            cstmt.registerOutParameter(1, Types.INTEGER);

            // Set input parameters
            cstmt.setString(2, notNull(student.getStudentName()));
            cstmt.setString(3, notNull(student.getNid()));
            cstmt.setString(4, notNull(student.getNationalityName()));
            cstmt.setString(5, notNull(student.getReligionName()));
            cstmt.setString(6, notNull(student.getCurrentAddress()));
            cstmt.setBoolean(7, student.isMedicalStatus());

            // Handle date
            if (student.getDateOfBirth() != null) {
                cstmt.setDate(8, new java.sql.Date(student.getDateOfBirth().getTime()));
            } else {
                cstmt.setNull(8, Types.DATE);
            }

            cstmt.setString(9, notNull(student.getPlaceOfBirth()));
            cstmt.setString(10, notNull(student.getGradeName()));
            cstmt.setString(11, notNull(student.getClassName()));

            // Medical descriptions
            if (student.getMedicalDescriptions() != null && !student.getMedicalDescriptions().isEmpty()) {
                String[] medicalArray = student.getMedicalDescriptions().split("; ");
                Array medicalSqlArray = conn.createArrayOf("TEXT", medicalArray);
                cstmt.setArray(12, medicalSqlArray);
            } else {
                cstmt.setNull(12, Types.ARRAY);
            }

            // Student phones
            if (student.getStudentPhones() != null && !student.getStudentPhones().isEmpty()) {
                Array phonesArray = conn.createArrayOf("TEXT",
                        student.getStudentPhones().toArray(new String[0]));
                cstmt.setArray(13, phonesArray);
            } else {
                cstmt.setNull(13, Types.ARRAY);
            }

            // Parents info
            JSONArray parentsJson = new JSONArray();
            if (student.getParentsInfo() != null) {
                for (ParentDetails parent : student.getParentsInfo()) {
                    JSONObject parentObj = new JSONObject();
                    parentObj.put("parent_name", notNull(parent.getParentName()));
                    parentObj.put("relationship", notNull(parent.getRelationship()));
                    parentObj.put("parent_nid", notNull(parent.getParentNid()));
                    parentObj.put("parent_nationality", notNull(parent.getParentNationality()));
                    parentObj.put("parent_job", notNull(parent.getParentJob()));
                    parentObj.put("parent_address", notNull(parent.getParentAddress()));
                    parentObj.put("parent_social_status", notNull(parent.getParentSocialStatus()));

                    if (parent.getParentPhones() != null) {
                        parentObj.put("parent_phones", new JSONArray(parent.getParentPhones()));
                    } else {
                        parentObj.put("parent_phones", new JSONArray());
                    }
                    parentsJson.put(parentObj);
                }
            }

            PGobject parentsPgObject = new PGobject();
            parentsPgObject.setType("jsonb");
            parentsPgObject.setValue(parentsJson.toString());
            cstmt.setObject(14, parentsPgObject);

            // NOTES HANDLING (NEW PARAMETER)
            JSONArray notesJson = new JSONArray();
            if (student.getStudentNotes() != null && !student.getStudentNotes().isEmpty()) {
                for (Notes note : student.getStudentNotes()) {
                    JSONObject noteObj = new JSONObject();
                    noteObj.put("note_text", notNull(note.getNoteText()));
                    noteObj.put("created_by", notNull(note.getCreatedBy()));
                    notesJson.put(noteObj);
                }
            }

            PGobject notesPgObject = new PGobject();
            notesPgObject.setType("jsonb");
            notesPgObject.setValue(notesJson.toString());
            cstmt.setObject(15, notesPgObject); // Set as 15th parameter

            // Execute and return
            cstmt.execute();
            int studentId = cstmt.getInt(1);

            return studentId;
        } catch (SQLException e) {

            logger.log(Level.SEVERE, "Error counting all students", e);
            throw e;
        } catch (Exception e) {

            logger.log(Level.SEVERE, "Error counting all students", e);
            throw new SQLException("Failed to insert student", e);
        }
    }

    // Helper method to prevent null values
    public boolean updateFullStudent(Student student) throws SQLException {
        String sql = UPDATE_STUDENT_DATA;

        try (Connection conn = DBConnection.getConnection();
                CallableStatement cstmt = conn.prepareCall(sql)) {



            // Register output parameter
            cstmt.registerOutParameter(1, Types.BOOLEAN);

            // Set input parameters
            cstmt.setInt(2, student.getStudentId());
            cstmt.setString(3, notNull(student.getStudentName()));
            cstmt.setString(4, notNull(student.getNid()));
            cstmt.setString(5, notNull(student.getNationalityName()));
            cstmt.setString(6, notNull(student.getReligionName()));
            cstmt.setString(7, notNull(student.getCurrentAddress()));
            cstmt.setBoolean(8, student.isMedicalStatus());
            cstmt.setDate(9, new java.sql.Date(student.getDateOfBirth().getTime()));
            cstmt.setString(10, notNull(student.getPlaceOfBirth()));
            cstmt.setString(11, notNull(student.getGradeName()));
            cstmt.setString(12, notNull(student.getClassName()));

            // Medical descriptions
            if (student.getMedicalDescriptions() != null) {
                String[] medicalArray = student.getMedicalDescriptions().split(";");
                Array medicalSqlArray = conn.createArrayOf("TEXT", medicalArray);
                cstmt.setArray(13, medicalSqlArray);
            } else {
                cstmt.setNull(13, Types.ARRAY);
            }

            // Student phones
            if (student.getStudentPhones() != null) {
                Array phonesArray = conn.createArrayOf("TEXT",
                        student.getStudentPhones().toArray(new String[0]));
                cstmt.setArray(14, phonesArray);
            } else {
                cstmt.setNull(14, Types.ARRAY);
            }

            // Parents info
            JSONArray parentsJson = new JSONArray();
            for (ParentDetails parent : student.getParentsInfo()) {
                JSONObject parentObj = new JSONObject();
                parentObj.put("parent_name", parent.getParentName());
                parentObj.put("relationship", parent.getRelationship());
                parentObj.put("parent_nid", parent.getParentNid());
                parentObj.put("parent_nationality", parent.getParentNationality());
                parentObj.put("parent_job", parent.getParentJob());
                parentObj.put("parent_address", parent.getParentAddress());
                parentObj.put("parent_social_status", parent.getParentSocialStatus());
                parentObj.put("parent_phones", new JSONArray(parent.getParentPhones()));
                parentsJson.put(parentObj);
            }

            PGobject parentsPgObject = new PGobject();
            parentsPgObject.setType("jsonb");
            parentsPgObject.setValue(parentsJson.toString());
            cstmt.setObject(15, parentsPgObject);

            // Execute and return
            cstmt.execute();
            return cstmt.getBoolean(1);
        } catch (Exception e) {

            throw new SQLException("Failed to update student", e);
        }
    }

    private String notNull(String value) {
        return value != null ? value : "";
    }


// End of StudentDAO.java


}


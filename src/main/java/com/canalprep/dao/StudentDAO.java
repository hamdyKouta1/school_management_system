package com.canalprep.dao;

import com.canalprep.model.Notes;
import com.canalprep.model.ParentDetails;
import com.canalprep.model.Student;
import com.canalprep.staticVAr.DBConst;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.*;
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
    private static final String INSERT_STUDENT_PHONE = DBConst.DB_INSERT_STUDENT_PHONE;
    private static final String INSERT_STUDENT_NOTE = DBConst.DB_INSERT_STUDENT_NOTE;
    private static final String INSERT_STUDENT_MEDICAL = DBConst.DB_INSERT_STUDENT_MEDICAL;
    private static final String DELETE_STUDENT_PHONE = DBConst.DB_DELETE_STUDENT_PHONE;
    private static final String DELETE_STUDENT_NOTE = DBConst.DB_DELETE_STUDENT_NOTE;
    private static final String DELETE_STUDENT_MEDICAL = DBConst.DB_DELETE_STUDENT_MEDICAL;
    private static final String SELECT_BY_STUDENT_NAME = DBConst.DB_SELECT_STUDENT_BY_NAME;
    private static final String UPDATE_STUDENT_MEDICAL_STATUS = DBConst.DB_UPDATE_MEDICAL_STATUS;




public int countAllStudents() {
    String sql = "SELECT COUNT(*) FROM students";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) return rs.getInt(1);
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return 0;
}

public int countStudentsByGrade(int gradeId) {
    String sql = "SELECT COUNT(*) FROM students WHERE grade_id = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, gradeId);
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return 0;
}

public int countStudentsByClass(int classId) {
    String sql = "SELECT COUNT(*) FROM students WHERE class_id = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, classId);
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return 0;
}




    public List<Integer> getStudentsByName(String name) {
    List<Integer> ids = new ArrayList<>();
    
    // Validate input
    if (name == null || name.trim().isEmpty()) {
        return ids;  // Return empty list for invalid input
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
        handleSQLException("Error getting students by name", e);
    }
    return ids;
}

   




   
    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(SELECT_ALL)) {

            while (rs.next()) {
                students.add(extractStudentFromResultSet(rs));
            }
        } catch (SQLException e) {
            handleSQLException("Error getting all students", e);
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
            handleSQLException("Error getting student by ID: " + studentId, e);
        }
        return null;
    }



        public List<Student> getStudentByRangeOfId(List<Integer> ListOfID) {
            List <Student> students = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_RANGE_OF_ID)) {
                   Array IDs = conn.createArrayOf("integer", ListOfID.toArray());
                  // System.out.println(IDs);
/*
Student s1 = new Student();
students.add(s1);
 * 
 */
            pstmt.setArray(1,IDs);
            //System.out.println(pstmt);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    students.add(extractStudentFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            handleSQLException("Error getting student by ID: " + ListOfID, e);
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
            handleSQLException("Error adding student", e);
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
            handleSQLException("Error updating student: " + student.getStudentId(), e);
        }
        return false;
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
            handleSQLException("Error deleting student: " + studentId, e);
            return false;
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
        // You could throw a custom application exception here
        // throw new DataAccessException(message, e);
    }

    public int insertFullStudent(Student student) throws SQLException {
        // 1 output + 14 inputs = 15 parameters
        String sql = "{ ? = call insert_full_student(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }";

        try (Connection conn = DBConnection.getConnection();
                CallableStatement cstmt = conn.prepareCall(sql)) {

            // Debugging: Print all values
            System.out.println("=== Parameters ===");
            System.out.println("studentName: " + student.getStudentName());
            System.out.println("nid: " + student.getNid());
            System.out.println("nationality: " + student.getNationalityName());
            System.out.println("religion: " + student.getReligionName());
            System.out.println("currentAddress: " + student.getCurrentAddress());
            System.out.println("medicalStatus: " + student.isMedicalStatus());
            System.out.println("dateOfBirth: " + student.getDateOfBirth());
            System.out.println("placeOfBirth: " + student.getPlaceOfBirth());
            System.out.println("grade: " + student.getGradeName());
            System.out.println("className: " + student.getClassName());
            System.out.println("medicalDescriptions: " + student.getMedicalDescriptions());
            System.out.println("studentPhones: " + student.getStudentPhones());
            System.out.println("parentsInfo: " + student.getParentsInfo());
            System.out.println("studentNotes: " + student.getStudentNotes());
            System.out.println("==================");

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
            System.out.println("Inserted student ID: " + studentId);
            return studentId;
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            System.err.println("General Error: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Failed to insert student", e);
        }
    }

    // Helper method to prevent null values
    public boolean updateFullStudent(Student student) throws SQLException {
        String sql = UPDATE_STUDENT_DATA;

        try (Connection conn = DBConnection.getConnection();
                CallableStatement cstmt = conn.prepareCall(sql)) {

            // Debugging output
            System.out.println("=== Update Parameters ===");
            System.out.println("studentId: " + student.getStudentId());
            System.out.println("studentName: " + student.getStudentName());
            // ... [other parameters] ...

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
            System.err.println("Update error: " + e.getMessage());
            throw new SQLException("Failed to update student", e);
        }
    }

    private String notNull(String value) {
        return value != null ? value : "";
    }

    public boolean addStudentPhone(Student student) throws SQLException {
        String sql = INSERT_STUDENT_PHONE;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, student.getStudentId());
            pstmt.setString(2, student.getStudentPhones().get(0));
            return pstmt.executeUpdate() > 0;
        }
    }

    // Add student note
    public boolean addStudentNote(Student student) throws SQLException {
        String sql = INSERT_STUDENT_NOTE;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, student.getStudentId());
            pstmt.setString(2, student.getStudentNotes().get(0).getNoteText());
            pstmt.setString(3, student.getStudentNotes().get(0).getCreatedBy());
            return pstmt.executeUpdate() > 0;
        }
    }

    // Add medical history
   public boolean addMedicalHistory(Student student) throws SQLException {
    String sql = INSERT_STUDENT_MEDICAL;
    String sql_update_status = UPDATE_STUDENT_MEDICAL_STATUS;
    boolean result = false;

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setInt(1, student.getStudentId());
        pstmt.setString(2, student.getMedicalDescriptions());
        result = pstmt.executeUpdate() > 0;

        // If insert successful, update medical status
        if (result) {
            try (Connection conn2 = DBConnection.getConnection();
                 PreparedStatement pstmt2 = conn2.prepareStatement(sql_update_status)) {

                pstmt2.setInt(2, student.getStudentId());
                pstmt2.setBoolean(1, true);
                pstmt2.executeUpdate();
            }
        }
    }

    return result;
}

    
    public boolean deleteStudentPhone(Student student) throws SQLException {
        String sql = DELETE_STUDENT_PHONE;
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, student.getStudentPhones().get(0));
            pstmt.setInt(2, student.getStudentId());
            return pstmt.executeUpdate() > 0;
        }
    }

    // Add student note
    public boolean deleteStudentNote(Student student) throws SQLException {
        String sql = DELETE_STUDENT_NOTE;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, student.getStudentNotes().get(0).getNoteId());
            return pstmt.executeUpdate() > 0;
        }
    }

    // Add medical history
   public boolean deleteMedicalHistory(Student student) throws SQLException {
    String sqlDelete = DELETE_STUDENT_MEDICAL;
    String sqlCheckRemaining = "SELECT 1 FROM medical_history WHERE student_id = ?";
    String sqlUpdateStatus = UPDATE_STUDENT_MEDICAL_STATUS;
    boolean result = false;

    try (Connection conn = DBConnection.getConnection()) {
        conn.setAutoCommit(false); // Begin transaction

        // 1. Delete specific medical history entry
        try (PreparedStatement pstmtDelete = conn.prepareStatement(sqlDelete)) {
            pstmtDelete.setString(1, student.getMedicalDescriptions());
            pstmtDelete.setInt(2, student.getStudentId());
            result = pstmtDelete.executeUpdate() > 0;
        }

        // 2. If deletion succeeded, check for remaining records
        if (result) {
            boolean hasRemaining = false;

            try (PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheckRemaining)) {
                pstmtCheck.setInt(1, student.getStudentId());
                try (ResultSet rs = pstmtCheck.executeQuery()) {
                    hasRemaining = rs.next(); // true if there’s at least one record
                }
            }

            // 3. If no remaining records, update medical status
            if (!hasRemaining) {
                try (PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdateStatus)) {
                    pstmtUpdate.setBoolean(1, false); // SET medical_status = ?
                    pstmtUpdate.setInt(2, student.getStudentId()); // WHERE student_id = ?
                    pstmtUpdate.executeUpdate();
                }
            }
        }

        conn.commit(); // Commit the transaction
    } catch (SQLException e) {
        e.printStackTrace(); // Log the exception
        throw e; // Rethrow after rollback
    }

    return result;
}


    /****************************************************************************************** */

    public Student buildStudentFromJson(JSONObject json) throws JSONException {
        Student student = new Student();
        student.setStudentName(json.getString("student_name"));
        student.setNid(json.getString("nid"));
        student.setNationalityName(json.getString("nationality"));
        student.setReligionName(json.getString("religion"));
        student.setCurrentAddress(json.getString("current_address"));
        student.setMedicalStatus(json.getBoolean("medical_status"));

        // Parse date
        String dobString = json.getString("date_of_birth");
        if (dobString != null && !dobString.isEmpty()) {
            student.setDateOfBirth(Date.valueOf(dobString));
        }

        student.setPlaceOfBirth(json.getString("place_of_birth"));
        student.setGradeName(json.getString("grade"));
        student.setClassName(json.getString("class"));

        // Medical descriptions
        if (json.has("medical_descriptions")) {
            student.setMedicalDescriptions(json.getString("medical_descriptions"));
        }

        // Student phones
        JSONArray phoneArray = json.getJSONArray("student_phones");
        List<String> phones = new ArrayList<>();
        for (int i = 0; i < phoneArray.length(); i++) {
            phones.add(phoneArray.getString(i));
        }
        student.setStudentPhones(phones);

        // Student notes
        if (json.has("student_notes")) {
            JSONArray notesArray = json.getJSONArray("student_notes");
            List<Notes> notesList = new ArrayList<>();
            for (int i = 0; i < notesArray.length(); i++) {
                JSONObject noteObj = notesArray.getJSONObject(i);
                Notes note = new Notes();
                note.setNoteText(noteObj.getString("note_text"));
                note.setCreatedBy(noteObj.getString("created_by"));
                notesList.add(note);
            }
            student.setStudentNotes(notesList);
        }

        // Parents info
        JSONArray parentsArray = json.getJSONArray("parents_info");
        List<ParentDetails> parents = new ArrayList<>();
        for (int i = 0; i < parentsArray.length(); i++) {
            JSONObject parentObj = parentsArray.getJSONObject(i);
            ParentDetails parent = new ParentDetails();
            parent.setParentName(parentObj.getString("parent_name"));
            parent.setRelationship(parentObj.getString("relationship"));
            parent.setParentNid(parentObj.getString("parent_nid"));
            parent.setParentNationality(parentObj.getString("parent_nationality"));
            parent.setParentJob(parentObj.getString("parent_job"));
            parent.setParentAddress(parentObj.getString("parent_address"));
            parent.setParentSocialStatus(parentObj.getString("parent_social_status"));

            JSONArray parentPhones = parentObj.getJSONArray("parent_phones");
            List<String> parentPhoneList = new ArrayList<>();
            for (int j = 0; j < parentPhones.length(); j++) {
                parentPhoneList.add(parentPhones.getString(j));
            }
            parent.setParentPhones(parentPhoneList);

            parents.add(parent);
        }
        student.setParentsInfo(parents);

        return student;
    }

}

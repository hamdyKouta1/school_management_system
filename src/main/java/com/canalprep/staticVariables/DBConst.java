package com.canalprep.staticVariables;


public abstract class DBConst {

    public static final String DB_DRIVER = "org.postgresql.Driver";

    public static final String DB_INSERT = "INSERT INTO students (student_name, nid, nationality_id, current_address, religion_id, medical_status) VALUES (?, ?, ?, ?, ?, ?)";
    public static final String DB_UPDATE = "UPDATE students SET student_name = ?, nid = ?, nationality_id = ?, current_address = ?, religion_id = ?, medical_status = ? WHERE student_id = ?";
    public static final String DB_UPDATE_STUDENT = "{ ? = call update_full_student(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }";
    public static final String DB_INSERT_STUDENT_PHONE = "INSERT INTO student_phone (student_id, phone_number) VALUES (?, ?)";
    public static final String DB_INSERT_STUDENT_NOTE = "INSERT INTO student_notes (student_id, note_text, created_by) VALUES (?, ?, ?)";
    public static final String DB_INSERT_STUDENT_MEDICAL = "INSERT INTO medical_history (student_id, description) VALUES (?, ?)";
    public static final String DB_DELETE_STUDENT_PHONE = "DELETE FROM student_Phone where phone_number = ? and student_id = ?";
    public static final String DB_DELETE_STUDENT_NOTE = "DELETE FROM student_notes where note_id = ?";
    public static final String DB_DELETE_STUDENT_MEDICAL = "DELETE FROM medical_history where description = ? and student_id = ?";
    public static final String DB_UPDATE_LAST_LOGIN = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = ?";
    public static final String DB_SELECT_USER_BY_USERNAME = "SELECT * FROM users WHERE username = ?";
    public static final String DB_INSERT_USER_SQL = "INSERT INTO users (username, email, password_hash, salt, role) VALUES (?, ?, ?, ?, ?)";
    public static final String DB_SELECT_STUDENT_BY_NAME = "SELECT students.student_id as s_id FROM students WHERE student_name LIKE ?";
    public static final String DB_UPDATE_MEDICAL_STATUS = "update students set medical_status = ? where student_id=?";
    public static final String DB_SELEC_STRING = "select * from get_student_details(null)";
    public static final String DB_SELECT_BY_ID = "select * from get_student_details(?)";
    public static final String DB_SELECT_BY_RANGE_OF_ID = "select * from get_student_details_array(?)";
    public static final String DB_DELETE_BY_ID = "{ ? = call delete_student_and_related_data(?) }";
    public static final String DB_INSERT_STUDENT_QUALIFICATION="INSERT INTO additional_qualifications (student_id, description) VALUES (?, ?)";
    public static final String DB_DELETE_STUDENT_QUALIFICATION="DELETE FROM additional_qualifications WHERE qualification_id = ?";
    public static final String DB_STUDENT_SELECT_ID_BY_NAME = "SELECT id FROM students WHERE student_name LIKE ?";

}

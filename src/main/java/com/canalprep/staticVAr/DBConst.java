package com.canalprep.staticVAr;


public abstract class DBConst {

    public static final String DB_DRIVER = "org.postgresql.Driver";
    public static final String DB_URL = "jdbc:postgresql://localhost:5432/Canal_Prep_School";
    public static final String DB_USER = "postgres";
    public static final String DB_PASSWORD = "123";
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
    public static final String DB_SELEC_STRING = "SELECT\n" + //
                "    s.student_id,\n" + //
                "    s.student_name,\n" + //
                "    s.nid,\n" + //
                "    n.national_name AS nationality,\n" + //
                "    r.religion_name AS religion,\n" + //
                "    s.current_address,\n" + //
                "    s.medical_status,\n" + //
                "    s.date_of_birth,\n" + //
                "    s.place_of_birth,\n" + //
                "    g.grade_name_ar AS grade,\n" + //
                "    c.class_name AS class,\n" + //
                "    s.created_at,\n" + //
                "    s.updated_at,\n" + //
                "    mh.medical_descriptions,\n" + //
                "    ph.student_phones,\n" + //
                "    p.parents_info,\n" + //
                "    COALESCE(ns.student_notes, '[]'::JSONB) AS student_notes\n" + //
                "FROM students s\n" + //
                "JOIN nationality n ON s.nationality_id = n.nationality_id\n" + //
                "JOIN religion r ON s.religion_id = r.religion_id\n" + //
                "LEFT JOIN grade_level g ON s.grade_id = g.grade_id\n" + //
                "LEFT JOIN class c ON s.class_id = c.class_id\n" + //
                "LEFT JOIN (\n" + //
                "    SELECT \n" + //
                "        student_id, \n" + //
                "        STRING_AGG(description, '; ') AS medical_descriptions\n" + //
                "    FROM medical_history\n" + //
                "    GROUP BY student_id\n" + //
                ") mh ON s.student_id = mh.student_id\n" + //
                "LEFT JOIN (\n" + //
                "    SELECT\n" + //
                "        student_id,\n" + //
                "        JSONB_AGG(JSONB_BUILD_OBJECT(\n" + //
                "            'parent_id', p.parent_id,\n" + //
                "            'parent_name', p.parent_name,\n" + //
                "            'relationship', rt.type_name,\n" + //
                "            'parent_nid', p.nid,\n" + //
                "            'parent_nationality', pn.national_name,\n" + //
                "            'parent_social_status_id', p.social_status_id,  \n" + //
                "            'parent_social_status', ss.social_status_description, \n" + //
                "            'parent_job', p.current_job,\n" + //
                "            'parent_address', p.current_address,\n" + //
                "            'parent_phones', pp.phones\n" + //
                "        )) AS parents_info\n" + //
                "    FROM student_parent sp\n" + //
                "    JOIN parents p ON sp.parent_id = p.parent_id\n" + //
                "    JOIN relationship_type rt ON sp.relationship_type = rt.type_id\n" + //
                "    JOIN nationality pn ON p.nationality_id = pn.nationality_id\n" + //
                "    JOIN social_status ss ON p.social_status_id = ss.social_status_id  -- Added join\n" + //
                "    LEFT JOIN (\n" + //
                "        SELECT parent_id, ARRAY_AGG(phone_number) AS phones\n" + //
                "        FROM parent_phone\n" + //
                "        GROUP BY parent_id\n" + //
                "    ) pp ON p.parent_id = pp.parent_id\n" + //
                "    GROUP BY sp.student_id\n" + //
                ") p ON s.student_id = p.student_id\n" + //
                "LEFT JOIN (\n" + //
                "    SELECT student_id, ARRAY_AGG(phone_number) AS student_phones\n" + //
                "    FROM student_phone\n" + //
                "    GROUP BY student_id\n" + //
                ") ph ON s.student_id = ph.student_id\n" + //
                "LEFT JOIN (\n" + //
                "    SELECT \n" + //
                "        student_id,\n" + //
                "        JSONB_AGG(JSONB_BUILD_OBJECT(\n" + //
                "            'note_id', note_id,\n" + //
                "            'note_date', note_date,\n" + //
                "            'note_text', note_text,\n" + //
                "            'created_by', created_by\n" + //
                "        )) AS student_notes\n" + //
                "    FROM student_notes\n" + //
                "    GROUP BY student_id\n" + //
                ") ns ON s.student_id = ns.student_id\n" + //
                "ORDER BY s.student_id";
    
   public static final String DB_SELECT_BY_ID = "SELECT\n" + //
        "    s.student_id,\n" + //
        "    s.student_name,\n" + //
        "    s.nid,\n" + //
        "    n.national_name AS nationality,\n" + //
        "    r.religion_name AS religion,\n" + //
        "    s.current_address,\n" + //
        "    s.medical_status,\n" + //
        "    s.date_of_birth,\n" + //
        "    s.place_of_birth,\n" + //
        "    g.grade_name_ar AS grade,\n" + //
        "    c.class_name AS class,\n" + //
        "    s.created_at,\n" + //
        "    s.updated_at,\n" + //
        "    mh.medical_descriptions,\n" + //
        "    ph.student_phones,\n" + //
        "    p.parents_info,\n" + //
        "    COALESCE(ns.student_notes, '[]'::JSONB) AS student_notes\n" + //
        "FROM students s\n" + //
        "JOIN nationality n ON s.nationality_id = n.nationality_id\n" + //
        "JOIN religion r ON s.religion_id = r.religion_id\n" + //
        "LEFT JOIN grade_level g ON s.grade_id = g.grade_id\n" + //
        "LEFT JOIN class c ON s.class_id = c.class_id\n" + //
        "LEFT JOIN (\n" + //
        "    SELECT \n" + //
        "        student_id, \n" + //
        "        STRING_AGG(description, '; ') AS medical_descriptions\n" + //
        "    FROM medical_history\n" + //
        "    GROUP BY student_id\n" + //
        ") mh ON s.student_id = mh.student_id\n" + //
        "LEFT JOIN (\n" + //
        "    SELECT\n" + //
        "        student_id,\n" + //
        "        JSONB_AGG(JSONB_BUILD_OBJECT(\n" + //
        "            'parent_id', p.parent_id,\n" + //
        "            'parent_name', p.parent_name,\n" + //
        "            'relationship', rt.type_name,\n" + //
        "            'parent_nid', p.nid,\n" + //
        "            'parent_nationality', pn.national_name,\n" + //
        "            'parent_social_status_id', p.social_status_id,  \n" + //
        "            'parent_social_status', ss.social_status_description, \n" + //
        "            'parent_job', p.current_job,\n" + //
        "            'parent_address', p.current_address,\n" + //
        "            'parent_phones', pp.phones\n" + //
        "        )) AS parents_info\n" + //
        "    FROM student_parent sp\n" + //
        "    JOIN parents p ON sp.parent_id = p.parent_id\n" + //
        "    JOIN relationship_type rt ON sp.relationship_type = rt.type_id\n" + //
        "    JOIN nationality pn ON p.nationality_id = pn.nationality_id\n" + //
        "    JOIN social_status ss ON p.social_status_id = ss.social_status_id\n" + //
        "    LEFT JOIN (\n" + //
        "        SELECT parent_id, ARRAY_AGG(phone_number) AS phones\n" + //
        "        FROM parent_phone\n" + //
        "        GROUP BY parent_id\n" + //
        "    ) pp ON p.parent_id = pp.parent_id\n" + //
        "    GROUP BY sp.student_id\n" + //
        ") p ON s.student_id = p.student_id\n" + //
        "LEFT JOIN (\n" + //
        "    SELECT student_id, ARRAY_AGG(phone_number) AS student_phones\n" + //
        "    FROM student_phone\n" + //
        "    GROUP BY student_id\n" + //
        ") ph ON s.student_id = ph.student_id\n" + //
        "LEFT JOIN (\n" + //
        "    SELECT \n" + //
        "        student_id,\n" + //
        "        JSONB_AGG(JSONB_BUILD_OBJECT(\n" + //
        "            'note_id', note_id,\n" + //
        "            'note_date', note_date,\n" + //
        "            'note_text', note_text,\n" + //
        "            'created_by', created_by\n" + //
        "        )) AS student_notes\n" + //
        "    FROM student_notes\n" + //
        "    GROUP BY student_id\n" + //
        ") ns ON s.student_id = ns.student_id\n" + //
        "WHERE s.student_id = ?";  // Changed to filter by student ID
   public static final String DB_SELECT_BY_RANGE_OF_ID = "SELECT\n" + //
        "    s.student_id,\n" + //
        "    s.student_name,\n" + //
        "    s.nid,\n" + //
        "    n.national_name AS nationality,\n" + //
        "    r.religion_name AS religion,\n" + //
        "    s.current_address,\n" + //
        "    s.medical_status,\n" + //
        "    s.date_of_birth,\n" + //
        "    s.place_of_birth,\n" + //
        "    g.grade_name_ar AS grade,\n" + //
        "    c.class_name AS class,\n" + //
        "    s.created_at,\n" + //
        "    s.updated_at,\n" + //
        "    mh.medical_descriptions,\n" + //
        "    ph.student_phones,\n" + //
        "    p.parents_info,\n" + //
        "    COALESCE(ns.student_notes, '[]'::JSONB) AS student_notes\n" + //
        "FROM students s\n" + //
        "JOIN nationality n ON s.nationality_id = n.nationality_id\n" + //
        "JOIN religion r ON s.religion_id = r.religion_id\n" + //
        "LEFT JOIN grade_level g ON s.grade_id = g.grade_id\n" + //
        "LEFT JOIN class c ON s.class_id = c.class_id\n" + //
        "LEFT JOIN (\n" + //
        "    SELECT \n" + //
        "        student_id, \n" + //
        "        STRING_AGG(description, '; ') AS medical_descriptions\n" + //
        "    FROM medical_history\n" + //
        "    GROUP BY student_id\n" + //
        ") mh ON s.student_id = mh.student_id\n" + //
        "LEFT JOIN (\n" + //
        "    SELECT\n" + //
        "        student_id,\n" + //
        "        JSONB_AGG(JSONB_BUILD_OBJECT(\n" + //
        "            'parent_id', p.parent_id,\n" + //
        "            'parent_name', p.parent_name,\n" + //
        "            'relationship', rt.type_name,\n" + //
        "            'parent_nid', p.nid,\n" + //
        "            'parent_nationality', pn.national_name,\n" + //
        "            'parent_social_status_id', p.social_status_id,  \n" + //
        "            'parent_social_status', ss.social_status_description, \n" + //
        "            'parent_job', p.current_job,\n" + //
        "            'parent_address', p.current_address,\n" + //
        "            'parent_phones', pp.phones\n" + //
        "        )) AS parents_info\n" + //
        "    FROM student_parent sp\n" + //
        "    JOIN parents p ON sp.parent_id = p.parent_id\n" + //
        "    JOIN relationship_type rt ON sp.relationship_type = rt.type_id\n" + //
        "    JOIN nationality pn ON p.nationality_id = pn.nationality_id\n" + //
        "    JOIN social_status ss ON p.social_status_id = ss.social_status_id\n" + //
        "    LEFT JOIN (\n" + //
        "        SELECT parent_id, ARRAY_AGG(phone_number) AS phones\n" + //
        "        FROM parent_phone\n" + //
        "        GROUP BY parent_id\n" + //
        "    ) pp ON p.parent_id = pp.parent_id\n" + //
        "    GROUP BY sp.student_id\n" + //
        ") p ON s.student_id = p.student_id\n" + //
        "LEFT JOIN (\n" + //
        "    SELECT student_id, ARRAY_AGG(phone_number) AS student_phones\n" + //
        "    FROM student_phone\n" + //
        "    GROUP BY student_id\n" + //
        ") ph ON s.student_id = ph.student_id\n" + //
        "LEFT JOIN (\n" + //
        "    SELECT \n" + //
        "        student_id,\n" + //
        "        JSONB_AGG(JSONB_BUILD_OBJECT(\n" + //
        "            'note_id', note_id,\n" + //
        "            'note_date', note_date,\n" + //
        "            'note_text', note_text,\n" + //
        "            'created_by', created_by\n" + //
        "        )) AS student_notes\n" + //
        "    FROM student_notes\n" + //
        "    GROUP BY student_id\n" + //
        ") ns ON s.student_id = ns.student_id\n" + //
        "WHERE s.student_id = ANY(?)";  // Changed to filter by student ID




    public static final String DB_DELETE_BY_ID = "{ ? = call delete_student_and_related_data(?) }";
}

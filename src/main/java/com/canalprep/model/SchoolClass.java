package com.canalprep.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public class SchoolClass {
    @JsonAlias("class_id")
    private int classId;
    
    @JsonAlias("class_name")
    private String className;
    
    @JsonAlias("grade_id")
    private int gradeId;
    
    // Default constructor
    public SchoolClass() {}
    
    // Constructor with parameters
    public SchoolClass(int classId, String className, int gradeId) {
        this.classId = classId;
        this.className = className;
        this.gradeId = gradeId;
    }
    
    // Getters and Setters
    public int getClassId() {
        return classId;
    }
    
    public void setClassId(int classId) {
        this.classId = classId;
    }
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public int getGradeId() {
        return gradeId;
    }
    
    public void setGradeId(int gradeId) {
        this.gradeId = gradeId;
    }
    
    @Override
    public String toString() {
        return "SchoolClass{" +
                "classId=" + classId +
                ", className='" + className + '\'' +
                ", gradeId=" + gradeId +
                '}';
    }
}
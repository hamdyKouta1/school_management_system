package com.canalprep.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public class GradeLevel {
    @JsonAlias("grade_id")
    private int gradeId;
    
    @JsonAlias("grade_name_ar")
    private String gradeNameAr;
    
    @JsonAlias("grade_name_en")
    private String gradeNameEn;
    
    @JsonAlias("grade_order")
    private int gradeOrder;
    
    // Default constructor
    public GradeLevel() {}
    
    // Constructor with parameters
    public GradeLevel(int gradeId, String gradeNameAr, String gradeNameEn, int gradeOrder) {
        this.gradeId = gradeId;
        this.gradeNameAr = gradeNameAr;
        this.gradeNameEn = gradeNameEn;
        this.gradeOrder = gradeOrder;
    }
    
    // Getters and Setters
    public int getGradeId() {
        return gradeId;
    }
    
    public void setGradeId(int gradeId) {
        this.gradeId = gradeId;
    }
    
    public String getGradeNameAr() {
        return gradeNameAr;
    }
    
    public void setGradeNameAr(String gradeNameAr) {
        this.gradeNameAr = gradeNameAr;
    }
    
    public String getGradeNameEn() {
        return gradeNameEn;
    }
    
    public void setGradeNameEn(String gradeNameEn) {
        this.gradeNameEn = gradeNameEn;
    }
    
    public int getGradeOrder() {
        return gradeOrder;
    }
    
    public void setGradeOrder(int gradeOrder) {
        this.gradeOrder = gradeOrder;
    }
    
    @Override
    public String toString() {
        return "GradeLevel{" +
                "gradeId=" + gradeId +
                ", gradeNameAr='" + gradeNameAr + '\'' +
                ", gradeNameEn='" + gradeNameEn + '\'' +
                ", gradeOrder=" + gradeOrder +
                '}';
    }
}
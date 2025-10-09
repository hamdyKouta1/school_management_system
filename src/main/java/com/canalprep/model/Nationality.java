package com.canalprep.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public class Nationality {
    @JsonAlias("nationality_id")
    private int nationalityId;
    
    @JsonAlias("national_name")
    private String nationalName;
    
    // Default constructor
    public Nationality() {}
    
    // Constructor with parameters
    public Nationality(int nationalityId, String nationalName) {
        this.nationalityId = nationalityId;
        this.nationalName = nationalName;
    }
    
    // Getters and Setters
    public int getNationalityId() {
        return nationalityId;
    }
    
    public void setNationalityId(int nationalityId) {
        this.nationalityId = nationalityId;
    }
    
    public String getNationalName() {
        return nationalName;
    }
    
    public void setNationalName(String nationalName) {
        this.nationalName = nationalName;
    }
    
    @Override
    public String toString() {
        return "Nationality{" +
                "nationalityId=" + nationalityId +
                ", nationalName='" + nationalName + '\'' +
                '}';
    }
}
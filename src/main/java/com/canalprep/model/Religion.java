package com.canalprep.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public class Religion {
    @JsonAlias("religion_id")
    private int religionId;
    
    @JsonAlias("religion_name")
    private String religionName;
    
    // Default constructor
    public Religion() {}
    
    // Constructor with parameters
    public Religion(int religionId, String religionName) {
        this.religionId = religionId;
        this.religionName = religionName;
    }
    
    // Getters and Setters
    public int getReligionId() {
        return religionId;
    }
    
    public void setReligionId(int religionId) {
        this.religionId = religionId;
    }
    
    public String getReligionName() {
        return religionName;
    }
    
    public void setReligionName(String religionName) {
        this.religionName = religionName;
    }
    
    @Override
    public String toString() {
        return "Religion{" +
                "religionId=" + religionId +
                ", religionName='" + religionName + '\'' +
                '}';
    }
}
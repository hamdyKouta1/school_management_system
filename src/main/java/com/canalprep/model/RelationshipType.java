package com.canalprep.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public class RelationshipType {
    @JsonAlias("type_id")
    private int typeId;
    
    @JsonAlias("type_name")
    private String typeName;
    
    // Default constructor
    public RelationshipType() {}
    
    // Constructor with parameters
    public RelationshipType(int typeId, String typeName) {
        this.typeId = typeId;
        this.typeName = typeName;
    }
    
    // Getters and Setters
    public int getTypeId() {
        return typeId;
    }
    
    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }
    
    public String getTypeName() {
        return typeName;
    }
    
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
    
    @Override
    public String toString() {
        return "RelationshipType{" +
                "typeId=" + typeId +
                ", typeName='" + typeName + '\'' +
                '}';
    }
}
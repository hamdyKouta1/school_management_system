package com.canalprep.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ParentDetails {
@JsonProperty("parent_id")
    private int parentId;
    
    @JsonProperty("parent_name")
    private String parentName;
    
    @JsonProperty("relationship")
    private String relationship;
    
    @JsonProperty("parent_nid")
    private String parentNid;
    
    @JsonProperty("parent_nationality")
    private String parentNationality;
    
    @JsonProperty("parent_job")
    private String parentJob;
    
    @JsonProperty("parent_address")
    private String parentAddress;
    
    @JsonProperty("parent_phones")
    private List<String> parentPhones;

    @JsonProperty("parent_social_status")
     private String parentSocialStatus; // Add this field
    
    @JsonProperty("parent_social_status_id")
     private int  parentSocialStatusId; // Add this field

    // Add getter/setter
    public int getParentSocialStatusId() { return parentSocialStatusId; }
    public void setParentSocialStatusId(int parentSocialStatusId) { 
        this.parentSocialStatusId = parentSocialStatusId; 
    }

    // Add getter/setter
    public String getParentSocialStatus() { return parentSocialStatus; }
    public void setParentSocialStatus(String parentSocialStatus) { 
        this.parentSocialStatus = parentSocialStatus; 
    }

    // Getters and Setters
    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getParentNid() {
        return parentNid;
    }

    public void setParentNid(String parentNid) {
        this.parentNid = parentNid;
    }

    public String getParentNationality() {
        return parentNationality;
    }

    public void setParentNationality(String parentNationality) {
        this.parentNationality = parentNationality;
    }

    public String getParentJob() {
        return parentJob;
    }

    public void setParentJob(String parentJob) {
        this.parentJob = parentJob;
    }

    public String getParentAddress() {
        return parentAddress;
    }

    public void setParentAddress(String parentAddress) {
        this.parentAddress = parentAddress;
    }

    public List<String> getParentPhones() {
        return parentPhones;
    }

    public void setParentPhones(List<String> parentPhones) {
        this.parentPhones = parentPhones;
    }
}
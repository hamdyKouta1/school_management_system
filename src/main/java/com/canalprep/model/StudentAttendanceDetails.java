package com.canalprep.model;


import java.util.List;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudentAttendanceDetails {
    @JsonProperty("student_id")
    private int studentId;
    
    @JsonProperty("student_name")
    private String studentName;
    
    @JsonProperty("current_address")
    private String currentAddress;
    
    @JsonProperty("medical_status")
    private String medicalStatus;
    
    @JsonProperty("grade")
    private String grade;
    
    @JsonProperty("class")
    private String className;
    
    @JsonProperty("student_phones")
    private List<String> studentPhones;
    
    @JsonProperty("parents_info")
    private List<ParentInfo> parentsInfo;
    

    
    // Nested class for parent information
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ParentInfo {
        @JsonProperty("parent_id")
        private int parentId;
        
        @JsonProperty("parent_name")
        private String parentName;
        
        @JsonProperty("relationship")
        private String relationship;
        
        @JsonProperty("parent_job")
        private String parentJob;
        
        @JsonProperty("parent_nid")
        private String parentNid;
        
        @JsonProperty("parent_phones")
        private List<String> parentPhones;
        
        @JsonProperty("parent_address")
        private String parentAddress;
        
        @JsonProperty("parent_nationality")
        private String parentNationality;
        
        @JsonProperty("parent_social_status")
        private String parentSocialStatus;
        
        @JsonProperty("social_status_id")
        private int socialStatusId;
        
        // Getters and Setters for ParentInfo
        public int getParentId() { return parentId; }
        public void setParentId(int parentId) { this.parentId = parentId; }
        
        public String getParentName() { return parentName; }
        public void setParentName(String parentName) { this.parentName = parentName; }
        
        public String getRelationship() { return relationship; }
        public void setRelationship(String relationship) { this.relationship = relationship; }
        
        public String getParentJob() { return parentJob; }
        public void setParentJob(String parentJob) { this.parentJob = parentJob; }
        
        public String getParentNid() { return parentNid; }
        public void setParentNid(String parentNid) { this.parentNid = parentNid; }
        
        public List<String> getParentPhones() { return parentPhones; }
        public void setParentPhones(List<String> parentPhones) { this.parentPhones = parentPhones; }
        
        public String getParentAddress() { return parentAddress; }
        public void setParentAddress(String parentAddress) { this.parentAddress = parentAddress; }
        
        public String getParentNationality() { return parentNationality; }
        public void setParentNationality(String parentNationality) { this.parentNationality = parentNationality; }
        
        public String getParentSocialStatus() { return parentSocialStatus; }
        public void setParentSocialStatus(String parentSocialStatus) { this.parentSocialStatus = parentSocialStatus; }
        
        public int getSocialStatusId() { return socialStatusId; }
        public void setSocialStatusId(int socialStatusId) { this.socialStatusId = socialStatusId; }
    }
    

    
    // Main class getters and setters
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    
    public String getCurrentAddress() { return currentAddress; }
    public void setCurrentAddress(String currentAddress) { this.currentAddress = currentAddress; }
    
    public String getMedicalStatus() { return medicalStatus; }
    public void setMedicalStatus(String medicalStatus) { this.medicalStatus = medicalStatus; }
    
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    
    public List<String> getStudentPhones() { return studentPhones; }
    public void setStudentPhones(List<String> studentPhones) { this.studentPhones = studentPhones; }
    
    public List<ParentInfo> getParentsInfo() { return parentsInfo; }
    public void setParentsInfo(List<ParentInfo> parentsInfo) { this.parentsInfo = parentsInfo; }
    

}
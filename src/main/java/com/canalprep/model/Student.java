package com.canalprep.model;

import java.sql.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public class Student {
  //  @JsonAlias("student_id")
    private int studentId;
  //  @JsonAlias
    private String studentName;
    private String nid;
//    @JsonAlias("nationality_id")
    private int nationalityId;
 //   @JsonAlias("nationality")
    private String nationalityName;
 //   @JsonAlias("current_address")
    private String currentAddress;
   // @JsonAlias("religion_id")
    private int religionId;
  //  @JsonAlias
    private String religionName;   
   // @JsonAlias("medical_status")
    private boolean medicalStatus;
//@JsonAlias("medical_descriptions")
    private String medicalDescriptions;
   // @JsonAlias("student_phones")
    private List<String> studentPhones;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date dateOfBirth;
    private String placeOfBirth;
    private int gradeId;
    private String gradeName;
    private int classId;
    private String className;
  
private List<Notes> studentNotes;
    
    public List<Notes> getStudentNotes() { return studentNotes; }
    public void setStudentNotes(List<Notes> studentNotes) {
        this.studentNotes = studentNotes;
    }


    public int getGradeId() {
        return gradeId;
    }

    public void setGradeId(int gradeId) {
        this.gradeId = gradeId;
    }

    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

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

    private List<Notes> studentNoteList;
    private java.sql.Timestamp updatedDate;
    private java.sql.Timestamp createdDate;

    public List<Notes> getStudentNoteList() {
        return studentNoteList;
    }

    public void setStudentNoteList(List<Notes> studentNoteList) {
        this.studentNoteList = studentNoteList;
    }

    public java.sql.Timestamp getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(java.sql.Timestamp updatedDate) {
        this.updatedDate = updatedDate;
    }

    public java.sql.Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(java.sql.Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    
    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public void setPlaceOfBirth(String placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }

   // @JsonAlias("parents_info")
    private List<ParentDetails> parentsInfo;

    // Getters and Setters
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getNid() { return nid; }
    public void setNid(String nid) { this.nid = nid; }
    public int getNationalityId() { return nationalityId; }
    public void setNationalityId(int nationalityId) { this.nationalityId = nationalityId; }
    public String getCurrentAddress() { return currentAddress; }
    public void setCurrentAddress(String currentAddress) { this.currentAddress = currentAddress; }
    public int getReligionId() { return religionId; }
    public void setReligionId(int religionId) { this.religionId = religionId; }
    public boolean isMedicalStatus() { return medicalStatus; }
    public void setMedicalStatus(boolean medicalStatus) { this.medicalStatus = medicalStatus; }
    public String getMedicalDescriptions() { return medicalDescriptions; }
    public void setMedicalDescriptions(String medicalDescriptions) { this.medicalDescriptions = medicalDescriptions; }
    public List<String> getStudentPhones() { return studentPhones; }
    public void setStudentPhones(List<String> studentPhones) { this.studentPhones = studentPhones; }
    public List<ParentDetails> getParentsInfo() { return parentsInfo; }
    public void setParentsInfo(List<ParentDetails> parentsInfo) { this.parentsInfo = parentsInfo; }
     public String getNationalityName() { return nationalityName; }
    public void setNationalityName(String nationalityName) { this.nationalityName = nationalityName; }
    public String getReligionName() { return religionName; }
    public void setReligionName(String religionName) { this.religionName = religionName; }
}

/*
 * 
 * student(0).getName()
 * 
 */
package com.canalprep.model;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonAlias;

public class Attendance {
    @JsonAlias("attendance_id")
    private int attendanceId;
    @JsonAlias("student_id")
    private int studentId;
    @JsonAlias("attendance_date")
    private Date attendanceDate;
    @JsonAlias("status_id")
    private int statusId;
    @JsonAlias("grade_id")
    private int grade;
    @JsonAlias("class_id")
    private String stdClass;
    @JsonAlias("today_date")
    private Date todayDate;

    private String gradeName;

    public String getGradeName() {
        return gradeName;
    }
    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public String getStdClass() {
        return stdClass;
    }

    public void setStdClass(String stdClass) {
        this.stdClass = stdClass;
    }

    public Date getTodayDate() {
        return todayDate;
    }

    public void setTodayDate(Date todayDate) {
        this.todayDate = todayDate;
    }



    // Getters and Setters
    public int getAttendanceId() { return attendanceId; }
    public void setAttendanceId(int attendanceId) { this.attendanceId = attendanceId; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public Date getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(Date attendanceDate) { this.attendanceDate = attendanceDate; }
    public int getStatusId() { return statusId; }
    public void setStatusId(int statusId) { this.statusId = statusId; }
      public void setAttendanceDate(String dateString) {
        this.attendanceDate = Date.valueOf(dateString);
    }
    
}
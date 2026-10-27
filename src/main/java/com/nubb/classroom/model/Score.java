package com.nubb.classroom.model;

import jakarta.persistence.*;

@Entity
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

        private Long accountId;
private Long studentId;
    
    @Column(name = "score_month")
    private String month;
    private String className;

    private String subjectName;
    private double monthlyScore;
    private double attendanceScore;
    private double assignmentScore;
    
    private double total;
    @Column(name = "academic_year", length = 50)
    private String academicYear;


    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public double getMonthlyScore() { return monthlyScore; }
    public void setMonthlyScore(double monthlyScore) { this.monthlyScore = monthlyScore; }

    public double getAttendanceScore() { return attendanceScore; }
    public void setAttendanceScore(double attendanceScore) { this.attendanceScore = attendanceScore; }

    public double getAssignmentScore() { return assignmentScore; }
    public void setAssignmentScore(double assignmentScore) { this.assignmentScore = assignmentScore; }

    

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }


    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
}
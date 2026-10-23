package com.nubb.classroom.model;

import jakarta.persistence.*;

@Entity
public class ScoreRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long accountId;
    private String className;
    private String subjectName;

    private double maxMonthlyScore = 0.0;
    private double maxAttendanceScore = 0.0;
    private double maxAssignmentScore = 0.0;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public double getMaxMonthlyScore() { return maxMonthlyScore; }
    public void setMaxMonthlyScore(double maxMonthlyScore) { this.maxMonthlyScore = maxMonthlyScore; }
    public double getMaxAttendanceScore() { return maxAttendanceScore; }
    public void setMaxAttendanceScore(double maxAttendanceScore) { this.maxAttendanceScore = maxAttendanceScore; }
    public double getMaxAssignmentScore() { return maxAssignmentScore; }
    public void setMaxAssignmentScore(double maxAssignmentScore) { this.maxAssignmentScore = maxAssignmentScore; }
}
package com.nubb.classroom.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_participation")
public class StudentParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private String className;

    @Column(nullable = false)
    private String academicYear;

    private LocalDate date;

    private LocalDateTime createdAt;

    // e.g. "EXCELLENT", "CORRECT", "ATTEMPTED", "INCORRECT"
    private String result;

    private double points;

    @Column(length = 500)
    private String note;

    public StudentParticipation() {
        this.createdAt = LocalDateTime.now();
        this.date = LocalDate.now();
    }

    public StudentParticipation(Long accountId, Student student, String className, String academicYear, String result, double points, String note) {
        this.accountId = accountId;
        this.student = student;
        this.className = className;
        this.academicYear = academicYear;
        this.result = result;
        this.points = points;
        this.note = note;
        this.createdAt = LocalDateTime.now();
        this.date = LocalDate.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public double getPoints() { return points; }
    public void setPoints(double points) { this.points = points; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getResultKhmer() {
        if ("EXCELLENT".equalsIgnoreCase(result)) return "ឆ្លើយបានល្អណាស់";
        if ("CORRECT".equalsIgnoreCase(result)) return "ឆ្លើយត្រូវ";
        if ("ATTEMPTED".equalsIgnoreCase(result)) return "បានចូលរួមឆ្លើយ";
        if ("INCORRECT".equalsIgnoreCase(result)) return "ឆ្លើយមិនទាន់ត្រូវ";
        return result != null && !result.isEmpty() ? result : "បានឆ្លើយ";
    }
}

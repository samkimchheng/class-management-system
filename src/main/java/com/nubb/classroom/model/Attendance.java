package com.nubb.classroom.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private LocalDate date;

    // true = present, false = absent
    @Column(nullable = false)
    private boolean present;

    // "PERMISSION" (ច្បាប់) or "UNEXCUSED" (គ្មានច្បាប់)
    @Column(name = "absence_type", length = 50)
    private String absenceType;

    public Attendance() {}

    public Attendance(Student student, LocalDate date, boolean present) {
        this.student = student;
        this.date = date;
        this.present = present;
        this.absenceType = present ? null : "UNEXCUSED";
    }

    public Attendance(Student student, LocalDate date, boolean present, String absenceType) {
        this.student = student;
        this.date = date;
        this.present = present;
        this.absenceType = absenceType;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public boolean isPresent() { return present; }
    public void setPresent(boolean present) { this.present = present; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public String getAbsenceType() { return absenceType; }
    public void setAbsenceType(String absenceType) { this.absenceType = absenceType; }

    public boolean isPermission() {
        return "PERMISSION".equalsIgnoreCase(absenceType) || "ច្បាប់".equals(absenceType);
    }

    public boolean isUnexcused() {
        return "UNEXCUSED".equalsIgnoreCase(absenceType) || "គ្មានច្បាប់".equals(absenceType) || (!present && !isPermission());
    }
}

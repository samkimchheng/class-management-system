package com.nubb.classroom.model;

import jakarta.persistence.*;

@Entity
public class Classroom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String className;
    private Long accountId;
    @Column(name = "academic_year", length = 50)
    private String academicYear;

    @Column(name = "track", length = 50)
    private String track; // "វិទ្យាសាស្ត្រ", "វិទ្យាសាស្ត្រសង្គម", "ទូទៅ"

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getTrack() {
        if (track != null && !track.trim().isEmpty()) {
            return track;
        }
        if (className != null) {
            String upper = className.trim().toUpperCase();
            if (className.contains("សង្គម") || upper.contains("SOC")) return "វិទ្យាសាស្ត្រសង្គម";
            if (className.contains("វិទ្យា") || upper.contains("SCI")) return "វិទ្យាសាស្ត្រ";
            if (upper.contains("11") || upper.contains("១១") ||
                upper.contains("12") || upper.contains("១២")) {
                return "វិទ្យាសាស្ត្រ";
            }
        }
        return "ទូទៅ";
    }

    public void setTrack(String track) {
        this.track = track;
    }
}

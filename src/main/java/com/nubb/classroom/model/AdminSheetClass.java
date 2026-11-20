package com.nubb.classroom.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_sheet_classes")
public class AdminSheetClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String className;

    @Column(length = 200)
    private String schoolName = "វិទ្យាល័យហ៊ុនសែន១០មករា";

    @Column(length = 60)
    private String academicYear = "២០២៥-២០២៦";

    private LocalDateTime createdAt = LocalDateTime.now();

    public AdminSheetClass() {}

    public AdminSheetClass(String className, String schoolName, String academicYear) {
        this.className = className;
        this.schoolName = (schoolName != null && !schoolName.trim().isEmpty()) ? schoolName.trim() : "វិទ្យាល័យហ៊ុនសែន១០មករា";
        this.academicYear = (academicYear != null && !academicYear.trim().isEmpty()) ? academicYear.trim() : "២០២៥-២០២៦";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

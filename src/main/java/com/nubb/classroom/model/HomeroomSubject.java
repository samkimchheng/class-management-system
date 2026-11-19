package com.nubb.classroom.model;

import jakarta.persistence.*;

@Entity
@Table(name = "homeroom_subjects")
public class HomeroomSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false, length = 100)
    private String className;

    @Column(length = 50)
    private String academicYear;

    @Column(nullable = false, length = 150)
    private String subjectName;

    @Column(length = 150)
    private String teacherName;

    @Column(nullable = false)
    private Double maxScore = 100.0;

    @Column(nullable = false)
    private Double coefficient = 1.0;

    @Column(nullable = false)
    private Integer displayOrder = 0;

    public HomeroomSubject() {}

    public HomeroomSubject(Long accountId, String className, String academicYear,
                           String subjectName, String teacherName, Double maxScore,
                           Double coefficient, Integer displayOrder) {
        this.accountId = accountId;
        this.className = className;
        this.academicYear = academicYear;
        this.subjectName = subjectName;
        this.teacherName = teacherName;
        this.maxScore = maxScore != null ? maxScore : 100.0;
        this.coefficient = coefficient != null ? coefficient : 1.0;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public Double getMaxScore() { return maxScore != null ? maxScore : 100.0; }
    public void setMaxScore(Double maxScore) { this.maxScore = maxScore; }

    public Double getCoefficient() { return coefficient != null ? coefficient : 1.0; }
    public void setCoefficient(Double coefficient) { this.coefficient = coefficient; }

    public Integer getDisplayOrder() { return displayOrder != null ? displayOrder : 0; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
}

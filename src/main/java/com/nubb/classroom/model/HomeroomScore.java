package com.nubb.classroom.model;

import jakarta.persistence.*;

@Entity
@Table(name = "homeroom_scores", indexes = {
    @Index(name = "idx_hr_score_lookup", columnList = "accountId, className, academicYear, period")
})
public class HomeroomScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false, length = 100)
    private String className;

    @Column(length = 50)
    private String academicYear;

    /**
     * Period codes:
     * "SEC" (តេស្តដើមឆ្នាំ),
     * "M1".."M12" (ខែវិច្ឆិកា..តុលា / ១២ខែ),
     * "EXAM_S1" (ប្រឡងឆមាសទី១),
     * "EXAM_S2" (ប្រឡងឆមាសទី២)
     */
    @Column(nullable = false, length = 30)
    private String period;

    @Column(nullable = false)
    private Long subjectId;

    private Double score;

    public HomeroomScore() {}

    public HomeroomScore(Long accountId, Long studentId, String className,
                         String academicYear, String period, Long subjectId, Double score) {
        this.accountId = accountId;
        this.studentId = studentId;
        this.className = className;
        this.academicYear = academicYear;
        this.period = period;
        this.subjectId = subjectId;
        this.score = score;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
}

package com.nubb.classroom.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_vocabulary_progress", indexes = {
    @Index(name = "idx_stu_vocab_stud_word", columnList = "studentId, wordId", unique = true),
    @Index(name = "idx_stu_vocab_student", columnList = "studentId")
})
public class StudentVocabularyProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Long wordId;

    private String unitName;

    private Boolean mastered = false; // true = ★ សិស្សចងចាំច្បាស់

    private Integer practiceCount = 0; // ចំនួនដងបានអនុវត្ត

    private Integer correctCount = 0; // ចំនួនឆ្លើយត្រូវ

    private LocalDateTime lastPracticed = LocalDateTime.now();

    public StudentVocabularyProgress() {}

    public StudentVocabularyProgress(Long studentId, Long wordId, String unitName, Boolean mastered) {
        this.studentId = studentId;
        this.wordId = wordId;
        this.unitName = unitName;
        this.mastered = mastered != null ? mastered : false;
        this.practiceCount = 1;
        this.correctCount = Boolean.TRUE.equals(mastered) ? 1 : 0;
        this.lastPracticed = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getWordId() {
        return wordId;
    }

    public void setWordId(Long wordId) {
        this.wordId = wordId;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public Boolean getMastered() {
        return mastered != null ? mastered : false;
    }

    public void setMastered(Boolean mastered) {
        this.mastered = mastered;
    }

    public Integer getPracticeCount() {
        return practiceCount != null ? practiceCount : 0;
    }

    public void setPracticeCount(Integer practiceCount) {
        this.practiceCount = practiceCount;
    }

    public Integer getCorrectCount() {
        return correctCount != null ? correctCount : 0;
    }

    public void setCorrectCount(Integer correctCount) {
        this.correctCount = correctCount;
    }

    public LocalDateTime getLastPracticed() {
        return lastPracticed;
    }

    public void setLastPracticed(LocalDateTime lastPracticed) {
        this.lastPracticed = lastPracticed;
    }
}

package com.nubb.classroom.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_phonics_progress", indexes = {
    @Index(name = "idx_stu_phonics_stud_item", columnList = "studentId, phonicsItemId", unique = true),
    @Index(name = "idx_stu_phonics_student", columnList = "studentId")
})
public class StudentPhonicsProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Long phonicsItemId;

    private Integer phonicsLevel = 1;

    private String unitName;

    // 4 International Skills Tracking
    private Boolean listeningCompleted = false; // 🎧 ស្តាប់
    private Boolean readingCompleted = false;   // 📖 អាន/ប្រកប
    private Boolean speakingCompleted = false;  // 🎙️ និយាយ
    private Boolean writingCompleted = false;   // ✍️ សរសេរ/ប្រកបតួអក្សរ

    private Boolean mastered = false;           // ⭐ ចេះស្ទាត់ជំនាញ

    private Integer practiceCount = 0;

    private Integer correctCount = 0;

    private LocalDateTime lastPracticed = LocalDateTime.now();

    public StudentPhonicsProgress() {}

    public StudentPhonicsProgress(Long studentId, Long phonicsItemId, Integer phonicsLevel, String unitName) {
        this.studentId = studentId;
        this.phonicsItemId = phonicsItemId;
        this.phonicsLevel = phonicsLevel != null ? phonicsLevel : 1;
        this.unitName = unitName;
        this.listeningCompleted = false;
        this.readingCompleted = false;
        this.speakingCompleted = false;
        this.writingCompleted = false;
        this.mastered = false;
        this.practiceCount = 0;
        this.correctCount = 0;
        this.lastPracticed = LocalDateTime.now();
    }

    public int getCompletedSkillsCount() {
        int c = 0;
        if (Boolean.TRUE.equals(listeningCompleted)) c++;
        if (Boolean.TRUE.equals(readingCompleted)) c++;
        if (Boolean.TRUE.equals(speakingCompleted)) c++;
        if (Boolean.TRUE.equals(writingCompleted)) c++;
        return c;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getPhonicsItemId() { return phonicsItemId; }
    public void setPhonicsItemId(Long phonicsItemId) { this.phonicsItemId = phonicsItemId; }

    public Integer getPhonicsLevel() { return phonicsLevel != null ? phonicsLevel : 1; }
    public void setPhonicsLevel(Integer phonicsLevel) { this.phonicsLevel = phonicsLevel; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public Boolean getListeningCompleted() { return listeningCompleted != null ? listeningCompleted : false; }
    public void setListeningCompleted(Boolean listeningCompleted) { this.listeningCompleted = listeningCompleted; }

    public Boolean getReadingCompleted() { return readingCompleted != null ? readingCompleted : false; }
    public void setReadingCompleted(Boolean readingCompleted) { this.readingCompleted = readingCompleted; }

    public Boolean getSpeakingCompleted() { return speakingCompleted != null ? speakingCompleted : false; }
    public void setSpeakingCompleted(Boolean speakingCompleted) { this.speakingCompleted = speakingCompleted; }

    public Boolean getWritingCompleted() { return writingCompleted != null ? writingCompleted : false; }
    public void setWritingCompleted(Boolean writingCompleted) { this.writingCompleted = writingCompleted; }

    public Boolean getMastered() { return mastered != null ? mastered : false; }
    public void setMastered(Boolean mastered) { this.mastered = mastered; }

    public Integer getPracticeCount() { return practiceCount != null ? practiceCount : 0; }
    public void setPracticeCount(Integer practiceCount) { this.practiceCount = practiceCount; }

    public Integer getCorrectCount() { return correctCount != null ? correctCount : 0; }
    public void setCorrectCount(Integer correctCount) { this.correctCount = correctCount; }

    public LocalDateTime getLastPracticed() { return lastPracticed; }
    public void setLastPracticed(LocalDateTime lastPracticed) { this.lastPracticed = lastPracticed; }
}

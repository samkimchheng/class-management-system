package com.nubb.classroom.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_teaching_log")
public class DailyTeachingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long accountId;
    private String academicYear; // e.g. "២០២៦-២០២៧"
    private LocalDate logDate;    // e.g. 2026-11-11
    private String dayOfWeek;    // e.g. "ពុធ", "ព្រហស្បតិ៍"
    private Integer slotIndex;   // 0 to 7 (index in daily shift)
    private Integer periodNumber;// 1 to 8 (e.g. ម៉ោងទី១, ម៉ោងទី២...)
    private String timeRange;    // e.g. "07:00 - 08:00 ព្រឹក"
    private String className;    // e.g. "10E", "11F"
    private String subjectName;  // e.g. "ភាសាអង់គ្លេស"
    private String lessonTitle;  // e.g. "Unit 3: Technology in Education"

    @Column(columnDefinition = "TEXT")
    private String contentSummary; // ខ្លឹមសារមេរៀនដែលត្រូវបង្រៀន

    @Column(columnDefinition = "TEXT")
    private String objectives;     // វត្ថុបំណង ឬ សកម្មភាពបង្រៀន

    @Column(columnDefinition = "TEXT")
    private String homework;       // កិច្ចការផ្ទះ ឬ សម្ភារឧបទេស

    @Column(columnDefinition = "TEXT")
    private String notes;          // កំណត់ចំណាំ ឬ ស្ថានភាពជាក់ស្តែង

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public LocalDate getLogDate() { return logDate; }
    public void setLogDate(LocalDate logDate) { this.logDate = logDate; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public Integer getSlotIndex() { return slotIndex; }
    public void setSlotIndex(Integer slotIndex) { this.slotIndex = slotIndex; }

    public Integer getPeriodNumber() { return periodNumber; }
    public void setPeriodNumber(Integer periodNumber) { this.periodNumber = periodNumber; }

    public String getTimeRange() { return timeRange; }
    public void setTimeRange(String timeRange) { this.timeRange = timeRange; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getLessonTitle() { return lessonTitle; }
    public void setLessonTitle(String lessonTitle) { this.lessonTitle = lessonTitle; }

    public String getContentSummary() { return contentSummary; }
    public void setContentSummary(String contentSummary) { this.contentSummary = contentSummary; }

    public String getObjectives() { return objectives; }
    public void setObjectives(String objectives) { this.objectives = objectives; }

    public String getHomework() { return homework; }
    public void setHomework(String homework) { this.homework = homework; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

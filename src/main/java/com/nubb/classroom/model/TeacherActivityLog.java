package com.nubb.classroom.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "teacher_activity_logs", indexes = {
        @Index(name = "idx_tal_account_date", columnList = "accountId, activityDate"),
        @Index(name = "idx_tal_date", columnList = "activityDate")
})
public class TeacherActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false)
    private LocalDate activityDate;

    @Column(nullable = false, length = 64)
    private String featureKey;

    @Column(nullable = false, length = 160)
    private String featureLabel;

    @Column(nullable = false)
    private Integer hitCount = 1;

    @Column(nullable = false)
    private LocalDateTime lastUsedAt;

    @Column(length = 255)
    private String lastPath;

    public TeacherActivityLog() {}

    public TeacherActivityLog(Long accountId, LocalDate activityDate, String featureKey, String featureLabel, LocalDateTime lastUsedAt, String lastPath) {
        this.accountId = accountId;
        this.activityDate = activityDate;
        this.featureKey = featureKey;
        this.featureLabel = featureLabel;
        this.hitCount = 1;
        this.lastUsedAt = lastUsedAt;
        this.lastPath = lastPath;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public LocalDate getActivityDate() { return activityDate; }
    public void setActivityDate(LocalDate activityDate) { this.activityDate = activityDate; }

    public String getFeatureKey() { return featureKey; }
    public void setFeatureKey(String featureKey) { this.featureKey = featureKey; }

    public String getFeatureLabel() { return featureLabel; }
    public void setFeatureLabel(String featureLabel) { this.featureLabel = featureLabel; }

    public Integer getHitCount() { return hitCount; }
    public void setHitCount(Integer hitCount) { this.hitCount = hitCount; }

    public LocalDateTime getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(LocalDateTime lastUsedAt) { this.lastUsedAt = lastUsedAt; }

    public String getLastPath() { return lastPath; }
    public void setLastPath(String lastPath) { this.lastPath = lastPath; }
}

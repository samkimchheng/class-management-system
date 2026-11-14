package com.nubb.classroom.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "external_learners")
public class ExternalLearner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "gender", length = 20)
    private String gender = "ប្រុស";

    @Column(name = "learner_code", nullable = false, unique = true, length = 80)
    private String learnerCode;

    @Column(name = "phone", length = 100)
    private String phone;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "allow_phonics")
    private Boolean allowPhonics = true;

    @Column(name = "allow_vocabulary")
    private Boolean allowVocabulary = true;

    @Column(name = "active")
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    public ExternalLearner() {}

    public Long getVirtualStudentId() {
        return this.id != null ? -this.id : -999999L;
    }

    public Student toVirtualStudent() {
        Student s = new Student();
        s.setId(getVirtualStudentId());
        s.setName(this.fullName);
        s.setGender(this.gender != null && !this.gender.isBlank() ? this.gender : "ប្រុស");
        s.setClassName("អ្នករៀនក្រៅថ្នាក់");
        s.setStudentCode(this.learnerCode);
        s.setStatus("កំពុងសិក្សា");
        return s;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getLearnerCode() { return learnerCode; }
    public void setLearnerCode(String learnerCode) { this.learnerCode = learnerCode; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Boolean getAllowPhonics() { return allowPhonics; }
    public void setAllowPhonics(Boolean allowPhonics) { this.allowPhonics = allowPhonics; }

    public Boolean getAllowVocabulary() { return allowVocabulary; }
    public void setAllowVocabulary(Boolean allowVocabulary) { this.allowVocabulary = allowVocabulary; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}

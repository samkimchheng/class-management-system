package com.nubb.classroom.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String role; // "ADMIN" or "TEACHER"
    private String fullName;
    private Boolean active = true;

    // Personal & Professional Information (Teacher Profile)
    private String gender;            // ភេទ (ប្រុស / ស្រី)
    private String schoolName;        // ឈ្មោះគ្រឹះស្ថានសិក្សា / សាលារៀន
    private String subject;           // មុខវិជ្ជាឯកទេស / បង្រៀន
    private String province;          // រាជធានី-ខេត្ត
    private String phone;             // លេខទូរស័ព្ទ
    private String telegram;          // គណនី Telegram
    private String email;             // អ៊ីមែល
    private String position;          // តួនាទី / ភារកិច្ច (ឧ. គ្រូទទួលបន្ទុកថ្នាក់)
    private String teachingClasses;   // កម្រិតថ្នាក់បង្រៀន
    @jakarta.persistence.Column(length = 1000)
    private String bio;               // បទពិសោធន៍ / ចំណាំ
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String signature;         // ហត្ថលេខាគ្រូ (Base64 data URI or image URL)
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String principalSignature; // ហត្ថលេខានាយក (Base64 data URI or image URL)
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String schoolStamp;        // ត្រាសាលា (Base64 data URI or image URL)
    private String principalName;      // គោត្តនាម និងនាម នាយកសាលា (Principal Full Name)

    public Account() {}

    public Account(String username, String password, String role, String fullName) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.active = true;
    }

    public Account(String username, String password, String role, String fullName, Boolean active) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.active = active != null ? active : true;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Boolean getActive() { return active != null ? active : true; }
    public void setActive(Boolean active) { this.active = active; }
    public boolean isActive() { return active != null && active; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getTelegram() { return telegram; }
    public void setTelegram(String telegram) { this.telegram = telegram; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getTeachingClasses() { return teachingClasses; }
    public void setTeachingClasses(String teachingClasses) { this.teachingClasses = teachingClasses; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }

    public String getPrincipalSignature() { return principalSignature; }
    public void setPrincipalSignature(String principalSignature) { this.principalSignature = principalSignature; }

    public String getSchoolStamp() { return schoolStamp; }
    public void setSchoolStamp(String schoolStamp) { this.schoolStamp = schoolStamp; }

    public String getPrincipalName() { return principalName; }
    public void setPrincipalName(String principalName) { this.principalName = principalName; }
}

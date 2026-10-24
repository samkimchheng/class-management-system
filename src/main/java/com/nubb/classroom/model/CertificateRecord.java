package com.nubb.classroom.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "certificate_records")
public class CertificateRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    @Column(length = 100)
    private String className;

    @Column(unique = true, nullable = false)
    private String serialNumber;

    @Column(length = 500)
    private String schoolName;

    @Column(length = 255)
    private String subjectName;

    @Column(length = 255)
    private String province;

    @Column(length = 255)
    private String teacherName;

    @Column(length = 255)
    private String roleSignerLeft;

    @Column(length = 255)
    private String roleSignerRight;

    private String issueDateKhmer;
    private String issueDateEn;

    private LocalDateTime updatedAt;

    public CertificateRecord() {
        this.updatedAt = LocalDateTime.now();
    }

    public CertificateRecord(Long studentId, String className, String serialNumber, String schoolName, String subjectName,
                             String province, String teacherName, String roleSignerLeft, String roleSignerRight,
                             String issueDateKhmer, String issueDateEn) {
        this.studentId = studentId;
        this.className = className;
        this.serialNumber = serialNumber;
        this.schoolName = schoolName;
        this.subjectName = subjectName;
        this.province = province;
        this.teacherName = teacherName;
        this.roleSignerLeft = roleSignerLeft;
        this.roleSignerRight = roleSignerRight;
        this.issueDateKhmer = issueDateKhmer;
        this.issueDateEn = issueDateEn;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getRoleSignerLeft() { return roleSignerLeft; }
    public void setRoleSignerLeft(String roleSignerLeft) { this.roleSignerLeft = roleSignerLeft; }

    public String getRoleSignerRight() { return roleSignerRight; }
    public void setRoleSignerRight(String roleSignerRight) { this.roleSignerRight = roleSignerRight; }

    public String getIssueDateKhmer() { return issueDateKhmer; }
    public void setIssueDateKhmer(String issueDateKhmer) { this.issueDateKhmer = issueDateKhmer; }

    public String getIssueDateEn() { return issueDateEn; }
    public void setIssueDateEn(String issueDateEn) { this.issueDateEn = issueDateEn; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

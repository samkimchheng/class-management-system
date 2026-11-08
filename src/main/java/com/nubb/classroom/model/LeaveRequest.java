package com.nubb.classroom.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "leave_requests")
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long accountId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "class_name", length = 100)
    private String className;

    @Column(name = "academic_year", length = 50)
    private String academicYear;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "leave_type", length = 100)
    private String leaveType;

    @Column(name = "leave_session", length = 100)
    private String leaveSession;

    @Column(name = "reason", length = 1000)
    private String reason;

    @Column(name = "parent_phone", length = 100)
    private String parentPhone;

    // "PENDING" (រង់ចាំការអនុម័ត), "APPROVED" (បានអនុម័ត), "REJECTED" (បានបដិសេធ)
    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "teacher_note", length = 500)
    private String teacherNote;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    public LeaveRequest() {
        this.createdAt = LocalDateTime.now();
        this.status = "PENDING";
        this.leaveSession = "ពេញមួយថ្ងៃ";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }

    public String getLeaveSession() { return leaveSession; }
    public void setLeaveSession(String leaveSession) { this.leaveSession = leaveSession; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getParentPhone() { return parentPhone; }
    public void setParentPhone(String parentPhone) { this.parentPhone = parentPhone; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTeacherNote() { return teacherNote; }
    public void setTeacherNote(String teacherNote) { this.teacherNote = teacherNote; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public long getTotalDays() {
        if (startDate == null || endDate == null) return 1L;
        long diff = ChronoUnit.DAYS.between(startDate, endDate) + 1L;
        return Math.max(1L, diff);
    }

    public boolean isPending() {
        return status == null || "PENDING".equalsIgnoreCase(status);
    }

    public boolean isApproved() {
        return "APPROVED".equalsIgnoreCase(status);
    }

    public boolean isRejected() {
        return "REJECTED".equalsIgnoreCase(status);
    }

    public String getStatusKhmer() {
        if (isApproved()) return "បានអនុម័ត (មានច្បាប់)";
        if (isRejected()) return "មិនអនុញ្ញាត";
        return "រង់ចាំការអនុម័ត";
    }
}

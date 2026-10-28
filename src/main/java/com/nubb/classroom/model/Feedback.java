package com.nubb.classroom.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "feedbacks")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long teacherAccountId;
    private String teacherName;
    private String teacherUsername;
    private String schoolName;
    private String province;
    private String phone;

    private String category;  // e.g. "ចំណុចខ្វះខាតប្រព័ន្ធ", "សំណូមពរបន្ថែមមុខងារ", "រាយការណ៍បញ្ហា/កំហុស", "ការប្រើប្រាស់", "ផ្សេងៗ"
    private String priority;  // "ធម្មតា", "មធ្យម", "បន្ទាន់"
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String status = "PENDING"; // PENDING, IN_PROGRESS, RESOLVED, CLOSED

    @Column(columnDefinition = "TEXT")
    private String adminReply;
    private String adminReplyBy;
    private LocalDateTime repliedAt;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Feedback() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTeacherAccountId() { return teacherAccountId; }
    public void setTeacherAccountId(Long teacherAccountId) { this.teacherAccountId = teacherAccountId; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getTeacherUsername() { return teacherUsername; }
    public void setTeacherUsername(String teacherUsername) { this.teacherUsername = teacherUsername; }

    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPriority() { return priority != null ? priority : "ធម្មតា"; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getStatus() { return status != null ? status : "PENDING"; }
    public void setStatus(String status) { this.status = status; }

    public String getAdminReply() { return adminReply; }
    public void setAdminReply(String adminReply) { this.adminReply = adminReply; }

    public String getAdminReplyBy() { return adminReplyBy; }
    public void setAdminReplyBy(String adminReplyBy) { this.adminReplyBy = adminReplyBy; }

    public LocalDateTime getRepliedAt() { return repliedAt; }
    public void setRepliedAt(LocalDateTime repliedAt) { this.repliedAt = repliedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Helper display methods
    public String getFormattedCreatedAt() {
        if (createdAt == null) return "";
        try {
            return createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (Exception e) {
            return createdAt.toString();
        }
    }

    public String getFormattedRepliedAt() {
        if (repliedAt == null) return "";
        try {
            return repliedAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (Exception e) {
            return repliedAt.toString();
        }
    }

    public String getStatusKhmer() {
        if (status == null) return "រង់ចាំពិនិត្យ";
        switch (status) {
            case "IN_PROGRESS": return "កំពុងកែលម្អ";
            case "RESOLVED": return "បានកែលម្អរួចរាល់";
            case "CLOSED": return "បានបិទបញ្ចប់";
            case "PENDING":
            default:
                return "រង់ចាំពិនិត្យ";
        }
    }

    public String getStatusColor() {
        if (status == null) return "#f59e0b"; // yellow
        switch (status) {
            case "IN_PROGRESS": return "#0284c7"; // sky blue
            case "RESOLVED": return "#10b981"; // green
            case "CLOSED": return "#64748b"; // slate
            case "PENDING":
            default:
                return "#f59e0b"; // yellow
        }
    }

    public String getPriorityColor() {
        if (priority == null) return "#64748b";
        switch (priority) {
            case "បន្ទាន់": return "#ef4444"; // red
            case "មធ្យម": return "#f59e0b"; // orange
            default: return "#3b82f6"; // blue
        }
    }
}

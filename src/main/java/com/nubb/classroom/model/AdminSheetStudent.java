package com.nubb.classroom.model;

import jakarta.persistence.*;

@Entity
@Table(name = "admin_sheet_students", indexes = {
    @Index(name = "idx_admin_sheet_stu_cls", columnList = "sheetClassId")
})
public class AdminSheetStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sheetClassId;

    @Column(nullable = false)
    private Integer orderNo = 0;

    @Column(nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, length = 20)
    private String gender = "ប"; // "ប" or "ស"

    public AdminSheetStudent() {}

    public AdminSheetStudent(Long sheetClassId, Integer orderNo, String fullName, String gender) {
        this.sheetClassId = sheetClassId;
        this.orderNo = orderNo != null ? orderNo : 0;
        this.fullName = fullName;
        this.gender = normalizeGender(gender);
    }

    public static String normalizeGender(String g) {
        if (g == null) return "ប";
        String s = g.trim().toLowerCase();
        if (s.equals("ស") || s.equals("ស្រី") || s.equals("f") || s.equals("female")) return "ស";
        return "ប";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSheetClassId() { return sheetClassId; }
    public void setSheetClassId(Long sheetClassId) { this.sheetClassId = sheetClassId; }

    public Integer getOrderNo() { return orderNo != null ? orderNo : 0; }
    public void setOrderNo(Integer orderNo) { this.orderNo = orderNo; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = normalizeGender(gender); }
}

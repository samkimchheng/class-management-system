package com.nubb.classroom.model;

import jakarta.persistence.*;

@Entity
@Table(name = "homeroom_student_extra")
public class HomeroomStudentExtra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false)
    private Long studentId;

    @Column(length = 100)
    private String className;

    @Column(length = 80)
    private String dob; // ថ្ងៃខែឆ្នាំកំណើត

    @Column(length = 250)
    private String pob; // ទីកន្លែងកំណើត

    @Column(length = 100)
    private String phone; // លេខទូរស័ព្ទសិស្ស/អាណាព្យាបាល

    @Column(columnDefinition = "TEXT")
    private String photoUrl; // រូបថត 4x6 (Base64 data URI or /uploads/images/...)

    @Column(length = 100)
    private String youthRole; // តួនាទីក្នុងក្រុមប្រឹក្សាយុវជន/ប្រធានថ្នាក់

    @Column(length = 50)
    private String cleaningGroup; // ក្រុមវេនសម្អាត (ចន្ទ..សៅរ៍)

    private Integer seatRow;
    private Integer seatCol;

    @Column(length = 250)
    private String previousSchool; // សាលាដើម / ថ្នាក់មុន (សលាកបត្រឯកត្តជន)

    @Column(length = 250)
    private String scholarshipOrNote; // អាហារូបករណ៍ / ស្ថានភាពគ្រួសារ / ចំណាំ

    @Column(length = 150)
    private String teacherComment; // មតិគ្រូបន្ទុកថ្នាក់ (ព្រឹត្តិបត្រពិន្ទុ / សៀវភៅតាមដាន)

    public HomeroomStudentExtra() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }

    public String getPob() { return pob; }
    public void setPob(String pob) { this.pob = pob; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getYouthRole() { return youthRole; }
    public void setYouthRole(String youthRole) { this.youthRole = youthRole; }

    public String getCleaningGroup() { return cleaningGroup; }
    public void setCleaningGroup(String cleaningGroup) { this.cleaningGroup = cleaningGroup; }

    public Integer getSeatRow() { return seatRow; }
    public void setSeatRow(Integer seatRow) { this.seatRow = seatRow; }

    public Integer getSeatCol() { return seatCol; }
    public void setSeatCol(Integer seatCol) { this.seatCol = seatCol; }

    public String getPreviousSchool() { return previousSchool; }
    public void setPreviousSchool(String previousSchool) { this.previousSchool = previousSchool; }

    public String getScholarshipOrNote() { return scholarshipOrNote; }
    public void setScholarshipOrNote(String scholarshipOrNote) { this.scholarshipOrNote = scholarshipOrNote; }

    public String getTeacherComment() { return teacherComment; }
    public void setTeacherComment(String teacherComment) { this.teacherComment = teacherComment; }
}

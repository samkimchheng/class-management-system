package com.nubb.classroom.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "homeroom_class_admin")
public class HomeroomClassAdmin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false, length = 100)
    private String className;

    @Column(length = 50)
    private String academicYear;

    @Column(columnDefinition = "TEXT")
    private String cleaningScheduleJson; // វេនសម្អាតថ្នាក់ (Mon-Sat)

    @Column(columnDefinition = "TEXT")
    private String seatingLayoutJson; // ប្លង់តុអង្គុយ (rows, cols, assignments)

    @Column(columnDefinition = "TEXT")
    private String youthCouncilJson; // ក្រុមប្រឹក្សាយុវជន / គណៈកម្មការថ្នាក់

    @Column(columnDefinition = "TEXT")
    private String inventoryJson; // បញ្ជីសារពើភ័ណ្ឌសម្ភារៈក្នុងថ្នាក់

    @Column(columnDefinition = "TEXT")
    private String textbookLoansJson; // បញ្ជីខ្ចី-សងសៀវភៅពុម្ព

    @Column(columnDefinition = "TEXT")
    private String geipPlanJson; // ផែនការសិស្ស GEIP/ជីព ៣ឆ្នាំ/១ឆ្នាំ/ត្រីមាស ១-៤ & កិច្ចព្រមព្រៀង

    @Column(columnDefinition = "TEXT")
    private String meetingLogsJson; // កំណត់ហេតុប្រជុំជីវភាពថ្នាក់

    @Column(columnDefinition = "TEXT")
    private String configJson; // ការកំណត់បន្ថែម (ឧ. ឈ្មោះសាលា, នាយក, ខេត្ត, ប្រធានថ្នាក់, ថ្ងៃខែចន្ទគតិ)

    @Column(columnDefinition = "TEXT")
    private String monthlyAttendanceJson; // បញ្ជីស្រង់អវត្តមានតាមខែ (ថ្ងៃឈប់សម្រាក + ច/អ តាមខែនីមួយៗ)

    private LocalDateTime updatedAt = LocalDateTime.now();

    public HomeroomClassAdmin() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getCleaningScheduleJson() { return cleaningScheduleJson; }
    public void setCleaningScheduleJson(String cleaningScheduleJson) { this.cleaningScheduleJson = cleaningScheduleJson; }

    public String getSeatingLayoutJson() { return seatingLayoutJson; }
    public void setSeatingLayoutJson(String seatingLayoutJson) { this.seatingLayoutJson = seatingLayoutJson; }

    public String getYouthCouncilJson() { return youthCouncilJson; }
    public void setYouthCouncilJson(String youthCouncilJson) { this.youthCouncilJson = youthCouncilJson; }

    public String getInventoryJson() { return inventoryJson; }
    public void setInventoryJson(String inventoryJson) { this.inventoryJson = inventoryJson; }

    public String getTextbookLoansJson() { return textbookLoansJson; }
    public void setTextbookLoansJson(String textbookLoansJson) { this.textbookLoansJson = textbookLoansJson; }

    public String getGeipPlanJson() { return geipPlanJson; }
    public void setGeipPlanJson(String geipPlanJson) { this.geipPlanJson = geipPlanJson; }

    public String getMeetingLogsJson() { return meetingLogsJson; }
    public void setMeetingLogsJson(String meetingLogsJson) { this.meetingLogsJson = meetingLogsJson; }

    public String getConfigJson() { return configJson; }
    public void setConfigJson(String configJson) { this.configJson = configJson; }

    public String getMonthlyAttendanceJson() { return monthlyAttendanceJson; }
    public void setMonthlyAttendanceJson(String monthlyAttendanceJson) { this.monthlyAttendanceJson = monthlyAttendanceJson; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

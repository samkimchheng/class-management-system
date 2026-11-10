package com.nubb.classroom.controller;

import com.nubb.classroom.model.*;
import com.nubb.classroom.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/student-portal")
public class StudentPortalApiController {

    @Autowired
    private ExamRepository examRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private ScoreRepository scoreRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;
    @Autowired
    private AttendanceRepository attendanceRepository;

    @GetMapping("/subjects")
    public ResponseEntity<?> getSubjects(@RequestParam String month, HttpSession session) {
        Student student = (Student) session.getAttribute("loggedInStudent");
        if (student == null) return ResponseEntity.status(401).build();

        Long stuAccId = student.getAccountId();
        if (stuAccId == null) return ResponseEntity.ok(Collections.emptyList());

        List<Exam> exams = examRepository.findAll().stream()
                .filter(e -> month != null && month.equals(e.getMonth()))
                .filter(e -> Objects.equals(stuAccId, e.getAccountId()))
                .filter(e -> ExamApiController.classMatches(e.getClassName(), student.getClassName()))
                .toList();

        List<String> subjects = exams.stream()
                .map(Exam::getSubjectName)
                .distinct()
                .toList();
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/exam")
    public ResponseEntity<?> getExam(@RequestParam String month, @RequestParam String subjectName, HttpSession session) {
        Student student = (Student) session.getAttribute("loggedInStudent");
        if (student == null) return ResponseEntity.status(401).build();

        Long stuAccId = student.getAccountId();
        if (stuAccId == null) return ResponseEntity.notFound().build();

        // Rule 1: Check if student is absent today
        LocalDate today = LocalDate.now();
        boolean isAbsentToday = attendanceRepository.findByStudentId(student.getId()).stream()
                .anyMatch(a -> today.equals(a.getDate()) && !a.isPresent());
        if (isAbsentToday) {
            return ResponseEntity.status(403).body(Map.of(
                    "blocked", true,
                    "reason", "ABSENT",
                    "message", "🚫 ប្អូនត្រូវបានកត់ត្រាថា «អវត្តមាន» នៅថ្ងៃនេះ (" + today + ")! មិនមានសិទ្ធិចូលប្រឡងតាមប្រព័ន្ធពីផ្ទះបានឡើយ។ សូមទាក់ទងលោកគ្រូ-អ្នកគ្រូបន្ទុកថ្នាក់!"
            ));
        }

        // Rule 2: Find exam for this month, subject, and student's class
        Optional<Exam> examOpt = examRepository.findAll().stream()
                .filter(e -> month != null && month.equals(e.getMonth()) && subjectName != null && subjectName.equals(e.getSubjectName()))
                .filter(e -> Objects.equals(stuAccId, e.getAccountId()))
                .filter(e -> ExamApiController.classMatches(e.getClassName(), student.getClassName()))
                .sorted(Comparator.comparing(Exam::getId).reversed())
                .findFirst();

        if (examOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "blocked", true,
                    "reason", "NOT_FOUND",
                    "message", "រកមិនឃើញវិញ្ញាសាសម្រាប់ថ្នាក់របស់ប្អូនទេ!"
            ));
        }
        Exam exam = examOpt.get();

        // Rule 2: Check if teacher has opened exam hours for this class
        if (!exam.isOpenForClass(student.getClassName())) {
            return ResponseEntity.status(403).body(Map.of(
                    "blocked", true,
                    "reason", "CLASS_CLOSED",
                    "message", "🔒 ម៉ោងប្រឡងសម្រាប់ថ្នាក់ «" + student.getClassName() + "» មិនទាន់បើក ឬត្រូវបានបិទរួចរាល់ហើយ! សូមរង់ចាំលោកគ្រូ-អ្នកគ្រូបើកដំណើរការ។"
            ));
        }

        // Rule 3: Check if already submitted and retake not allowed
        Optional<Score> existingOpt = scoreRepository.findByStudentIdAndMonthAndClassNameAndSubjectNameAndAccountId(
                student.getId(), month, student.getClassName(), subjectName, stuAccId);
        if (existingOpt.isPresent()) {
            Score sc = existingOpt.get();
            boolean submitted = Boolean.TRUE.equals(sc.getExamSubmitted()) || (sc.getMonthlyScore() > 0 && !Boolean.TRUE.equals(sc.getRetakeAllowed()));
            if (submitted && !Boolean.TRUE.equals(sc.getRetakeAllowed())) {
                return ResponseEntity.status(403).body(Map.of(
                        "blocked", true,
                        "reason", "ALREADY_SUBMITTED",
                        "score", sc.getMonthlyScore(),
                        "message", "⚠️ ប្អូនបានប្រឡងវិញ្ញាសានេះរួចរាល់ហើយ (ពិន្ទុទទួលបាន៖ " + sc.getMonthlyScore() + " ពិន្ទុ)! មិនមានសិទ្ធិចូលប្រឡងម្តងទៀតឡើយ លើកលែងតែមានការអនុញ្ញាតពីលោកគ្រូ-អ្នកគ្រូ។"
                ));
            }
        }

        List<Question> questions = questionRepository.findByExamIdAndAccountId(exam.getId(), exam.getAccountId());
        if (questions.isEmpty()) {
            questions = questionRepository.findAll().stream().filter(q -> exam.getId().equals(q.getExamId())).toList();
        }
        return ResponseEntity.ok(Map.of("exam", exam, "questions", questions));
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitExam(@RequestBody SubmitPayload payload, HttpSession session) {
        Student student = (Student) session.getAttribute("loggedInStudent");
        if (student == null) return ResponseEntity.status(401).build();

        Long stuAccId = student.getAccountId();

        // Rule 1: Verify not absent today
        LocalDate today = LocalDate.now();
        boolean isAbsentToday = attendanceRepository.findByStudentId(student.getId()).stream()
                .anyMatch(a -> today.equals(a.getDate()) && !a.isPresent());
        if (isAbsentToday) {
            return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "blocked", true,
                    "reason", "ABSENT",
                    "message", "🚫 ប្អូនត្រូវបានកត់ត្រាថា «អវត្តមាន» នៅថ្ងៃនេះ មិនអាចបញ្ជូនចម្លើយប្រឡងបានឡើយ!"
            ));
        }

        // Rule 2: Verify exam class access is currently open
        Optional<Exam> examOpt = examRepository.findAll().stream()
                .filter(e -> payload.getMonth() != null && payload.getMonth().equals(e.getMonth()) && payload.getSubjectName() != null && payload.getSubjectName().equals(e.getSubjectName()))
                .filter(e -> Objects.equals(stuAccId, e.getAccountId()))
                .filter(e -> ExamApiController.classMatches(e.getClassName(), student.getClassName()))
                .sorted(Comparator.comparing(Exam::getId).reversed())
                .findFirst();

        if (examOpt.isPresent() && !examOpt.get().isOpenForClass(student.getClassName())) {
            return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "blocked", true,
                    "reason", "CLASS_CLOSED",
                    "message", "🔒 ម៉ោងប្រឡងត្រូវបានបិទរួចរាល់ហើយ មិនអាចបញ្ជូនចម្លើយបានទៀតទេ!"
            ));
        }

        // Rule 3: Verify not already submitted without retake permission
        Optional<Score> existingOpt = scoreRepository.findByStudentIdAndMonthAndClassNameAndSubjectNameAndAccountId(
                student.getId(), payload.getMonth(), student.getClassName(), payload.getSubjectName(), stuAccId);
        if (existingOpt.isPresent()) {
            Score sc = existingOpt.get();
            boolean submitted = Boolean.TRUE.equals(sc.getExamSubmitted()) || (sc.getMonthlyScore() > 0 && !Boolean.TRUE.equals(sc.getRetakeAllowed()));
            if (submitted && !Boolean.TRUE.equals(sc.getRetakeAllowed())) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "blocked", true,
                        "reason", "ALREADY_SUBMITTED",
                        "message", "⚠️ ប្អូនបានប្រឡងរួចរាល់ហើយ មិនអាចបញ្ជូនម្តងទៀតបានទេ លើកលែងតែមានការអនុញ្ញាតពីគ្រូ!"
                ));
            }
        }

        Score score = existingOpt.orElse(new Score());
        score.setStudentId(student.getId());
        score.setMonth(payload.getMonth());
        score.setClassName(student.getClassName());
        score.setSubjectName(payload.getSubjectName());
        score.setAccountId(stuAccId);
        if (student.getAcademicYear() != null && !student.getAcademicYear().trim().isEmpty()) {
            score.setAcademicYear(student.getAcademicYear().trim());
        }

        score.setMonthlyScore(payload.getTotalScore());
        if (payload.getStudentWriting() != null && !payload.getStudentWriting().trim().isEmpty()) {
            score.setStudentWriting(payload.getStudentWriting().trim());
        }
        score.setTotal(score.getMonthlyScore() + score.getAttendanceScore() + score.getAssignmentScore());
        score.setExamSubmitted(true);
        score.setRetakeAllowed(false); // Retake used up upon submission

        scoreRepository.save(score);

        return ResponseEntity.ok(Map.of("success", true, "totalScore", payload.getTotalScore()));
    }

    @GetMapping("/check-status")
    public ResponseEntity<?> checkStatus(HttpSession session) {
        Student student = (Student) session.getAttribute("loggedInStudent");
        if (student == null) return ResponseEntity.status(401).build();

        LocalDate today = LocalDate.now();
        boolean isAbsentToday = attendanceRepository.findByStudentId(student.getId()).stream()
                .anyMatch(a -> today.equals(a.getDate()) && !a.isPresent());

        return ResponseEntity.ok(Map.of(
                "isAbsentToday", isAbsentToday,
                "studentName", student.getName(),
                "className", student.getClassName(),
                "date", today.toString()
        ));
    }

    @GetMapping("/attendance-summary")
    public ResponseEntity<?> getAttendanceSummary(HttpSession session) {
        Student student = (Student) session.getAttribute("loggedInStudent");
        if (student == null) return ResponseEntity.status(401).build();
        if (student.getId() != null) {
            student = studentRepository.findById(student.getId()).orElse(student);
            session.setAttribute("loggedInStudent", student);
        }
        return ResponseEntity.ok(calculateAttendanceSummary(student, attendanceRepository));
    }

    public static Map<String, Object> calculateAttendanceSummary(Student student, AttendanceRepository attendanceRepository) {
        Map<String, Object> res = new LinkedHashMap<>();
        if (student == null || student.getId() == null) {
            res.put("totalAbsences", 0L);
            res.put("permissionCount", 0L);
            res.put("unexcusedCount", 0L);
            res.put("permissionPercent", 0.0);
            res.put("unexcusedPercent", 0.0);
            res.put("attendanceRate", 100.0);
            res.put("absenceRate", 0.0);
            res.put("recordedDays", 0L);
            res.put("presentDays", 0L);
            return res;
        }

        List<Attendance> list = attendanceRepository.findByStudentId(student.getId());
        if (list == null) list = Collections.emptyList();

        List<Attendance> absences = list.stream().filter(a -> !a.isPresent()).toList();

        Set<LocalDate> allDates = new HashSet<>();
        Set<LocalDate> permDates = new HashSet<>();
        Set<LocalDate> unexcusedDates = new HashSet<>();

        for (Attendance a : absences) {
            LocalDate d = a.getDate();
            if (d != null) {
                allDates.add(d);
                if (a.isPermission()) {
                    permDates.add(d);
                } else {
                    unexcusedDates.add(d);
                }
            }
        }

        long totalAbsences = allDates.size();
        long permCount = permDates.size();
        long unexcusedCount = Math.max(0, totalAbsences - permCount);

        double permPercent = totalAbsences > 0 ? Math.round((permCount * 1000.0) / totalAbsences) / 10.0 : 0.0;
        double unexcusedPercent = totalAbsences > 0 ? Math.round((unexcusedCount * 1000.0) / totalAbsences) / 10.0 : 0.0;

        Long accountId = student.getAccountId();
        long recordedDays = 0;
        try {
            if (accountId != null) {
                recordedDays = attendanceRepository.findAll().stream()
                        .filter(a -> Objects.equals(a.getAccountId(), accountId))
                        .map(Attendance::getDate)
                        .filter(Objects::nonNull)
                        .distinct()
                        .count();
            }
        } catch (Exception ignored) {}

        if (recordedDays < totalAbsences) {
            recordedDays = Math.max(totalAbsences, 1);
        }
        long presentDays = Math.max(0, recordedDays - totalAbsences);
        double attendanceRate = recordedDays > 0 ? Math.round((presentDays * 1000.0) / recordedDays) / 10.0 : 100.0;
        double absenceRate = recordedDays > 0 ? Math.round((totalAbsences * 1000.0) / recordedDays) / 10.0 : 0.0;

        res.put("totalAbsences", totalAbsences);
        res.put("permissionCount", permCount);
        res.put("unexcusedCount", unexcusedCount);
        res.put("permissionPercent", permPercent);
        res.put("unexcusedPercent", unexcusedPercent);
        res.put("attendanceRate", attendanceRate);
        res.put("absenceRate", absenceRate);
        res.put("recordedDays", recordedDays);
        res.put("presentDays", presentDays);
        return res;
    }

    @GetMapping("/leave-requests")
    public ResponseEntity<?> getMyLeaveRequests(HttpSession session) {
        Student student = (Student) session.getAttribute("loggedInStudent");
        if (student == null) return ResponseEntity.status(401).build();

        List<LeaveRequest> list = leaveRequestRepository.findByStudentIdOrderByCreatedAtDesc(student.getId());
        return ResponseEntity.ok(list);
    }

    @PostMapping("/leave-request")
    public ResponseEntity<?> submitLeaveRequest(@RequestBody LeaveRequestPayload payload, HttpSession session) {
        Student student = (Student) session.getAttribute("loggedInStudent");
        if (student == null) return ResponseEntity.status(401).build();

        if (student.getId() != null) {
            student = studentRepository.findById(student.getId()).orElse(student);
            session.setAttribute("loggedInStudent", student);
        }

        LocalDate start;
        LocalDate end;
        try {
            start = (payload.getStartDate() != null && !payload.getStartDate().trim().isEmpty())
                    ? LocalDate.parse(payload.getStartDate().trim())
                    : LocalDate.now();
        } catch (Exception e) {
            start = LocalDate.now();
        }
        try {
            end = (payload.getEndDate() != null && !payload.getEndDate().trim().isEmpty())
                    ? LocalDate.parse(payload.getEndDate().trim())
                    : start;
        } catch (Exception e) {
            end = start;
        }
        if (end.isBefore(start)) {
            end = start;
        }
        if (end.isAfter(start.plusDays(30))) {
            end = start.plusDays(30);
        }

        String reason = payload.getReason() != null ? payload.getReason().trim() : "";
        if (reason.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "សូមបញ្ចូលមូលហេតុនៃការសុំច្បាប់!"));
        }

        Long stuAccId = student.getAccountId();

        LeaveRequest lr = new LeaveRequest();
        lr.setStudent(student);
        lr.setAccountId(stuAccId);
        lr.setClassName(student.getClassName());
        lr.setAcademicYear(student.getAcademicYear() != null && !student.getAcademicYear().trim().isEmpty()
                ? student.getAcademicYear().trim() : "២០២៦-២០២៧");
        lr.setStartDate(start);
        lr.setEndDate(end);
        lr.setLeaveType(payload.getLeaveType() != null && !payload.getLeaveType().trim().isEmpty()
                ? payload.getLeaveType().trim() : "ឈឺ");
        lr.setLeaveSession(payload.getLeaveSession() != null && !payload.getLeaveSession().trim().isEmpty()
                ? payload.getLeaveSession().trim() : "ពេញមួយថ្ងៃ");
        lr.setReason(reason);
        lr.setParentPhone(payload.getParentPhone() != null ? payload.getParentPhone().trim() : "");
        lr.setStatus("PENDING");
        lr.setCreatedAt(LocalDateTime.now());

        LeaveRequest saved = leaveRequestRepository.save(lr);
        syncAttendanceForLeave(student, start, end, stuAccId, true);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "បានបញ្ជូនពាក្យសុំច្បាប់ទៅលោកគ្រូ/អ្នកគ្រូដោយជោគជ័យ!",
                "leaveRequestId", saved.getId()
        ));
    }

    @DeleteMapping("/leave-request/{id}")
    public ResponseEntity<?> cancelLeaveRequest(@PathVariable Long id, HttpSession session) {
        Student student = (Student) session.getAttribute("loggedInStudent");
        if (student == null) return ResponseEntity.status(401).build();

        Optional<LeaveRequest> lrOpt = leaveRequestRepository.findById(id);
        if (lrOpt.isEmpty() || lrOpt.get().getStudent() == null || !lrOpt.get().getStudent().getId().equals(student.getId())) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "រកមិនឃើញសំណើសុំច្បាប់នេះទេ!"));
        }

        LeaveRequest lr = lrOpt.get();
        if (!lr.isPending()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "មិនអាចលុបសំណើដែលគ្រូបានពិនិត្យរួចហើយនោះទេ!"));
        }

        Long stuAccId = student.getAccountId();
        leaveRequestRepository.delete(lr);
        syncAttendanceForLeave(student, lr.getStartDate(), lr.getEndDate(), stuAccId, false);

        return ResponseEntity.ok(Map.of("success", true, "message", "បានបោះបង់សំណើសុំច្បាប់ដោយជោគជ័យ!"));
    }

    private void syncAttendanceForLeave(Student student, LocalDate start, LocalDate end, Long accountId, boolean markPermission) {
        if (student == null || student.getId() == null || start == null || end == null) return;
        try {
            List<Attendance> existing = attendanceRepository.findByStudentIdAndDateBetween(student.getId(), start, end);
            if (markPermission) {
                List<Attendance> toSave = new ArrayList<>();
                LocalDate d = start;
                int guard = 0;
                while (!d.isAfter(end) && guard < 35) {
                    final LocalDate curr = d;
                    Optional<Attendance> found = existing.stream().filter(a -> curr.equals(a.getDate())).findFirst();
                    if (found.isPresent()) {
                        Attendance att = found.get();
                        att.setPresent(false);
                        att.setAbsenceType("PERMISSION");
                        if (att.getAccountId() == null) att.setAccountId(accountId);
                        toSave.add(att);
                    } else {
                        Attendance att = new Attendance(student, curr, false, "PERMISSION");
                        att.setAccountId(accountId);
                        toSave.add(att);
                    }
                    d = d.plusDays(1);
                    guard++;
                }
                if (!toSave.isEmpty()) {
                    attendanceRepository.saveAll(toSave);
                }
            } else {
                for (Attendance att : existing) {
                    if (att.isPermission()) {
                        attendanceRepository.delete(att);
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    public static class SubmitPayload {
        private String month;
        private String subjectName;
        private double totalScore;

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
        public double getTotalScore() { return totalScore; }
        public void setTotalScore(double totalScore) { this.totalScore = totalScore; }

        private String studentWriting;
        public String getStudentWriting() { return studentWriting; }
        public void setStudentWriting(String studentWriting) { this.studentWriting = studentWriting; }
    }

    public static class LeaveRequestPayload {
        private String startDate;
        private String endDate;
        private String leaveType;
        private String leaveSession;
        private String reason;
        private String parentPhone;

        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }
        public String getLeaveType() { return leaveType; }
        public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
        public String getLeaveSession() { return leaveSession; }
        public void setLeaveSession(String leaveSession) { this.leaveSession = leaveSession; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getParentPhone() { return parentPhone; }
        public void setParentPhone(String parentPhone) { this.parentPhone = parentPhone; }
    }
}

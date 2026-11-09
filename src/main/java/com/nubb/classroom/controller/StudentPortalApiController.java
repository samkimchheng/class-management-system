package com.nubb.classroom.controller;

import com.nubb.classroom.model.*;
import com.nubb.classroom.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

        Long stuAccId = (student.getAccountId() == null || student.getAccountId() == 1L) ? 97L : student.getAccountId();
        List<Exam> exams = examRepository.findAll().stream()
                .filter(e -> month != null && month.equals(e.getMonth()))
                .filter(e -> {
                    Long exAccId = (e.getAccountId() == null || e.getAccountId() == 1L) ? 97L : e.getAccountId();
                    if (stuAccId.equals(exAccId)) return true;
                    return stuAccId != 98L && exAccId != 98L;
                })
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

        Long stuAccId = (student.getAccountId() == null || student.getAccountId() == 1L) ? 97L : student.getAccountId();
        Optional<Exam> examOpt = examRepository.findAll().stream()
                .filter(e -> month != null && month.equals(e.getMonth()) && subjectName != null && subjectName.equals(e.getSubjectName()))
                .filter(e -> {
                    Long exAccId = (e.getAccountId() == null || e.getAccountId() == 1L) ? 97L : e.getAccountId();
                    if (stuAccId.equals(exAccId)) return true;
                    return stuAccId != 98L && exAccId != 98L;
                })
                .filter(e -> ExamApiController.classMatches(e.getClassName(), student.getClassName()))
                .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                .findFirst();

        if (examOpt.isPresent()) {
            Exam exam = examOpt.get();
            List<Question> questions = questionRepository.findByExamIdAndAccountId(exam.getId(), exam.getAccountId());
            if (questions.isEmpty()) {
                questions = questionRepository.findAll().stream().filter(q -> exam.getId().equals(q.getExamId())).toList();
            }
            return ResponseEntity.ok(Map.of("exam", exam, "questions", questions));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitExam(@RequestBody SubmitPayload payload, HttpSession session) {
        Student student = (Student) session.getAttribute("loggedInStudent");
        if (student == null) return ResponseEntity.status(401).build();

        Long stuAccId = (student.getAccountId() == null || student.getAccountId() == 1L) ? 97L : student.getAccountId();
        Optional<Score> existingOpt = scoreRepository.findByStudentIdAndMonthAndClassNameAndSubjectNameAndAccountId(
                student.getId(), payload.getMonth(), student.getClassName(), payload.getSubjectName(), stuAccId);

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

        scoreRepository.save(score);

        return ResponseEntity.ok(Map.of("success", true, "totalScore", payload.getTotalScore()));
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

        Long stuAccId = (student.getAccountId() == null || student.getAccountId() == 1L) ? 97L : student.getAccountId();

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

        Long stuAccId = (student.getAccountId() == null || student.getAccountId() == 1L) ? 97L : student.getAccountId();
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

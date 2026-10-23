package com.nubb.classroom.controller;

import com.nubb.classroom.model.*;
import com.nubb.classroom.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/subjects")
    public ResponseEntity<?> getSubjects(@RequestParam String month, HttpSession session) {
        Student student = (Student) session.getAttribute("loggedInStudent");
        if (student == null) return ResponseEntity.status(401).build();
        
        List<Exam> exams = examRepository.findByMonthAndClassNameAndAccountId(month, student.getClassName(), student.getAccountId());
        List<String> subjects = exams.stream().map(Exam::getSubjectName).distinct().toList();
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/exam")
    public ResponseEntity<?> getExam(@RequestParam String month, @RequestParam String subjectName, HttpSession session) {
        Student student = (Student) session.getAttribute("loggedInStudent");
        if (student == null) return ResponseEntity.status(401).build();
        
        Optional<Exam> examOpt = examRepository.findByMonthAndClassNameAndSubjectNameAndAccountId(month, student.getClassName(), subjectName, student.getAccountId());
        if (examOpt.isPresent()) {
            List<Question> questions = questionRepository.findByExamIdAndAccountId(examOpt.get().getId(), student.getAccountId());
            return ResponseEntity.ok(Map.of("exam", examOpt.get(), "questions", questions));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitExam(@RequestBody SubmitPayload payload, HttpSession session) {
        Student student = (Student) session.getAttribute("loggedInStudent");
        if (student == null) return ResponseEntity.status(401).build();
        
        Optional<Score> existingOpt = scoreRepository.findByStudentIdAndMonthAndClassNameAndSubjectNameAndAccountId(
                student.getId(), payload.getMonth(), student.getClassName(), payload.getSubjectName(), student.getAccountId());
        
        Score score = existingOpt.orElse(new Score());
        score.setStudentId(student.getId());
        score.setMonth(payload.getMonth());
        score.setClassName(student.getClassName());
        score.setSubjectName(payload.getSubjectName());
        score.setAccountId(student.getAccountId());
        
        score.setMonthlyScore(payload.getTotalScore());
        
        score.setTotal(score.getMonthlyScore() + score.getAttendanceScore() + score.getAssignmentScore());
        
        scoreRepository.save(score);
        
        return ResponseEntity.ok(Map.of("success", true, "totalScore", payload.getTotalScore()));
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
    }
}
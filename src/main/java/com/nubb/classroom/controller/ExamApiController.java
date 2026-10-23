package com.nubb.classroom.controller;

import com.nubb.classroom.model.Exam;
import com.nubb.classroom.model.Question;
import com.nubb.classroom.repository.ExamRepository;
import com.nubb.classroom.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/exams")
public class ExamApiController {
    @org.springframework.beans.factory.annotation.Autowired
    private jakarta.servlet.http.HttpSession session;


    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @PostMapping("/save")
    public ResponseEntity<?> saveExam(@RequestBody ExamPayload payload) {
        // Find or create exam
        Exam exam = examRepository.findByMonthAndClassNameAndSubjectNameAndAccountId(payload.getMonth(), payload.getClassName(), payload.getSubjectName(), (Long) session.getAttribute("ACCOUNT_ID"))
                .orElse(new Exam());
        exam.setMonth(payload.getMonth());
        exam.setClassName(payload.getClassName());
        exam.setSubjectName(payload.getSubjectName());
        exam.setAccountId((Long) session.getAttribute("ACCOUNT_ID")); examRepository.save(exam);

        // Delete old questions for this exam
        List<Question> oldQuestions = questionRepository.findByExamIdAndAccountId(exam.getId(), (Long) session.getAttribute("ACCOUNT_ID"));
        questionRepository.deleteAll(oldQuestions);

        // Save new questions
        for (Question q : payload.getQuestions()) {
            q.setExamId(exam.getId());
            q.setAccountId((Long) session.getAttribute("ACCOUNT_ID")); questionRepository.save(q);
        }

        return ResponseEntity.ok().body(Map.of("success", true, "message", "បានរក្សាទុកដោយជោគជ័យ"));
    }

    @GetMapping("/get")
    public ResponseEntity<?> getExam(@RequestParam String month, @RequestParam String className, @RequestParam String subjectName) {
        Optional<Exam> examOpt = examRepository.findByMonthAndClassNameAndSubjectNameAndAccountId(month, className, subjectName, (Long) session.getAttribute("ACCOUNT_ID"));
        if (examOpt.isPresent()) {
            List<Question> questions = questionRepository.findByExamIdAndAccountId(examOpt.get().getId(), (Long) session.getAttribute("ACCOUNT_ID"));
            return ResponseEntity.ok(Map.of("exam", examOpt.get(), "questions", questions));
        }
        return ResponseEntity.notFound().build();
    }

    public static class ExamPayload {
        private String month;
        private String className;
        private String subjectName;

        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
        private List<Question> questions;

        // Getters and Setters
        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        public List<Question> getQuestions() { return questions; }
        public void setQuestions(List<Question> questions) { this.questions = questions; }
    }
}




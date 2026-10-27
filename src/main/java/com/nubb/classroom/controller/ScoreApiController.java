package com.nubb.classroom.controller;

import com.nubb.classroom.model.Score;
import com.nubb.classroom.model.ScoreRule;
import com.nubb.classroom.model.Student;
import com.nubb.classroom.repository.ScoreRepository;
import com.nubb.classroom.repository.ScoreRuleRepository;
import com.nubb.classroom.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/scores")
public class ScoreApiController {

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @jakarta.annotation.PostConstruct
    public void fixDbSchema() {
        String[] cols = {"listening", "speaking", "reading", "writing", "dictation", "grammar", "vocabulary"};
        for (String col : cols) {
            try {
                jdbcTemplate.execute("ALTER TABLE score ALTER COLUMN " + col + " DROP NOT NULL");
            } catch (Exception e) {
                // Ignore if column doesn't exist
            }
        }
    }
    @org.springframework.beans.factory.annotation.Autowired
    private jakarta.servlet.http.HttpSession session;

    @Autowired
    private ScoreRepository scoreRepository;
    
    @Autowired
    private ScoreRuleRepository scoreRuleRepository;

    @Autowired
    private StudentRepository studentRepository;

    @GetMapping("/get")
    public ResponseEntity<List<Score>> getScores(@RequestParam String month, @RequestParam String className, @RequestParam String subjectName) {
        String academicYear = (String) session.getAttribute("CURRENT_ACADEMIC_YEAR");
        if (academicYear == null || academicYear.trim().isEmpty()) {
            academicYear = "២០២៦-២០២៧";
        }
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (className == null || className.isEmpty() || "ថ្នាក់រៀនទាំងអស់".equals(className)) {
            return ResponseEntity.ok(scoreRepository.findByMonthAndSubjectNameAndAccountIdAndAcademicYear(
                month, subjectName, accountId, academicYear));
        }
        return ResponseEntity.ok(scoreRepository.findByMonthAndClassNameAndSubjectNameAndAccountIdAndAcademicYear(
            month, className, subjectName, accountId, academicYear));
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveScores(@RequestBody SavePayload payload) {
        String academicYear = (String) session.getAttribute("CURRENT_ACADEMIC_YEAR");
        if (academicYear == null || academicYear.trim().isEmpty()) {
            academicYear = "២០២៦-២០២៧";
        }
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        for (Score s : payload.getScores()) {
            String actualClassName = payload.getClassName();
            if (actualClassName == null || actualClassName.isEmpty() || "ថ្នាក់រៀនទាំងអស់".equals(actualClassName)) {
                if (s.getClassName() != null && !s.getClassName().isEmpty() && !"ថ្នាក់រៀនទាំងអស់".equals(s.getClassName())) {
                    actualClassName = s.getClassName();
                } else {
                    Optional<Student> stOpt = studentRepository.findById(s.getStudentId());
                    if (stOpt.isPresent() && stOpt.get().getClassName() != null) {
                        actualClassName = stOpt.get().getClassName();
                    }
                }
            }

            List<Score> existingScores = scoreRepository.findByStudentIdAndMonthAndSubjectNameAndAccountIdAndAcademicYear(
                    s.getStudentId(), payload.getMonth(), payload.getSubjectName(), accountId, academicYear);
            
            Score score = (!existingScores.isEmpty()) ? existingScores.get(0) : new Score();
            score.setStudentId(s.getStudentId());
            score.setMonth(payload.getMonth());
            score.setClassName(actualClassName);
            score.setSubjectName(payload.getSubjectName());
            score.setMonthlyScore(s.getMonthlyScore());
            score.setAttendanceScore(s.getAttendanceScore());
            score.setAssignmentScore(s.getAssignmentScore());
            score.setAcademicYear(academicYear);
            
            score.setTotal(s.getMonthlyScore() + s.getAttendanceScore() + s.getAssignmentScore());
            
            score.setAccountId(accountId); 
            scoreRepository.save(score);
        }
        return ResponseEntity.ok(Map.of("success", true));
    }
    
    @GetMapping("/rules")
    public ResponseEntity<ScoreRule> getScoreRule(@RequestParam String className, @RequestParam String subjectName) {
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        Optional<ScoreRule> rule = scoreRuleRepository.findByAccountIdAndClassNameAndSubjectName(accountId, className, subjectName);
        if (rule.isPresent()) {
            return ResponseEntity.ok(rule.get());
        }
        
        String khmerAllClassrooms = "ថ្នាក់រៀនទាំងអស់";
        if (!khmerAllClassrooms.equals(className)) {
            Optional<ScoreRule> globalRule = scoreRuleRepository.findByAccountIdAndClassNameAndSubjectName(accountId, khmerAllClassrooms, subjectName);
            if (globalRule.isPresent()) {
                return ResponseEntity.ok(globalRule.get());
            }
        }
        
        ScoreRule defaultRule = new ScoreRule();
        defaultRule.setAccountId(accountId);
        defaultRule.setClassName(className);
        defaultRule.setSubjectName(subjectName);
        defaultRule.setMaxMonthlyScore(0);
        defaultRule.setMaxAttendanceScore(0);
        defaultRule.setMaxAssignmentScore(0);
        return ResponseEntity.ok(defaultRule);
    }
    
    @PostMapping("/rules")
    public ResponseEntity<?> saveScoreRule(@RequestBody ScoreRule payload) {
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        Optional<ScoreRule> existingOpt = scoreRuleRepository.findByAccountIdAndClassNameAndSubjectName(accountId, payload.getClassName(), payload.getSubjectName());
        
        ScoreRule rule = existingOpt.orElse(new ScoreRule());
        rule.setAccountId(accountId);
        rule.setClassName(payload.getClassName());
        rule.setSubjectName(payload.getSubjectName());
        rule.setMaxMonthlyScore(payload.getMaxMonthlyScore());
        rule.setMaxAttendanceScore(payload.getMaxAttendanceScore());
        rule.setMaxAssignmentScore(payload.getMaxAssignmentScore());
        
        scoreRuleRepository.save(rule);
        
        return ResponseEntity.ok(Map.of("success", true));
    }

    public static class SavePayload {
        private String month;
        private String className;
        private String subjectName;
        private List<Score> scores;

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

        public List<Score> getScores() { return scores; }
        public void setScores(List<Score> scores) { this.scores = scores; }
    }
}

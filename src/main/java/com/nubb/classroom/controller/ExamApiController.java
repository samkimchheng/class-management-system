package com.nubb.classroom.controller;

import com.nubb.classroom.model.Exam;
import com.nubb.classroom.model.Question;
import com.nubb.classroom.repository.ExamRepository;
import com.nubb.classroom.repository.QuestionRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/exams")
public class ExamApiController {

    @Autowired
    private HttpSession session;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private QuestionRepository questionRepository;

    public static boolean classMatches(String examClassName, String targetClass) {
        return LessonController.isClassMatching(examClassName, targetClass);
    }

    public static boolean hasCommonClass(String classStr1, String classStr2) {
        if (classStr1 == null || classStr2 == null) return false;
        String[] parts1 = classStr1.split("[,;]+");
        String[] parts2 = classStr2.split("[,;]+");
        for (String p1 : parts1) {
            String t1 = p1.trim();
            if (t1.isEmpty()) continue;
            for (String p2 : parts2) {
                String t2 = p2.trim();
                if (t2.isEmpty()) continue;
                if (t1.equalsIgnoreCase(t2)) {
                    return true;
                }
            }
        }
        return false;
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveExam(@RequestBody ExamPayload payload) {
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "សូមចូលប្រើប្រាស់ជាមុនសិន"));
        }
        if (accountId == 1L) {
            accountId = 97L;
        }

        String targetClass = "";
        if (payload.getClasses() != null && !payload.getClasses().isEmpty()) {
            targetClass = String.join(", ", payload.getClasses());
        } else if (payload.getClassName() != null && !payload.getClassName().trim().isEmpty()) {
            targetClass = payload.getClassName().trim();
        }

        if (targetClass.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "សូមជ្រើសរើសថ្នាក់រៀនយ៉ាងហោចណាស់មួយ"));
        }

        Exam exam = null;
        if (payload.getId() != null) {
            exam = examRepository.findByIdAndAccountId(payload.getId(), accountId).orElse(null);
        }

        if (exam == null) {
            // Find existing exam for same month, subject, and matching class
            List<Exam> existingList = examRepository.findByMonthAndSubjectNameAndAccountId(payload.getMonth(), payload.getSubjectName(), accountId);
            for (Exam e : existingList) {
                if (e.getClassName() != null && (e.getClassName().equalsIgnoreCase(targetClass) || hasCommonClass(e.getClassName(), targetClass))) {
                    exam = e;
                    break;
                }
            }
        }

        if (exam == null) {
            exam = new Exam();
        }

        exam.setMonth(payload.getMonth());
        exam.setClassName(targetClass);
        exam.setSubjectName(payload.getSubjectName());
        exam.setAccountId(accountId);
        exam.setReadingPassage(payload.getReadingPassage());
        exam.setWordBank(payload.getWordBank());
        exam.setSection1Title(payload.getSection1Title());
        exam.setSection1Instruction(payload.getSection1Instruction());
        exam.setSection2Title(payload.getSection2Title());
        exam.setSection2Instruction(payload.getSection2Instruction());
        exam.setSection3Title(payload.getSection3Title());
        exam.setSection3Instruction(payload.getSection3Instruction());
        exam.setSection4Title(payload.getSection4Title());
        exam.setSection4Instruction(payload.getSection4Instruction());
        exam.setWritingPrompt(payload.getWritingPrompt());
        exam.setWritingMaxPoints(payload.getWritingMaxPoints());
        examRepository.save(exam);

        // Delete old questions for this exam
        List<Question> oldQuestions = questionRepository.findByExamIdAndAccountId(exam.getId(), accountId);
        questionRepository.deleteAll(oldQuestions);

        // Save new questions
        if (payload.getQuestions() != null) {
            for (Question q : payload.getQuestions()) {
                q.setExamId(exam.getId());
                q.setAccountId(accountId);
                questionRepository.save(q);
            }
        }

        return ResponseEntity.ok().body(Map.of(
                "success", true,
                "message", "វិញ្ញាសាត្រូវបានរក្សាទុកដោយជោគជ័យ!",
                "examId", exam.getId(),
                "className", exam.getClassName()
        ));
    }

    @GetMapping("/get")
    public ResponseEntity<?> getExam(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String subjectName) {
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) return ResponseEntity.status(401).build();
        if (accountId == 1L) {
            accountId = 97L;
        }

        Optional<Exam> examOpt = Optional.empty();
        if (id != null) {
            examOpt = examRepository.findByIdAndAccountId(id, accountId);
        } else if (month != null && subjectName != null) {
            List<Exam> exams = examRepository.findByMonthAndSubjectNameAndAccountId(month, subjectName, accountId);
            if (className != null && !className.trim().isEmpty() && !className.trim().equals("ថ្នាក់រៀនទាំងអស់")) {
                examOpt = exams.stream().filter(e -> classMatches(e.getClassName(), className)).findFirst();
            } else if (!exams.isEmpty()) {
                examOpt = Optional.of(exams.get(0));
            }
        }

        if (examOpt.isPresent()) {
            List<Question> questions = questionRepository.findByExamIdAndAccountId(examOpt.get().getId(), accountId);
            return ResponseEntity.ok(Map.of("exam", examOpt.get(), "questions", questions));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/list")
    public ResponseEntity<?> listExams() {
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) return ResponseEntity.status(401).build();
        if (accountId == 1L) {
            accountId = 97L;
        }

        List<Exam> exams = examRepository.findByAccountId(accountId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Exam e : exams) {
            int qCount = questionRepository.findByExamIdAndAccountId(e.getId(), accountId).size();
            Map<String, Object> map = new HashMap<>();
            map.put("id", e.getId());
            map.put("month", e.getMonth() != null ? e.getMonth() : "");
            map.put("className", e.getClassName() != null ? e.getClassName() : "");
            map.put("subjectName", e.getSubjectName() != null ? e.getSubjectName() : "");
            map.put("questionCount", qCount);
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteExam(@PathVariable Long id) {
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) return ResponseEntity.status(401).build();
        if (accountId == 1L) {
            accountId = 97L;
        }

        Optional<Exam> examOpt = examRepository.findByIdAndAccountId(id, accountId);
        if (examOpt.isPresent()) {
            List<Question> questions = questionRepository.findByExamIdAndAccountId(id, accountId);
            questionRepository.deleteAll(questions);
            examRepository.delete(examOpt.get());
            return ResponseEntity.ok(Map.of("success", true, "message", "បានលុបវិញ្ញាសាដោយជោគជ័យ"));
        }
        return ResponseEntity.notFound().build();
    }

    public static class ExamPayload {
        private Long id;
        private String month;
        private String className;
        private List<String> classes;
        private String subjectName;
        private List<Question> questions;
        private String readingPassage;
        private String wordBank;
        private String section1Title;
        private String section1Instruction;
        private String section2Title;
        private String section2Instruction;
        private String section3Title;
        private String section3Instruction;

        public String getReadingPassage() { return readingPassage; }
        public void setReadingPassage(String readingPassage) { this.readingPassage = readingPassage; }
        public String getWordBank() { return wordBank; }
        public void setWordBank(String wordBank) { this.wordBank = wordBank; }
        public String getSection1Title() { return section1Title; }
        public void setSection1Title(String section1Title) { this.section1Title = section1Title; }
        public String getSection1Instruction() { return section1Instruction; }
        public void setSection1Instruction(String section1Instruction) { this.section1Instruction = section1Instruction; }
        public String getSection2Title() { return section2Title; }
        public void setSection2Title(String section2Title) { this.section2Title = section2Title; }
        public String getSection2Instruction() { return section2Instruction; }
        public void setSection2Instruction(String section2Instruction) { this.section2Instruction = section2Instruction; }
        public String getSection3Title() { return section3Title; }
        public void setSection3Title(String section3Title) { this.section3Title = section3Title; }
        public String getSection3Instruction() { return section3Instruction; }
        public void setSection3Instruction(String section3Instruction) { this.section3Instruction = section3Instruction; }

        private String section4Title;
        private String section4Instruction;
        private String writingPrompt;
        private Integer writingMaxPoints;

        public String getSection4Title() { return section4Title; }
        public void setSection4Title(String section4Title) { this.section4Title = section4Title; }
        public String getSection4Instruction() { return section4Instruction; }
        public void setSection4Instruction(String section4Instruction) { this.section4Instruction = section4Instruction; }
        public String getWritingPrompt() { return writingPrompt; }
        public void setWritingPrompt(String writingPrompt) { this.writingPrompt = writingPrompt; }
        public Integer getWritingMaxPoints() { return writingMaxPoints; }
        public void setWritingMaxPoints(Integer writingMaxPoints) { this.writingMaxPoints = writingMaxPoints; }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        public List<String> getClasses() { return classes; }
        public void setClasses(List<String> classes) { this.classes = classes; }
        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
        public List<Question> getQuestions() { return questions; }
        public void setQuestions(List<Question> questions) { this.questions = questions; }
    }
}

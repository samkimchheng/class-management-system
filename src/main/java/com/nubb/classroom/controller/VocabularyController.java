package com.nubb.classroom.controller;

import com.nubb.classroom.config.StandardVocabularySeeder;
import com.nubb.classroom.model.Account;
import com.nubb.classroom.model.Student;
import com.nubb.classroom.model.StudentVocabularyProgress;
import com.nubb.classroom.model.VocabularyWord;
import com.nubb.classroom.repository.AccountRepository;
import com.nubb.classroom.repository.StudentRepository;
import com.nubb.classroom.repository.StudentVocabularyProgressRepository;
import com.nubb.classroom.repository.VocabularyWordRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class VocabularyController {

    @Autowired
    private VocabularyWordRepository vocabularyWordRepository;

    @Autowired
    private StudentVocabularyProgressRepository studentVocabularyProgressRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private StandardVocabularySeeder standardVocabularySeeder;

    @Autowired
    private HttpSession session;

    // Helper to format unit display name e.g. "unit 1" -> "Unit 1"
    public static String formatUnitDisplayName(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "Unit ?";
        String s = raw.trim();
        // Capitalize 'unit'
        if (s.toLowerCase().startsWith("unit")) {
            return "Unit " + s.substring(4).trim();
        }
        return s;
    }

    // Helper to check if a word belongs to a target unit filter
    public static boolean matchesUnit(String wordUnit, String targetUnit) {
        if (targetUnit == null || targetUnit.trim().isEmpty() || "ALL".equalsIgnoreCase(targetUnit.trim())) {
            return true;
        }
        if (wordUnit == null) return false;
        String w = wordUnit.toLowerCase().trim();
        String t = targetUnit.toLowerCase().trim();
        if (w.equals(t)) return true;

        // If target is like "unit 1" or "1", extract number
        String num = t.replaceAll("[^0-9]", "");
        if (!num.isEmpty()) {
            // Regex boundary check for number
            return w.matches(".*\\b" + num + "\\b.*");
        }
        return w.contains(t);
    }

    // ==========================================
    // 1. TEACHER MANAGEMENT VIEW
    // ==========================================
    @GetMapping("/vocabulary")
    public String teacherVocabularyView(
            @RequestParam(required = false) String unit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String className,
            Model model,
            HttpServletResponse response) {

        if (response != null) {
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
        }

        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) {
            return "redirect:/login";
        }

        Account teacher = accountRepository.findById(accountId).orElse(null);
        if (teacher == null) {
            return "redirect:/login";
        }

        // Strict access control: Only Sam Kimchheng or ADMIN or account with explicit permission
        String role = (String) session.getAttribute("ACCOUNT_ROLE");
        String username = (String) session.getAttribute("ACCOUNT_USERNAME");
        Boolean vocabPerm = (Boolean) session.getAttribute("VOCABULARY_PERMISSION");
        boolean allowed = Boolean.TRUE.equals(vocabPerm)
                || "ADMIN".equalsIgnoreCase(role)
                || Long.valueOf(97L).equals(accountId)
                || "samkimchheng".equalsIgnoreCase(username)
                || (teacher != null && ("samkimchheng".equalsIgnoreCase(teacher.getUsername()) || Boolean.TRUE.equals(teacher.getVocabularyPermission())));

        if (!allowed) {
            return "redirect:/?accessDenied=vocab";
        }

        // Auto-seed if empty
        if (vocabularyWordRepository.countByGradeLevel(10) < 460) {
            standardVocabularySeeder.seedGrade10Vocabulary();
        }

        List<VocabularyWord> allWords = vocabularyWordRepository.findByGradeLevelOrderByWordNumberAsc(10);

        // Build distinct ordered list of units (Unit 1 to Unit 31)
        Set<String> unitSet = new TreeSet<>((a, b) -> {
            int numA = extractFirstNumber(a);
            int numB = extractFirstNumber(b);
            if (numA != numB) return Integer.compare(numA, numB);
            return a.compareToIgnoreCase(b);
        });
        for (VocabularyWord w : allWords) {
            if (w.getUnitName() != null && !w.getUnitName().trim().isEmpty()) {
                unitSet.add(w.getUnitName().trim().toLowerCase());
            }
        }
        List<String> units = new ArrayList<>(unitSet);

        // Filter words
        String selectedUnit = (unit != null && !unit.trim().isEmpty()) ? unit.trim() : "ALL";
        String cleanSearch = (search != null) ? search.trim().toLowerCase() : "";

        List<VocabularyWord> filteredWords = allWords.stream()
                .filter(w -> matchesUnit(w.getUnitName(), selectedUnit))
                .filter(w -> {
                    if (cleanSearch.isEmpty()) return true;
                    boolean mEng = w.getEnglishWord() != null && w.getEnglishWord().toLowerCase().contains(cleanSearch);
                    boolean mClean = w.getCleanWord() != null && w.getCleanWord().toLowerCase().contains(cleanSearch);
                    boolean mKhm = w.getKhmerTranslation() != null && w.getKhmerTranslation().toLowerCase().contains(cleanSearch);
                    boolean mUnit = w.getUnitName() != null && w.getUnitName().toLowerCase().contains(cleanSearch);
                    return mEng || mClean || mKhm || mUnit;
                })
                .collect(Collectors.toList());

        // Statistics
        long totalWords = allWords.size();
        long activeWords = allWords.stream().filter(w -> Boolean.TRUE.equals(w.getActive())).count();

        // ----------------------------------------------------
        // Student Progress Tracking under teacher supervision
        // ----------------------------------------------------
        List<Student> allStudents;
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);

        if (isAdmin) {
            allStudents = studentRepository.findAll();
        } else {
            // Find teacher's students (e.g. matching accountId or teaching classes)
            allStudents = studentRepository.findByAccountId(accountId);
            if (allStudents.isEmpty()) {
                allStudents = studentRepository.findAll();
            }
        }

        // Filter to Grade 10 students (e.g. 10E, 10F, 10G, etc.)
        List<Student> grade10Students = allStudents.stream()
                .filter(s -> s.getClassName() != null && (s.getClassName().contains("10") || s.getClassName().contains("១០")))
                .sorted(Comparator.comparing(Student::getClassName).thenComparing(Student::getName))
                .collect(Collectors.toList());

        // If no Grade 10 specific, fallback to all students
        if (grade10Students.isEmpty()) {
            grade10Students = allStudents;
        }

        // Distinct Grade 10 classes
        List<String> grade10Classes = grade10Students.stream()
                .map(Student::getClassName)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        String selectedClass = (className != null && !className.trim().isEmpty()) ? className.trim() : "ALL";

        // Build Student Progress Cards
        List<Map<String, Object>> studentProgressList = new ArrayList<>();
        long totalMasteredAcrossStudents = 0;
        int activePracticingStudents = 0;

        for (Student s : grade10Students) {
            if (!"ALL".equalsIgnoreCase(selectedClass) && !selectedClass.equalsIgnoreCase(s.getClassName())) {
                continue;
            }

            List<StudentVocabularyProgress> progressList = studentVocabularyProgressRepository.findByStudentId(s.getId());
            long masteredCount = progressList.stream().filter(p -> Boolean.TRUE.equals(p.getMastered())).count();
            int totalPractice = progressList.stream().mapToInt(StudentVocabularyProgress::getPracticeCount).sum();

            if (totalPractice > 0) activePracticingStudents++;
            totalMasteredAcrossStudents += masteredCount;

            int percent = (int) Math.round(((double) masteredCount / Math.max(1, totalWords)) * 100.0);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("student", s);
            row.put("masteredCount", masteredCount);
            row.put("totalWords", totalWords);
            row.put("percent", percent);
            row.put("totalPractice", totalPractice);
            
            LocalDateTime lastActive = progressList.stream()
                    .map(StudentVocabularyProgress::getLastPracticed)
                    .filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
            row.put("lastActive", lastActive);

            studentProgressList.add(row);
        }

        // Sort by mastered count desc
        studentProgressList.sort((a, b) -> Long.compare((Long) b.get("masteredCount"), (Long) a.get("masteredCount")));

        model.addAttribute("teacherAccount", teacher);
        model.addAttribute("words", filteredWords);
        model.addAttribute("units", units);
        model.addAttribute("selectedUnit", selectedUnit);
        model.addAttribute("searchQuery", search != null ? search : "");
        model.addAttribute("totalWords", totalWords);
        model.addAttribute("activeWords", activeWords);
        model.addAttribute("totalUnits", units.size());
        model.addAttribute("classes", grade10Classes);
        model.addAttribute("selectedClass", selectedClass);
        model.addAttribute("studentProgressList", studentProgressList);
        model.addAttribute("totalStudentsCount", studentProgressList.size());
        model.addAttribute("activePracticingStudents", activePracticingStudents);
        model.addAttribute("totalMasteredAcrossStudents", totalMasteredAcrossStudents);

        return "vocabulary";
    }

    // ==========================================
    // 2. STUDENT LEARNING VIEW
    // ==========================================
    @GetMapping("/test/vocabulary")
    public String studentVocabularyView(
            @RequestParam(required = false) String unit,
            @RequestParam(required = false) String search,
            Model model,
            HttpServletResponse response) {

        if (response != null) {
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
        }

        Student student = (Student) session.getAttribute("loggedInStudent");
        if (student == null) {
            return "redirect:/test?redirectTo=/test/vocabulary";
        }

        // Keep student fresh from DB
        if (student.getId() != null) {
            student = studentRepository.findById(student.getId()).orElse(student);
            session.setAttribute("loggedInStudent", student);
        }
        session.setAttribute("ACTIVE_USER_MODE", "STUDENT");

        // Auto-seed if empty
        if (vocabularyWordRepository.countByGradeLevel(10) < 460) {
            standardVocabularySeeder.seedGrade10Vocabulary();
        }

        List<VocabularyWord> allActiveWords = vocabularyWordRepository.findByGradeLevelAndActiveTrueOrderByWordNumberAsc(10);

        // Build distinct units
        Set<String> unitSet = new TreeSet<>((a, b) -> {
            int numA = extractFirstNumber(a);
            int numB = extractFirstNumber(b);
            if (numA != numB) return Integer.compare(numA, numB);
            return a.compareToIgnoreCase(b);
        });
        for (VocabularyWord w : allActiveWords) {
            if (w.getUnitName() != null) unitSet.add(w.getUnitName().trim().toLowerCase());
        }
        List<String> units = new ArrayList<>(unitSet);

        // Fetch student's progress
        List<StudentVocabularyProgress> progressList = studentVocabularyProgressRepository.findByStudentId(student.getId());
        Set<Long> masteredWordIds = progressList.stream()
                .filter(p -> Boolean.TRUE.equals(p.getMastered()))
                .map(StudentVocabularyProgress::getWordId)
                .collect(Collectors.toSet());

        Map<Long, Integer> practiceCounts = progressList.stream()
                .collect(Collectors.toMap(StudentVocabularyProgress::getWordId, StudentVocabularyProgress::getPracticeCount, (a, b) -> a));

        // Filter words for view
        String selectedUnit = (unit != null && !unit.trim().isEmpty()) ? unit.trim() : "ALL";
        String cleanSearch = (search != null) ? search.trim().toLowerCase() : "";

        List<VocabularyWord> displayWords = allActiveWords.stream()
                .filter(w -> matchesUnit(w.getUnitName(), selectedUnit))
                .filter(w -> {
                    if (cleanSearch.isEmpty()) return true;
                    boolean mEng = w.getEnglishWord() != null && w.getEnglishWord().toLowerCase().contains(cleanSearch);
                    boolean mClean = w.getCleanWord() != null && w.getCleanWord().toLowerCase().contains(cleanSearch);
                    boolean mKhm = w.getKhmerTranslation() != null && w.getKhmerTranslation().toLowerCase().contains(cleanSearch);
                    return mEng || mClean || mKhm;
                })
                .collect(Collectors.toList());

        // Stats
        long totalWords = allActiveWords.size();
        long totalMastered = masteredWordIds.size();
        int overallPercent = (int) Math.round(((double) totalMastered / Math.max(1, totalWords)) * 100.0);

        // Unit stats breakdown
        Map<String, int[]> unitProgress = new HashMap<>(); // [mastered, total]
        for (VocabularyWord w : allActiveWords) {
            String u = w.getUnitName() != null ? w.getUnitName().toLowerCase().trim() : "other";
            unitProgress.putIfAbsent(u, new int[]{0, 0});
            unitProgress.get(u)[1]++;
            if (masteredWordIds.contains(w.getId())) {
                unitProgress.get(u)[0]++;
            }
        }

        model.addAttribute("student", student);
        model.addAttribute("words", displayWords);
        model.addAttribute("units", units);
        model.addAttribute("selectedUnit", selectedUnit);
        model.addAttribute("searchQuery", search != null ? search : "");
        model.addAttribute("masteredWordIds", masteredWordIds);
        model.addAttribute("practiceCounts", practiceCounts);
        model.addAttribute("totalWords", totalWords);
        model.addAttribute("totalMastered", totalMastered);
        model.addAttribute("overallPercent", overallPercent);
        model.addAttribute("unitProgress", unitProgress);

        return "student-vocabulary";
    }

    // ==========================================
    // 3. REST APIS FOR TEACHER
    // ==========================================
    private boolean isAuthorizedForVocabulary() {
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) return false;
        String role = (String) session.getAttribute("ACCOUNT_ROLE");
        String username = (String) session.getAttribute("ACCOUNT_USERNAME");
        Boolean vocabPerm = (Boolean) session.getAttribute("VOCABULARY_PERMISSION");
        return Boolean.TRUE.equals(vocabPerm)
                || "ADMIN".equalsIgnoreCase(role)
                || Long.valueOf(97L).equals(accountId)
                || "samkimchheng".equalsIgnoreCase(username);
    }

    @PostMapping("/api/vocabulary/save")
    @ResponseBody
    public ResponseEntity<?> saveWord(@RequestBody Map<String, Object> payload) {
        if (!isAuthorizedForVocabulary()) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "គ្មានសិទ្ធិប្រើប្រាស់មុខងារវាក្យសព្ទទេ!"));
        }
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "សូមចូលប្រើប្រាស់ជាគ្រូបង្រៀនជាមុនសិន!"));
        }

        try {
            Long id = payload.get("id") != null && !payload.get("id").toString().isEmpty()
                    ? Long.parseLong(payload.get("id").toString()) : null;

            String english = (String) payload.get("englishWord");
            String khmer = (String) payload.get("khmerTranslation");
            String unit = (String) payload.get("unitName");
            String pos = (String) payload.get("partOfSpeech");
            String sentence = (String) payload.get("exampleSentence");
            Integer wordNum = payload.get("wordNumber") != null ? Integer.parseInt(payload.get("wordNumber").toString()) : null;

            if (english == null || english.trim().isEmpty() || khmer == null || khmer.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "សូមបំពេញពាក្យជាភាសាអង់គ្លេស និងការបកប្រែខ្មែរ!"));
            }

            VocabularyWord word;
            if (id != null) {
                word = vocabularyWordRepository.findById(id).orElse(new VocabularyWord());
            } else {
                word = new VocabularyWord();
                word.setWordNumber(wordNum != null ? wordNum : (int) (vocabularyWordRepository.count() + 1));
                word.setGradeLevel(10);
                word.setActive(true);
            }

            word.setEnglishWord(english.trim());
            
            // Clean word for TTS
            String clean = english.trim().replaceAll("\\s*\\(.*?\\)", "").trim();
            word.setCleanWord(clean);
            word.setPartOfSpeech(pos != null ? pos.trim() : "");
            word.setKhmerTranslation(khmer.trim());
            word.setUnitName(unit != null && !unit.trim().isEmpty() ? unit.trim().toLowerCase() : "unit 1");
            word.setExampleSentence(sentence != null ? sentence.trim() : null);
            word.setAccountId(accountId);

            vocabularyWordRepository.save(word);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "បានរក្សាទុកពាក្យ «" + word.getEnglishWord() + "» ដោយជោគជ័យ!",
                    "word", word
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "កំហុសបច្ចេកទេស៖ " + e.getMessage()));
        }
    }

    @PostMapping("/api/vocabulary/toggle-status")
    @ResponseBody
    public ResponseEntity<?> toggleWordStatus(@RequestParam Long id) {
        if (!isAuthorizedForVocabulary()) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "គ្មានសិទ្ធិប្រើប្រាស់មុខងារវាក្យសព្ទទេ!"));
        }
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "សូមចូលប្រើប្រាស់ជាមុនសិន!"));
        }

        Optional<VocabularyWord> opt = vocabularyWordRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "រកមិនឃើញពាក្យនេះទេ!"));
        }

        VocabularyWord word = opt.get();
        word.setActive(!Boolean.TRUE.equals(word.getActive()));
        vocabularyWordRepository.save(word);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "active", word.getActive(),
                "message", (word.getActive() ? "បានបើក" : "បានបិទ") + " ពាក្យ «" + word.getEnglishWord() + "» សម្រាប់សិស្សរៀន!"
        ));
    }

    @PostMapping("/api/vocabulary/bulk-toggle")
    @ResponseBody
    public ResponseEntity<?> bulkToggleByUnit(@RequestParam String unit, @RequestParam boolean active) {
        if (!isAuthorizedForVocabulary()) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "គ្មានសិទ្ធិប្រើប្រាស់មុខងារវាក្យសព្ទទេ!"));
        }
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "សូមចូលប្រើប្រាស់ជាមុនសិន!"));
        }

        List<VocabularyWord> all = vocabularyWordRepository.findByGradeLevelOrderByWordNumberAsc(10);
        int updated = 0;
        for (VocabularyWord w : all) {
            if (matchesUnit(w.getUnitName(), unit)) {
                w.setActive(active);
                updated++;
            }
        }
        vocabularyWordRepository.saveAll(all);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "updatedCount", updated,
                "message", (active ? "បានបើក" : "បានបិទ") + " ពាក្យទាំងអស់ក្នុង " + formatUnitDisplayName(unit) + " (" + updated + " ពាក្យ)!"
        ));
    }

    @PostMapping("/api/vocabulary/delete")
    @ResponseBody
    public ResponseEntity<?> deleteWord(@RequestParam Long id) {
        if (!isAuthorizedForVocabulary()) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "គ្មានសិទ្ធិប្រើប្រាស់មុខងារវាក្យសព្ទទេ!"));
        }
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "សូមចូលប្រើប្រាស់ជាមុនសិន!"));
        }

        Optional<VocabularyWord> opt = vocabularyWordRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "រកមិនឃើញពាក្យនេះទេ!"));
        }

        vocabularyWordRepository.delete(opt.get());
        return ResponseEntity.ok(Map.of("success", true, "message", "បានលុបពាក្យដោយជោគជ័យ!"));
    }

    @PostMapping("/api/vocabulary/reset-default")
    @ResponseBody
    public ResponseEntity<?> resetDefaultVocabulary() {
        if (!isAuthorizedForVocabulary()) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "គ្មានសិទ្ធិប្រើប្រាស់មុខងារវាក្យសព្ទទេ!"));
        }
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "សូមចូលប្រើប្រាស់ជាមុនសិន!"));
        }

        standardVocabularySeeder.seedGrade10Vocabulary();
        return ResponseEntity.ok(Map.of("success", true, "message", "បានកំណត់វាក្យសព្ទថ្នាក់ទី១០ ទៅជាស្តង់ដាដើម ៤៦០ ពាក្យដោយជោគជ័យ!"));
    }

    // ==========================================
    // 4. REST APIS FOR STUDENT LEARNING
    // ==========================================
    @PostMapping("/api/student-portal/vocabulary/toggle-mastered")
    @ResponseBody
    public ResponseEntity<?> toggleMastered(@RequestBody Map<String, Object> payload) {
        Student student = (Student) session.getAttribute("loggedInStudent");
        if (student == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "សូមចូលប្រើប្រាស់ជាសិស្សជាមុនសិន!"));
        }

        Long wordId = payload.get("wordId") != null ? Long.parseLong(payload.get("wordId").toString()) : null;
        Boolean mastered = payload.get("mastered") != null ? (Boolean) payload.get("mastered") : null;

        if (wordId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "សូមបញ្ជាក់លេខសម្គាល់ពាក្យ!"));
        }

        VocabularyWord word = vocabularyWordRepository.findById(wordId).orElse(null);
        if (word == null) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "រកមិនឃើញពាក្យ!"));
        }

        StudentVocabularyProgress prog = studentVocabularyProgressRepository
                .findByStudentIdAndWordId(student.getId(), wordId)
                .orElse(new StudentVocabularyProgress(student.getId(), wordId, word.getUnitName(), false));

        boolean newMastered = (mastered != null) ? mastered : !Boolean.TRUE.equals(prog.getMastered());
        prog.setMastered(newMastered);
        prog.setPracticeCount(prog.getPracticeCount() + 1);
        if (newMastered) {
            prog.setCorrectCount(prog.getCorrectCount() + 1);
        }
        prog.setLastPracticed(LocalDateTime.now());
        studentVocabularyProgressRepository.save(prog);

        long totalMastered = studentVocabularyProgressRepository.countByStudentIdAndMasteredTrue(student.getId());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "mastered", newMastered,
                "totalMastered", totalMastered,
                "message", newMastered ? "⭐ បានសម្គាល់ថាចងចាំច្បាស់!" : "បានដោះការសម្គាល់"
        ));
    }

    @PostMapping("/api/student-portal/vocabulary/record-quiz")
    @ResponseBody
    public ResponseEntity<?> recordQuiz(@RequestBody Map<String, Object> payload) {
        Student student = (Student) session.getAttribute("loggedInStudent");
        if (student == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "សូមចូលប្រើប្រាស់ជាសិស្សជាមុនសិន!"));
        }

        List<Map<String, Object>> results = (List<Map<String, Object>>) payload.get("results");
        if (results != null && !results.isEmpty()) {
            for (Map<String, Object> r : results) {
                Long wordId = r.get("wordId") != null ? Long.parseLong(r.get("wordId").toString()) : null;
                Boolean correct = (Boolean) r.get("correct");
                if (wordId == null) continue;

                VocabularyWord word = vocabularyWordRepository.findById(wordId).orElse(null);
                if (word == null) continue;

                StudentVocabularyProgress prog = studentVocabularyProgressRepository
                        .findByStudentIdAndWordId(student.getId(), wordId)
                        .orElse(new StudentVocabularyProgress(student.getId(), wordId, word.getUnitName(), false));

                prog.setPracticeCount(prog.getPracticeCount() + 1);
                if (Boolean.TRUE.equals(correct)) {
                    prog.setCorrectCount(prog.getCorrectCount() + 1);
                    // If answered correctly in quiz, automatically mark as mastered
                    prog.setMastered(true);
                }
                prog.setLastPracticed(LocalDateTime.now());
                studentVocabularyProgressRepository.save(prog);
            }
        }

        long totalMastered = studentVocabularyProgressRepository.countByStudentIdAndMasteredTrue(student.getId());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "totalMastered", totalMastered,
                "message", "បានកត់ត្រាលទ្ធផលហាត់រៀនដោយជោគជ័យ!"
        ));
    }

    // Dynamic quiz questions generator for a unit
    @GetMapping("/api/student-portal/vocabulary/quiz-data")
    @ResponseBody
    public ResponseEntity<?> getQuizData(@RequestParam(required = false) String unit) {
        List<VocabularyWord> allActive = vocabularyWordRepository.findByGradeLevelAndActiveTrueOrderByWordNumberAsc(10);
        if (allActive.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<VocabularyWord> targetWords = allActive.stream()
                .filter(w -> matchesUnit(w.getUnitName(), unit))
                .collect(Collectors.toList());

        if (targetWords.isEmpty()) {
            targetWords = allActive;
        }

        Collections.shuffle(targetWords);
        int limit = Math.min(10, targetWords.size());
        List<VocabularyWord> quizWords = targetWords.subList(0, limit);

        // All Khmer translations pool for distractors
        List<String> allTranslations = allActive.stream()
                .map(VocabularyWord::getKhmerTranslation)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<Map<String, Object>> questions = new ArrayList<>();
        Random rand = new Random();

        for (VocabularyWord w : quizWords) {
            Map<String, Object> q = new LinkedHashMap<>();
            q.put("wordId", w.getId());
            q.put("englishWord", w.getEnglishWord());
            q.put("cleanWord", w.getCleanWord());
            q.put("partOfSpeech", w.getPartOfSpeech());
            q.put("correctAnswer", w.getKhmerTranslation());
            q.put("unitName", formatUnitDisplayName(w.getUnitName()));

            // Pick 3 distractors
            Set<String> options = new LinkedHashSet<>();
            options.add(w.getKhmerTranslation());
            int attempts = 0;
            while (options.size() < 4 && attempts < 50 && allTranslations.size() >= 4) {
                String distractor = allTranslations.get(rand.nextInt(allTranslations.size()));
                options.add(distractor);
                attempts++;
            }
            List<String> shuffledOptions = new ArrayList<>(options);
            Collections.shuffle(shuffledOptions);
            q.put("options", shuffledOptions);

            questions.add(q);
        }

        return ResponseEntity.ok(questions);
    }

    // Helper: extracts first number in a string e.g. "unit 24" -> 24
    private static int extractFirstNumber(String text) {
        if (text == null) return 999;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d+").matcher(text);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group());
            } catch (Exception ignored) {}
        }
        return 999;
    }
}

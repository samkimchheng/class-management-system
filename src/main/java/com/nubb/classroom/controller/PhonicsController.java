package com.nubb.classroom.controller;

import com.nubb.classroom.config.StandardPhonicsSeeder;
import com.nubb.classroom.model.Account;
import com.nubb.classroom.model.PhonicsItem;
import com.nubb.classroom.model.Student;
import com.nubb.classroom.model.StudentPhonicsProgress;
import com.nubb.classroom.repository.AccountRepository;
import com.nubb.classroom.repository.PhonicsItemRepository;
import com.nubb.classroom.repository.StudentPhonicsProgressRepository;
import com.nubb.classroom.repository.StudentRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class PhonicsController {

    @Autowired
    private PhonicsItemRepository phonicsItemRepository;

    @Autowired
    private StudentPhonicsProgressRepository studentPhonicsProgressRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private StandardPhonicsSeeder standardPhonicsSeeder;

    @Autowired
    private HttpSession session;

    public static int normalizeLevel(Integer level) {
        if (level != null && level >= 1 && level <= 5) {
            return level;
        }
        return 1;
    }

    public static String getLevelTitleKhmer(int level) {
        return switch (level) {
            case 1 -> "កម្រិតទី១៖ អក្សរក្រម និងសូរសំឡេងមូលដ្ឋាន (Level 1: The Alphabet)";
            case 2 -> "កម្រិតទី២៖ ស្រៈសំឡេងខ្លី និងការប្រកប CVC (Level 2: Short Vowels)";
            case 3 -> "កម្រិតទី៣៖ ស្រៈសំឡេងវែង Magic E & Vowel Teams (Level 3: Long Vowels)";
            case 4 -> "កម្រិតទី៤៖ ព្យញ្ជនៈផ្សំ និងសូរពិសេស (Level 4: Consonant Blends & Digraphs)";
            case 5 -> "កម្រិតទី៥៖ ការផ្សំអក្សរកម្រិតខ្ពស់ និងពាក្យច្រើនព្យាង្គ (Level 5: Letter Combinations & Fluency)";
            default -> "កម្រិតទី១៖ អក្សរក្រម និងសូរសំឡេងមូលដ្ឋាន";
        };
    }

    public static String getLevelShortLabel(int level) {
        return switch (level) {
            case 1 -> "Level 1: Alphabet (A-Z)";
            case 2 -> "Level 2: Short Vowels";
            case 3 -> "Level 3: Long Vowels";
            case 4 -> "Level 4: Blends & Digraphs";
            case 5 -> "Level 5: Advanced Fluency";
            default -> "Level " + level;
        };
    }

    public static boolean isAuthorizedForPhonics(Account acc) {
        if (acc == null) return false;
        if ("ADMIN".equalsIgnoreCase(acc.getRole())
                || Long.valueOf(97L).equals(acc.getId())
                || "samkimchheng".equalsIgnoreCase(acc.getUsername())
                || Boolean.TRUE.equals(acc.getVocabularyPermission())) {
            return true;
        }
        String subj = acc.getSubject() != null ? acc.getSubject().toLowerCase() : "";
        return subj.contains("english") || subj.contains("អង់គ្លេស");
    }

    private boolean checkCurrentTeacherAuthorized() {
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) return false;
        Account acc = accountRepository.findById(accountId).orElse(null);
        boolean allowed = isAuthorizedForPhonics(acc);
        session.setAttribute("VOCABULARY_PERMISSION", allowed);
        return allowed;
    }

    private List<String> buildSortedUnits(List<PhonicsItem> items) {
        Set<String> unitSet = new LinkedHashSet<>();
        for (PhonicsItem item : items) {
            if (item.getUnitName() != null && !item.getUnitName().trim().isEmpty()) {
                unitSet.add(item.getUnitName().trim());
            }
        }
        return new ArrayList<>(unitSet);
    }

    public static boolean matchesUnit(String itemUnit, String targetUnit) {
        if (targetUnit == null || targetUnit.trim().isEmpty() || "ALL".equalsIgnoreCase(targetUnit.trim())) {
            return true;
        }
        if (itemUnit == null) return false;
        return itemUnit.trim().equalsIgnoreCase(targetUnit.trim());
    }

    // =========================================================================
    // 1. ADMIN & TEACHER MANAGEMENT + CLASS PROGRESS ANALYTICS VIEW (/phonics)
    // =========================================================================
    @GetMapping("/phonics")
    public String teacherPhonicsView(
            @RequestParam(required = false) Integer level,
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

        boolean allowed = isAuthorizedForPhonics(teacher);
        session.setAttribute("VOCABULARY_PERMISSION", allowed);
        if (!allowed) {
            return "redirect:/?accessDenied=phonics";
        }

        standardPhonicsSeeder.ensureAllLevelsSeeded();

        int selectedLevel = normalizeLevel(level);

        // Load all items across all 5 levels for accurate multi-level analytics
        List<PhonicsItem> allItems = phonicsItemRepository.findAllByOrderByPhonicsLevelAscItemNumberAsc();
        Map<Integer, Set<Long>> levelItemIdsMap = new HashMap<>();
        for (int lv = 1; lv <= 5; lv++) {
            levelItemIdsMap.put(lv, new HashSet<>());
        }
        Set<Long> allActiveItemIds = new HashSet<>();
        for (PhonicsItem it : allItems) {
            int lv = it.getPhonicsLevel() != null ? it.getPhonicsLevel() : 1;
            levelItemIdsMap.computeIfAbsent(lv, k -> new HashSet<>()).add(it.getId());
            if (Boolean.TRUE.equals(it.getActive())) {
                allActiveItemIds.add(it.getId());
            }
        }

        long level1Total = levelItemIdsMap.get(1).size();
        long level2Total = levelItemIdsMap.get(2).size();
        long level3Total = levelItemIdsMap.get(3).size();
        long level4Total = levelItemIdsMap.get(4).size();
        long level5Total = levelItemIdsMap.get(5).size();
        long allLevelsTotal = Math.max(1, allItems.size());

        List<PhonicsItem> levelItems = allItems.stream()
                .filter(it -> Objects.equals(it.getPhonicsLevel(), selectedLevel))
                .collect(Collectors.toList());

        List<String> units = buildSortedUnits(levelItems);
        Set<Long> currentLevelIds = levelItemIdsMap.getOrDefault(selectedLevel, Collections.emptySet());

        String selectedUnit = (unit != null && !unit.trim().isEmpty()) ? unit.trim() : "ALL";
        String cleanSearch = (search != null) ? search.trim().toLowerCase() : "";

        List<PhonicsItem> filteredItems = levelItems.stream()
                .filter(w -> matchesUnit(w.getUnitName(), selectedUnit))
                .filter(w -> {
                    if (cleanSearch.isEmpty()) return true;
                    boolean mWord = w.getEnglishWord() != null && w.getEnglishWord().toLowerCase().contains(cleanSearch);
                    boolean mPat = w.getSoundPattern() != null && w.getSoundPattern().toLowerCase().contains(cleanSearch);
                    boolean mKhm = w.getKhmerTranslation() != null && w.getKhmerTranslation().toLowerCase().contains(cleanSearch);
                    boolean mPho = w.getKhmerPhonetic() != null && w.getKhmerPhonetic().toLowerCase().contains(cleanSearch);
                    return mWord || mPat || mKhm || mPho;
                })
                .collect(Collectors.toList());

        long totalLevelWords = levelItems.size();
        long activeLevelWords = levelItems.stream().filter(w -> Boolean.TRUE.equals(w.getActive())).count();

        // ---------------------------------------------------------------------
        // Student & Class-by-Class Progress Analytics
        // ---------------------------------------------------------------------
        boolean isAdmin = "ADMIN".equalsIgnoreCase(teacher.getRole())
                || Long.valueOf(97L).equals(teacher.getId())
                || "samkimchheng".equalsIgnoreCase(teacher.getUsername());

        List<Student> allStudents;
        if (isAdmin) {
            allStudents = studentRepository.findAll();
        } else {
            allStudents = studentRepository.findByAccountId(accountId);
            if (allStudents.isEmpty()) {
                allStudents = studentRepository.findAll();
            }
        }

        // Exclude deleted/inactive if applicable
        allStudents = allStudents.stream()
                .filter(s -> s.getClassName() != null && !s.getClassName().trim().isEmpty())
                .sorted(Comparator.comparing(Student::getClassName).thenComparing(Student::getName))
                .collect(Collectors.toList());

        List<String> allClasses = allStudents.stream()
                .map(s -> s.getClassName().trim())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        String selectedClass = (className != null && !className.trim().isEmpty()) ? className.trim() : "ALL";

        // Bulk load all phonics progress records for these students
        List<Long> studentIds = allStudents.stream().map(Student::getId).collect(Collectors.toList());
        Map<Long, List<StudentPhonicsProgress>> progressByStudent = new HashMap<>();
        if (!studentIds.isEmpty()) {
            List<StudentPhonicsProgress> allProgs = studentPhonicsProgressRepository.findByStudentIdIn(studentIds);
            for (StudentPhonicsProgress p : allProgs) {
                progressByStudent.computeIfAbsent(p.getStudentId(), k -> new ArrayList<>()).add(p);
            }
        }

        // 1) Compute Class-by-Class Summary Table
        Map<String, List<Student>> studentsByClass = allStudents.stream()
                .collect(Collectors.groupingBy(s -> s.getClassName().trim(), LinkedHashMap::new, Collectors.toList()));

        List<Map<String, Object>> classSummaryList = new ArrayList<>();
        for (Map.Entry<String, List<Student>> entry : studentsByClass.entrySet()) {
            String clsName = entry.getKey();
            List<Student> clsStudents = entry.getValue();
            int stuCount = clsStudents.size();
            int activeCount = 0;
            double sumOverallPct = 0;
            double sumLevelPct = 0;
            long sumListening = 0, sumReading = 0, sumSpeaking = 0, sumWriting = 0;

            for (Student s : clsStudents) {
                List<StudentPhonicsProgress> pList = progressByStudent.getOrDefault(s.getId(), Collections.emptyList());
                long masteredAll = pList.stream().filter(p -> Boolean.TRUE.equals(p.getMastered())).count();
                long masteredLv = pList.stream()
                        .filter(p -> Boolean.TRUE.equals(p.getMastered()) && currentLevelIds.contains(p.getPhonicsItemId()))
                        .count();
                int prac = pList.stream().mapToInt(StudentPhonicsProgress::getPracticeCount).sum();
                if (prac > 0 || masteredAll > 0) activeCount++;

                sumOverallPct += ((double) masteredAll / allLevelsTotal) * 100.0;
                sumLevelPct += ((double) masteredLv / Math.max(1, totalLevelWords)) * 100.0;

                sumListening += pList.stream().filter(p -> Boolean.TRUE.equals(p.getListeningCompleted())).count();
                sumReading += pList.stream().filter(p -> Boolean.TRUE.equals(p.getReadingCompleted())).count();
                sumSpeaking += pList.stream().filter(p -> Boolean.TRUE.equals(p.getSpeakingCompleted())).count();
                sumWriting += pList.stream().filter(p -> Boolean.TRUE.equals(p.getWritingCompleted())).count();
            }

            long maxPossibleClassItems = Math.max(1L, (long) stuCount * allLevelsTotal);
            Map<String, Object> cRow = new LinkedHashMap<>();
            cRow.put("className", clsName);
            cRow.put("studentCount", stuCount);
            cRow.put("activeLearners", activeCount);
            cRow.put("avgOverallPercent", (int) Math.round(sumOverallPct / Math.max(1, stuCount)));
            cRow.put("avgLevelPercent", (int) Math.round(sumLevelPct / Math.max(1, stuCount)));
            cRow.put("listeningPercent", (int) Math.round(((double) sumListening / maxPossibleClassItems) * 100.0));
            cRow.put("readingPercent", (int) Math.round(((double) sumReading / maxPossibleClassItems) * 100.0));
            cRow.put("speakingPercent", (int) Math.round(((double) sumSpeaking / maxPossibleClassItems) * 100.0));
            cRow.put("writingPercent", (int) Math.round(((double) sumWriting / maxPossibleClassItems) * 100.0));
            classSummaryList.add(cRow);
        }

        // 2) Compute Individual Student Progress Rows (Filtered by selectedClass)
        List<Map<String, Object>> studentProgressList = new ArrayList<>();
        int activePracticingStudents = 0;
        long totalMasteredAcrossStudents = 0;

        for (Student s : allStudents) {
            if (!"ALL".equalsIgnoreCase(selectedClass) && !selectedClass.equalsIgnoreCase(s.getClassName().trim())) {
                continue;
            }
            List<StudentPhonicsProgress> pList = progressByStudent.getOrDefault(s.getId(), Collections.emptyList());

            long overallMastered = pList.stream().filter(p -> Boolean.TRUE.equals(p.getMastered())).count();
            long levelMastered = pList.stream()
                    .filter(p -> Boolean.TRUE.equals(p.getMastered()) && currentLevelIds.contains(p.getPhonicsItemId()))
                    .count();

            int totalPractice = pList.stream().mapToInt(StudentPhonicsProgress::getPracticeCount).sum();
            if (totalPractice > 0 || overallMastered > 0) activePracticingStudents++;
            totalMasteredAcrossStudents += overallMastered;

            int overallPercent = (int) Math.round(((double) overallMastered / allLevelsTotal) * 100.0);
            int levelPercent = (int) Math.round(((double) levelMastered / Math.max(1, totalLevelWords)) * 100.0);

            // Per-level % breakdown (L1 - L5)
            int[] lvPercents = new int[6];
            for (int lv = 1; lv <= 5; lv++) {
                Set<Long> lvIds = levelItemIdsMap.getOrDefault(lv, Collections.emptySet());
                long lvMast = pList.stream()
                        .filter(p -> Boolean.TRUE.equals(p.getMastered()) && lvIds.contains(p.getPhonicsItemId()))
                        .count();
                lvPercents[lv] = (int) Math.round(((double) lvMast / Math.max(1, lvIds.size())) * 100.0);
            }

            long listeningCount = pList.stream().filter(p -> Boolean.TRUE.equals(p.getListeningCompleted())).count();
            long readingCount = pList.stream().filter(p -> Boolean.TRUE.equals(p.getReadingCompleted())).count();
            long speakingCount = pList.stream().filter(p -> Boolean.TRUE.equals(p.getSpeakingCompleted())).count();
            long writingCount = pList.stream().filter(p -> Boolean.TRUE.equals(p.getWritingCompleted())).count();

            LocalDateTime lastActive = pList.stream()
                    .map(StudentPhonicsProgress::getLastPracticed)
                    .filter(Objects::nonNull)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("student", s);
            row.put("overallMastered", overallMastered);
            row.put("overallTotal", allLevelsTotal);
            row.put("overallPercent", overallPercent);
            row.put("levelMastered", levelMastered);
            row.put("levelTotal", totalLevelWords);
            row.put("levelPercent", levelPercent);
            row.put("l1Percent", lvPercents[1]);
            row.put("l2Percent", lvPercents[2]);
            row.put("l3Percent", lvPercents[3]);
            row.put("l4Percent", lvPercents[4]);
            row.put("l5Percent", lvPercents[5]);
            row.put("listeningCount", listeningCount);
            row.put("readingCount", readingCount);
            row.put("speakingCount", speakingCount);
            row.put("writingCount", writingCount);
            row.put("totalPractice", totalPractice);
            row.put("lastActive", lastActive);

            studentProgressList.add(row);
        }

        studentProgressList.sort((a, b) -> Long.compare((Long) b.get("overallMastered"), (Long) a.get("overallMastered")));

        model.addAttribute("teacherAccount", teacher);
        model.addAttribute("selectedLevel", selectedLevel);
        model.addAttribute("levelTitleKhmer", getLevelTitleKhmer(selectedLevel));
        model.addAttribute("levelShortLabel", getLevelShortLabel(selectedLevel));
        model.addAttribute("level1Total", level1Total);
        model.addAttribute("level2Total", level2Total);
        model.addAttribute("level3Total", level3Total);
        model.addAttribute("level4Total", level4Total);
        model.addAttribute("level5Total", level5Total);
        model.addAttribute("allLevelsTotal", allLevelsTotal);
        model.addAttribute("items", filteredItems);
        model.addAttribute("units", units);
        model.addAttribute("selectedUnit", selectedUnit);
        model.addAttribute("searchQuery", search != null ? search : "");
        model.addAttribute("totalLevelWords", totalLevelWords);
        model.addAttribute("activeLevelWords", activeLevelWords);
        model.addAttribute("classes", allClasses);
        model.addAttribute("selectedClass", selectedClass);
        model.addAttribute("classSummaryList", classSummaryList);
        model.addAttribute("studentProgressList", studentProgressList);
        model.addAttribute("totalStudentsCount", studentProgressList.size());
        model.addAttribute("activePracticingStudents", activePracticingStudents);
        model.addAttribute("totalMasteredAcrossStudents", totalMasteredAcrossStudents);

        return "phonics";
    }

    // =========================================================================
    // 2. STUDENT INTERACTIVE LEARNING PORTAL (/test/phonics)
    // =========================================================================
    @GetMapping("/test/phonics")
    public String studentPhonicsView(
            @RequestParam(required = false) Integer level,
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
            return "redirect:/test?redirectTo=/test/phonics";
        }

        if (student.getId() != null) {
            student = studentRepository.findById(student.getId()).orElse(student);
            session.setAttribute("loggedInStudent", student);
        }
        session.setAttribute("ACTIVE_USER_MODE", "STUDENT");

        standardPhonicsSeeder.ensureAllLevelsSeeded();

        int selectedLevel = normalizeLevel(level);

        List<PhonicsItem> allActiveItems = phonicsItemRepository.findByActiveTrueOrderByPhonicsLevelAscItemNumberAsc();
        Map<Integer, List<PhonicsItem>> itemsByLevel = new HashMap<>();
        for (int lv = 1; lv <= 5; lv++) {
            itemsByLevel.put(lv, new ArrayList<>());
        }
        for (PhonicsItem it : allActiveItems) {
            int lv = it.getPhonicsLevel() != null ? it.getPhonicsLevel() : 1;
            itemsByLevel.computeIfAbsent(lv, k -> new ArrayList<>()).add(it);
        }

        List<PhonicsItem> levelActiveItems = itemsByLevel.getOrDefault(selectedLevel, Collections.emptyList());
        List<String> units = buildSortedUnits(levelActiveItems);

        List<StudentPhonicsProgress> studentProgress = studentPhonicsProgressRepository.findByStudentId(student.getId());
        Map<Long, StudentPhonicsProgress> progressMap = studentProgress.stream()
                .collect(Collectors.toMap(StudentPhonicsProgress::getPhonicsItemId, p -> p, (a, b) -> a));

        Set<Long> masteredIds = studentProgress.stream()
                .filter(p -> Boolean.TRUE.equals(p.getMastered()))
                .map(StudentPhonicsProgress::getPhonicsItemId)
                .collect(Collectors.toSet());

        Set<Long> listeningIds = studentProgress.stream()
                .filter(p -> Boolean.TRUE.equals(p.getListeningCompleted()))
                .map(StudentPhonicsProgress::getPhonicsItemId)
                .collect(Collectors.toSet());

        Set<Long> readingIds = studentProgress.stream()
                .filter(p -> Boolean.TRUE.equals(p.getReadingCompleted()))
                .map(StudentPhonicsProgress::getPhonicsItemId)
                .collect(Collectors.toSet());

        Set<Long> speakingIds = studentProgress.stream()
                .filter(p -> Boolean.TRUE.equals(p.getSpeakingCompleted()))
                .map(StudentPhonicsProgress::getPhonicsItemId)
                .collect(Collectors.toSet());

        Set<Long> writingIds = studentProgress.stream()
                .filter(p -> Boolean.TRUE.equals(p.getWritingCompleted()))
                .map(StudentPhonicsProgress::getPhonicsItemId)
                .collect(Collectors.toSet());

        // Compute Level 1-5 progress percentages for student
        int[] levelPercents = new int[6];
        for (int lv = 1; lv <= 5; lv++) {
            List<PhonicsItem> lvList = itemsByLevel.getOrDefault(lv, Collections.emptyList());
            long mast = lvList.stream().filter(it -> masteredIds.contains(it.getId())).count();
            levelPercents[lv] = (int) Math.round(((double) mast / Math.max(1, lvList.size())) * 100.0);
        }

        long overallTotal = Math.max(1, allActiveItems.size());
        long overallMastered = allActiveItems.stream().filter(it -> masteredIds.contains(it.getId())).count();
        int overallPercent = (int) Math.round(((double) overallMastered / overallTotal) * 100.0);

        long levelTotal = Math.max(1, levelActiveItems.size());
        long levelMastered = levelActiveItems.stream().filter(it -> masteredIds.contains(it.getId())).count();
        int levelPercent = (int) Math.round(((double) levelMastered / levelTotal) * 100.0);

        String selectedUnit = (unit != null && !unit.trim().isEmpty()) ? unit.trim() : "ALL";
        String cleanSearch = (search != null) ? search.trim().toLowerCase() : "";

        List<PhonicsItem> displayItems = levelActiveItems.stream()
                .filter(w -> matchesUnit(w.getUnitName(), selectedUnit))
                .filter(w -> {
                    if (cleanSearch.isEmpty()) return true;
                    boolean mWord = w.getEnglishWord() != null && w.getEnglishWord().toLowerCase().contains(cleanSearch);
                    boolean mPat = w.getSoundPattern() != null && w.getSoundPattern().toLowerCase().contains(cleanSearch);
                    boolean mKhm = w.getKhmerTranslation() != null && w.getKhmerTranslation().toLowerCase().contains(cleanSearch);
                    return mWord || mPat || mKhm;
                })
                .collect(Collectors.toList());

        model.addAttribute("student", student);
        model.addAttribute("selectedLevel", selectedLevel);
        model.addAttribute("levelTitleKhmer", getLevelTitleKhmer(selectedLevel));
        model.addAttribute("levelShortLabel", getLevelShortLabel(selectedLevel));
        model.addAttribute("items", displayItems);
        model.addAttribute("units", units);
        model.addAttribute("selectedUnit", selectedUnit);
        model.addAttribute("searchQuery", search != null ? search : "");
        model.addAttribute("masteredIds", masteredIds);
        model.addAttribute("listeningIds", listeningIds);
        model.addAttribute("readingIds", readingIds);
        model.addAttribute("speakingIds", speakingIds);
        model.addAttribute("writingIds", writingIds);
        model.addAttribute("progressMap", progressMap);
        model.addAttribute("overallTotal", allActiveItems.size());
        model.addAttribute("overallMastered", overallMastered);
        model.addAttribute("overallPercent", overallPercent);
        model.addAttribute("levelTotal", levelActiveItems.size());
        model.addAttribute("levelMastered", levelMastered);
        model.addAttribute("levelPercent", levelPercent);
        model.addAttribute("level1Percent", levelPercents[1]);
        model.addAttribute("level2Percent", levelPercents[2]);
        model.addAttribute("level3Percent", levelPercents[3]);
        model.addAttribute("level4Percent", levelPercents[4]);
        model.addAttribute("level5Percent", levelPercents[5]);
        model.addAttribute("listeningDoneCount", listeningIds.size());
        model.addAttribute("readingDoneCount", readingIds.size());
        model.addAttribute("speakingDoneCount", speakingIds.size());
        model.addAttribute("writingDoneCount", writingIds.size());

        return "student-phonics";
    }

    // =========================================================================
    // 3. REST APIS FOR ADMIN & TEACHER MANAGEMENT
    // =========================================================================
    @PostMapping("/api/phonics/save")
    @ResponseBody
    public ResponseEntity<?> savePhonicsItem(@RequestBody Map<String, Object> payload) {
        if (!checkCurrentTeacherAuthorized()) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "គ្មានសិទ្ធិគ្រប់គ្រងមេរៀនប្រកបអង់គ្លេសទេ!"));
        }
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");

        try {
            Long id = payload.get("id") != null && !payload.get("id").toString().isEmpty()
                    ? Long.parseLong(payload.get("id").toString()) : null;

            Integer levelReq = payload.get("phonicsLevel") != null && !payload.get("phonicsLevel").toString().isEmpty()
                    ? Integer.parseInt(payload.get("phonicsLevel").toString()) : 1;
            int targetLevel = normalizeLevel(levelReq);

            String unitName = (String) payload.get("unitName");
            String soundPattern = (String) payload.get("soundPattern");
            String englishWord = (String) payload.get("englishWord");
            String ipaPhonetic = (String) payload.get("ipaPhonetic");
            String khmerPhonetic = (String) payload.get("khmerPhonetic");
            String blendingParts = (String) payload.get("blendingParts");
            String khmerTranslation = (String) payload.get("khmerTranslation");
            String emojiIcon = (String) payload.get("emojiIcon");
            String exampleSentence = (String) payload.get("exampleSentence");
            String exampleSentenceKhmer = (String) payload.get("exampleSentenceKhmer");

            if (englishWord == null || englishWord.trim().isEmpty() || khmerTranslation == null || khmerTranslation.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "សូមបំពេញពាក្យអង់គ្លេស និងការបកប្រែភាសាខ្មែរ!"));
            }

            PhonicsItem item;
            if (id != null) {
                item = phonicsItemRepository.findById(id).orElse(new PhonicsItem());
            } else {
                item = new PhonicsItem();
                item.setItemNumber((int) (phonicsItemRepository.countByPhonicsLevel(targetLevel) + 1));
                item.setActive(true);
            }

            item.setPhonicsLevel(targetLevel);
            item.setUnitName(unitName != null && !unitName.trim().isEmpty() ? unitName.trim() : "Unit 1");
            item.setSoundPattern(soundPattern != null && !soundPattern.trim().isEmpty() ? soundPattern.trim() : englishWord.substring(0, 1));
            item.setEnglishWord(englishWord.trim());
            item.setCleanWord(englishWord.trim().replaceAll("\\s*\\(.*?\\)", "").trim());
            item.setIpaPhonetic(ipaPhonetic != null ? ipaPhonetic.trim() : "");
            item.setKhmerPhonetic(khmerPhonetic != null ? khmerPhonetic.trim() : "");
            if (blendingParts == null || blendingParts.trim().isEmpty()) {
                // Default split by characters if not specified
                item.setBlendingParts(String.join("|", englishWord.trim().split("")));
            } else {
                item.setBlendingParts(blendingParts.trim());
            }
            item.setKhmerTranslation(khmerTranslation.trim());
            item.setEmojiIcon(emojiIcon != null && !emojiIcon.trim().isEmpty() ? emojiIcon.trim() : "🔤");
            item.setExampleSentence(exampleSentence != null ? exampleSentence.trim() : "");
            item.setExampleSentenceKhmer(exampleSentenceKhmer != null ? exampleSentenceKhmer.trim() : "");
            item.setAccountId(accountId);

            phonicsItemRepository.save(item);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "បានរក្សាទុកមេរៀនប្រកបពាក្យ «" + item.getEnglishWord() + "» ដោយជោគជ័យ!",
                    "item", item
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "កំហុសបច្ចេកទេស៖ " + e.getMessage()));
        }
    }

    @PostMapping("/api/phonics/toggle-status")
    @ResponseBody
    public ResponseEntity<?> togglePhonicsStatus(@RequestParam Long id) {
        if (!checkCurrentTeacherAuthorized()) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "គ្មានសិទ្ធិគ្រប់គ្រងទេ!"));
        }
        Optional<PhonicsItem> opt = phonicsItemRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "រកមិនឃើញពាក្យនេះទេ!"));
        }
        PhonicsItem item = opt.get();
        item.setActive(!Boolean.TRUE.equals(item.getActive()));
        phonicsItemRepository.save(item);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "active", item.getActive(),
                "message", (item.getActive() ? "បានបើក" : "បានបិទ") + " ពាក្យ «" + item.getEnglishWord() + "» សម្រាប់សិស្សរៀន!"
        ));
    }

    @PostMapping("/api/phonics/bulk-toggle")
    @ResponseBody
    public ResponseEntity<?> bulkTogglePhonics(
            @RequestParam(defaultValue = "1") Integer level,
            @RequestParam(required = false, defaultValue = "ALL") String unit,
            @RequestParam boolean active) {
        if (!checkCurrentTeacherAuthorized()) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "គ្មានសិទ្ធិគ្រប់គ្រងទេ!"));
        }
        int targetLevel = normalizeLevel(level);
        List<PhonicsItem> list = phonicsItemRepository.findByPhonicsLevelOrderByItemNumberAsc(targetLevel);
        int count = 0;
        for (PhonicsItem it : list) {
            if (matchesUnit(it.getUnitName(), unit)) {
                it.setActive(active);
                count++;
            }
        }
        phonicsItemRepository.saveAll(list);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "updatedCount", count,
                "message", (active ? "បានបើក" : "បានបិទ") + " មេរៀនចំនួន " + count + " ពាក្យដោយជោគជ័យ!"
        ));
    }

    @PostMapping("/api/phonics/delete")
    @ResponseBody
    public ResponseEntity<?> deletePhonicsItem(@RequestParam Long id) {
        if (!checkCurrentTeacherAuthorized()) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "គ្មានសិទ្ធិគ្រប់គ្រងទេ!"));
        }
        if (!phonicsItemRepository.existsById(id)) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "រកមិនឃើញពាក្យនេះទេ!"));
        }
        phonicsItemRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "បានលុបពាក្យប្រកបដោយជោគជ័យ!"));
    }

    @PostMapping("/api/phonics/reset-default")
    @ResponseBody
    public ResponseEntity<?> resetDefaultPhonics(@RequestParam(required = false, defaultValue = "0") Integer level) {
        if (!checkCurrentTeacherAuthorized()) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "គ្មានសិទ្ធិគ្រប់គ្រងទេ!"));
        }
        if (level != null && level >= 1 && level <= 5) {
            standardPhonicsSeeder.seedByLevel(level);
            return ResponseEntity.ok(Map.of("success", true, "message", "បានបញ្ចូលមេរៀនស្តង់ដារ Oxford Phonics កម្រិតទី " + level + " ឡើងវិញដោយជោគជ័យ!"));
        } else {
            standardPhonicsSeeder.ensureAllLevelsSeeded();
            return ResponseEntity.ok(Map.of("success", true, "message", "បានបញ្ចូលមេរៀនស្តង់ដារ Oxford Phonics ទាំង ៥ កម្រិតដោយជោគជ័យ!"));
        }
    }

    // =========================================================================
    // 4. REST APIS FOR STUDENT 4-SKILLS PROGRESS RECORDING
    // =========================================================================
    @PostMapping("/api/student-portal/phonics/record-skill")
    @ResponseBody
    public ResponseEntity<?> recordStudentSkill(@RequestBody Map<String, Object> payload) {
        Student student = (Student) session.getAttribute("loggedInStudent");
        if (student == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "សូមចូលគណនីសិស្សជាមុនសិន!"));
        }

        Long itemId = payload.get("itemId") != null ? Long.parseLong(payload.get("itemId").toString()) : null;
        String skill = payload.get("skill") != null ? payload.get("skill").toString().toUpperCase().trim() : "MASTERED";
        Boolean correct = payload.get("correct") != null ? Boolean.parseBoolean(payload.get("correct").toString()) : true;

        if (itemId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "សូមបញ្ជាក់លេខសម្គាល់ពាក្យ!"));
        }

        PhonicsItem item = phonicsItemRepository.findById(itemId).orElse(null);
        if (item == null) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "រកមិនឃើញមេរៀននេះទេ!"));
        }

        StudentPhonicsProgress prog = studentPhonicsProgressRepository
                .findByStudentIdAndPhonicsItemId(student.getId(), itemId)
                .orElse(new StudentPhonicsProgress(student.getId(), itemId, item.getPhonicsLevel(), item.getUnitName()));

        prog.setPhonicsLevel(item.getPhonicsLevel());
        prog.setUnitName(item.getUnitName());
        prog.setPracticeCount(prog.getPracticeCount() + 1);
        if (Boolean.TRUE.equals(correct)) {
            prog.setCorrectCount(prog.getCorrectCount() + 1);
        }

        switch (skill) {
            case "LISTENING" -> {
                if (correct) prog.setListeningCompleted(true);
            }
            case "READING" -> {
                if (correct) prog.setReadingCompleted(true);
            }
            case "SPEAKING" -> {
                if (correct) prog.setSpeakingCompleted(true);
            }
            case "WRITING" -> {
                if (correct) prog.setWritingCompleted(true);
            }
            case "TOGGLE_MASTERED" -> {
                boolean next = !Boolean.TRUE.equals(prog.getMastered());
                prog.setMastered(next);
                if (next) {
                    prog.setListeningCompleted(true);
                    prog.setReadingCompleted(true);
                }
            }
            default -> {
                if (correct) prog.setMastered(true);
            }
        }

        // Automatically mark mastered if at least 2 skills are completed or if explicitly answered correctly in practice
        if (prog.getCompletedSkillsCount() >= 2 || ("SPEAKING".equals(skill) && correct) || ("WRITING".equals(skill) && correct)) {
            prog.setMastered(true);
        }

        prog.setLastPracticed(LocalDateTime.now());
        studentPhonicsProgressRepository.save(prog);

        // Compute updated stats for student
        List<PhonicsItem> allActive = phonicsItemRepository.findByActiveTrueOrderByPhonicsLevelAscItemNumberAsc();
        List<StudentPhonicsProgress> allStuProg = studentPhonicsProgressRepository.findByStudentId(student.getId());
        Set<Long> masteredSet = allStuProg.stream()
                .filter(p -> Boolean.TRUE.equals(p.getMastered()))
                .map(StudentPhonicsProgress::getPhonicsItemId)
                .collect(Collectors.toSet());

        int currentLv = item.getPhonicsLevel() != null ? item.getPhonicsLevel() : 1;
        long lvTotal = allActive.stream().filter(it -> Objects.equals(it.getPhonicsLevel(), currentLv)).count();
        long lvMastered = allActive.stream().filter(it -> Objects.equals(it.getPhonicsLevel(), currentLv) && masteredSet.contains(it.getId())).count();
        int lvPercent = (int) Math.round(((double) lvMastered / Math.max(1, lvTotal)) * 100.0);

        long overallMastered = allActive.stream().filter(it -> masteredSet.contains(it.getId())).count();
        int overallPercent = (int) Math.round(((double) overallMastered / Math.max(1, allActive.size())) * 100.0);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("mastered", prog.getMastered());
        result.put("listeningCompleted", prog.getListeningCompleted());
        result.put("readingCompleted", prog.getReadingCompleted());
        result.put("speakingCompleted", prog.getSpeakingCompleted());
        result.put("writingCompleted", prog.getWritingCompleted());
        result.put("levelMastered", lvMastered);
        result.put("levelTotal", lvTotal);
        result.put("levelPercent", lvPercent);
        result.put("overallMastered", overallMastered);
        result.put("overallTotal", allActive.size());
        result.put("overallPercent", overallPercent);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/student-portal/phonics/practice-data")
    @ResponseBody
    public ResponseEntity<?> getPracticeData(
            @RequestParam(defaultValue = "1") Integer level,
            @RequestParam(required = false, defaultValue = "ALL") String unit) {

        int targetLevel = normalizeLevel(level);
        standardPhonicsSeeder.seedByLevel(targetLevel);

        List<PhonicsItem> levelActive = phonicsItemRepository.findByPhonicsLevelAndActiveTrueOrderByItemNumberAsc(targetLevel);
        if (levelActive.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<PhonicsItem> pool = levelActive.stream()
                .filter(w -> matchesUnit(w.getUnitName(), unit))
                .collect(Collectors.toList());
        if (pool.isEmpty()) {
            pool = new ArrayList<>(levelActive);
        }

        Collections.shuffle(pool);
        int limit = Math.min(12, pool.size());
        List<PhonicsItem> selected = pool.subList(0, limit);

        List<String> allWords = levelActive.stream().map(PhonicsItem::getEnglishWord).distinct().collect(Collectors.toList());
        List<String> allMeanings = levelActive.stream().map(PhonicsItem::getKhmerTranslation).distinct().collect(Collectors.toList());

        Random rand = new Random();
        List<Map<String, Object>> result = new ArrayList<>();

        for (PhonicsItem w : selected) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", w.getId());
            map.put("englishWord", w.getEnglishWord());
            map.put("cleanWord", w.getCleanWord());
            map.put("soundPattern", w.getSoundPattern());
            map.put("ipaPhonetic", w.getIpaPhonetic());
            map.put("khmerPhonetic", w.getKhmerPhonetic());
            map.put("blendingParts", w.getBlendingParts());
            map.put("khmerTranslation", w.getKhmerTranslation());
            map.put("emojiIcon", w.getEmojiIcon());
            map.put("exampleSentence", w.getExampleSentence());
            map.put("exampleSentenceKhmer", w.getExampleSentenceKhmer());

            // 4 English word options for Listening Quiz
            Set<String> wordOpts = new LinkedHashSet<>();
            wordOpts.add(w.getEnglishWord());
            int tries = 0;
            while (wordOpts.size() < 4 && tries < 40 && allWords.size() >= 4) {
                wordOpts.add(allWords.get(rand.nextInt(allWords.size())));
                tries++;
            }
            List<String> shuffledWords = new ArrayList<>(wordOpts);
            Collections.shuffle(shuffledWords);
            map.put("wordOptions", shuffledWords);

            // 4 Khmer meaning options for Reading Quiz
            Set<String> meaningOpts = new LinkedHashSet<>();
            meaningOpts.add(w.getKhmerTranslation());
            tries = 0;
            while (meaningOpts.size() < 4 && tries < 40 && allMeanings.size() >= 4) {
                meaningOpts.add(allMeanings.get(rand.nextInt(allMeanings.size())));
                tries++;
            }
            List<String> shuffledMeanings = new ArrayList<>(meaningOpts);
            Collections.shuffle(shuffledMeanings);
            map.put("meaningOptions", shuffledMeanings);

            result.add(map);
        }

        return ResponseEntity.ok(result);
    }

    // In-memory cache for clear Female English TTS audio (avoids repeated external fetches)
    private static final Map<String, byte[]> TTS_AUDIO_CACHE = new ConcurrentHashMap<>();

    /**
     * Streams clear, natural Female English TTS audio (MP3) for words, phonemes, and example sentences.
     */
    @GetMapping("/api/tts/english")
    @ResponseBody
    public ResponseEntity<byte[]> streamFemaleEnglishTts(
            @RequestParam("text") String text,
            @RequestParam(value = "slow", defaultValue = "false") boolean slow) {

        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String cleaned = normalizePhonicsTtsText(text.trim());
        if (cleaned.length() > 220) {
            cleaned = cleaned.substring(0, 220);
        }

        String cacheKey = (slow ? "SLOW:" : "NORM:") + cleaned.toLowerCase(Locale.ROOT);
        byte[] cachedBytes = TTS_AUDIO_CACHE.get(cacheKey);
        if (cachedBytes != null && cachedBytes.length > 0) {
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf("audio/mpeg"))
                    .cacheControl(CacheControl.maxAge(24, TimeUnit.HOURS).cachePublic())
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"tts.mp3\"")
                    .body(cachedBytes);
        }

        try {
            String encoded = URLEncoder.encode(cleaned, StandardCharsets.UTF_8);
            String speed = slow ? "0.28" : "0.85";
            String ttsUrl = "https://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&tl=en&ttsspeed=" + speed + "&q=" + encoded;

            HttpURLConnection conn = (HttpURLConnection) URI.create(ttsUrl).toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");
            conn.setRequestProperty("Referer", "https://translate.google.com/");

            int status = conn.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                try (InputStream in = conn.getInputStream()) {
                    byte[] audioBytes = in.readAllBytes();
                    if (audioBytes.length > 100) {
                        if (TTS_AUDIO_CACHE.size() > 1500) {
                            TTS_AUDIO_CACHE.clear();
                        }
                        TTS_AUDIO_CACHE.put(cacheKey, audioBytes);
                        return ResponseEntity.ok()
                                .contentType(MediaType.valueOf("audio/mpeg"))
                                .cacheControl(CacheControl.maxAge(24, TimeUnit.HOURS).cachePublic())
                                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"tts.mp3\"")
                                .body(audioBytes);
                    }
                }
            }
        } catch (Exception ignored) {
            // Client will automatically fall back to Web Speech API Female Voice
        }

        return ResponseEntity.noContent().build();
    }

    private static String normalizePhonicsTtsText(String raw) {
        String s = raw.trim();
        // Strip leading/trailing hyphens or slashes from sound patterns like "-at" or "/æt/"
        if (s.contains("/")) {
            s = s.replaceAll("/[^/]+/", "").trim();
        }
        if (s.startsWith("-") && s.length() > 1) {
            s = s.substring(1).trim();
        }
        if (s.endsWith("-") && s.length() > 1) {
            s = s.substring(0, s.length() - 1).trim();
        }
        String lower = s.toLowerCase(Locale.ROOT);
        switch (lower) {
            case "a_e": return "ay";
            case "i_e": return "eye";
            case "o_e": return "oh";
            case "u_e": return "you";
            case "e_e": return "ee";
            case "igh": return "eye";
            case "sh": return "shh";
            case "ch": return "chuh";
            case "th": return "thuh";
            case "ph": return "fff";
            case "wh": return "wuh";
            case "ck": return "kuh";
            case "qu": return "kwuh";
            case "kn": return "nuh";
            case "wr": return "ruh";
            case "ng": return "ing";
            case "nk": return "ink";
            case "tion": return "shun";
            case "ture": return "cher";
            case "le": return "ul";
            default:
                return s.replace("_", " ");
        }
    }
}

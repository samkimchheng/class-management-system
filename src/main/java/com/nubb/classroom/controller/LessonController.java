package com.nubb.classroom.controller;

import com.nubb.classroom.model.*;
import com.nubb.classroom.repository.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Controller
public class LessonController {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private LessonTranscriptRepository lessonTranscriptRepository;

    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private HttpSession session;

    @ModelAttribute("myClassrooms")
    public List<Classroom> getMyClassrooms() {
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId != null) {
            List<Classroom> list = new ArrayList<>();
            Set<String> seen = new LinkedHashSet<>();
            List<Classroom> dbClasses = classroomRepository.findByAccountId(accountId);
            if ((dbClasses == null || dbClasses.isEmpty()) && accountId != 98L) {
                dbClasses = classroomRepository.findByAccountId(97L);
            }
            if (dbClasses != null) {
                for (Classroom c : dbClasses) {
                    if (c.getClassName() != null && !c.getClassName().trim().isEmpty()) {
                        String norm = normalizeClass(c.getClassName());
                        if (seen.add(norm)) {
                            list.add(c);
                        }
                    }
                }
            }
            List<Student> dbStudents = studentRepository.findByAccountId(accountId);
            if (dbStudents != null) {
                for (Student s : dbStudents) {
                    if (s.getClassName() != null && !s.getClassName().trim().isEmpty()) {
                        String norm = normalizeClass(s.getClassName());
                        if (seen.add(norm)) {
                            Classroom extra = new Classroom();
                            extra.setAccountId(accountId);
                            extra.setClassName(s.getClassName().trim());
                            list.add(extra);
                        }
                    }
                }
            }
            return list;
        }
        return new ArrayList<>();
    }

    public static boolean isClassMatching(String lessonClass, String studentClass) {
        if (lessonClass == null || studentClass == null) return false;
        String lTrim = lessonClass.trim();
        String sTrim = studentClass.trim();
        if (lTrim.isEmpty() || sTrim.isEmpty()) return false;

        // 1. Universal class matching
        if (lTrim.equals("គ្រប់ថ្នាក់") || lTrim.equals("ថ្នាក់រៀនទាំងអស់") || lTrim.equalsIgnoreCase("ALL")) {
            return true;
        }

        return isExplicitClassMatching(lTrim, sTrim);
    }

    public static boolean isExplicitClassMatching(String lessonClass, String studentClass) {
        if (lessonClass == null || studentClass == null) return false;
        String lTrim = lessonClass.trim();
        String sTrim = studentClass.trim();
        if (lTrim.isEmpty() || sTrim.isEmpty()) return false;
        if (lTrim.equals("គ្រប់ថ្នាក់") || lTrim.equals("ថ្នាក់រៀនទាំងអស់") || lTrim.equalsIgnoreCase("ALL")) {
            return false;
        }

        String normStudent = normalizeClass(sTrim);
        if (normStudent.isEmpty()) return false;
        String[] parts = lTrim.split("[,;]+");
        for (String part : parts) {
            String normPart = normalizeClass(part.trim());
            if (!normPart.isEmpty() && normPart.equalsIgnoreCase(normStudent)) {
                return true;
            }
        }
        return false;
    }

    public static List<Lesson> resolveLessonsForStudent(LessonRepository lessonRepository, Student student) {
        if (student == null) return new ArrayList<>();
        Long studentAccountId = student.getAccountId();
        String studentClassName = student.getClassName();
        List<Lesson> allLessons = lessonRepository.findAll();
        List<Lesson> result = new ArrayList<>();

        for (Lesson l : allLessons) {
            boolean sameAccountPool;
            if (studentAccountId == null) {
                sameAccountPool = (l.getAccountId() == null || l.getAccountId() != 98L);
            } else if (studentAccountId == 98L) {
                sameAccountPool = Objects.equals(l.getAccountId(), 98L);
            } else if (studentAccountId == 1L || studentAccountId == 97L) {
                sameAccountPool = (l.getAccountId() == null || l.getAccountId() == 1L || l.getAccountId() == 97L);
            } else {
                sameAccountPool = Objects.equals(l.getAccountId(), studentAccountId);
            }

            if (studentClassName != null && !studentClassName.trim().isEmpty()) {
                if (sameAccountPool && isClassMatching(l.getClassName(), studentClassName)) {
                    result.add(l);
                } else if ((studentAccountId == null || studentAccountId != 98L)
                        && (l.getAccountId() == null || l.getAccountId() != 98L)
                        && isExplicitClassMatching(l.getClassName(), studentClassName)) {
                    result.add(l);
                }
            } else if (sameAccountPool) {
                result.add(l);
            }
        }

        result.sort(Comparator.comparing(Lesson::getId));
        return result;
    }

    private boolean isLessonVisibleToTeacher(Lesson l, Long accountId, List<Classroom> myClassrooms) {
        if (accountId == null) return false;
        if (accountId == 1L) return true;
        if (accountId == 98L) return Objects.equals(l.getAccountId(), 98L);
        if (Objects.equals(l.getAccountId(), accountId)) return true;
        if (accountId == 97L && (l.getAccountId() == null || l.getAccountId() == 1L || l.getAccountId() == 97L)) {
            return true;
        }
        if (l.getAccountId() == null || l.getAccountId() != 98L) {
            if (myClassrooms != null) {
                for (Classroom c : myClassrooms) {
                    if (c.getClassName() != null && isExplicitClassMatching(l.getClassName(), c.getClassName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static String normalizeClass(String cls) {
        if (cls == null) return "";
        return cls.trim()
                .replace("០", "0")
                .replace("១", "1")
                .replace("២", "2")
                .replace("៣", "3")
                .replace("៤", "4")
                .replace("៥", "5")
                .replace("៦", "6")
                .replace("៧", "7")
                .replace("៨", "8")
                .replace("៩", "9")
                .replace("ថ្នាក់ទី", "")
                .replace("ថ្នាក់", "")
                .replaceAll("\\s+", "")
                .toLowerCase();
    }

    @GetMapping("/lessons")
    public String viewLessons(
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String subjectName,
            HttpServletResponse response,
            Model model) {

        if (response != null) {
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
        }

        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        Student student = (Student) session.getAttribute("loggedInStudent");
        String activeMode = (String) session.getAttribute("ACTIVE_USER_MODE");
        boolean useStudentMode = (student != null) && !(accountId != null && "TEACHER".equals(activeMode));

        List<Lesson> lessons = new ArrayList<>();
        if (useStudentMode) {
            // Keep student entity fresh from database in case class or account was updated or re-created
            Student freshStudent = null;
            if (student.getId() != null) {
                freshStudent = studentRepository.findById(student.getId()).orElse(null);
            }
            if (freshStudent == null && student.getStudentCode() != null) {
                String cleanCode = student.getStudentCode().trim();
                freshStudent = studentRepository.findAll().stream()
                        .filter(s -> s.getStudentCode() != null && s.getStudentCode().trim().equalsIgnoreCase(cleanCode))
                        .max(Comparator.comparing(Student::getId, Comparator.nullsFirst(Comparator.naturalOrder())))
                        .orElse(null);
            }
            if (freshStudent != null) {
                student = freshStudent;
                session.setAttribute("loggedInStudent", student);
            }

            lessons = resolveLessonsForStudent(lessonRepository, student);
        } else if (accountId != null) {
            List<Classroom> myClasses = getMyClassrooms();
            List<Lesson> accountLessons = new ArrayList<>();
            for (Lesson l : lessonRepository.findAll()) {
                if (isLessonVisibleToTeacher(l, accountId, myClasses)) {
                    accountLessons.add(l);
                }
            }

            if (className != null && !className.isEmpty() && !"ថ្នាក់រៀនទាំងអស់".equals(className)) {
                for (Lesson l : accountLessons) {
                    if (isClassMatching(l.getClassName(), className)) {
                        if (subjectName != null && !subjectName.isEmpty() && !"មុខវិជ្ជាទាំងអស់".equals(subjectName)) {
                            if (com.nubb.classroom.WebController.isSameSubject(l.getSubjectName(), subjectName)) {
                                lessons.add(l);
                            }
                        } else {
                            lessons.add(l);
                        }
                    }
                }
            } else if (subjectName != null && !subjectName.isEmpty() && !"មុខវិជ្ជាទាំងអស់".equals(subjectName)) {
                for (Lesson l : accountLessons) {
                    if (com.nubb.classroom.WebController.isSameSubject(l.getSubjectName(), subjectName)) {
                        lessons.add(l);
                    }
                }
            } else {
                lessons = accountLessons;
            }
        }

        if (lessons != null && !lessons.isEmpty()) {
            lessons.sort(java.util.Comparator.comparing(Lesson::getId));
        }

        String userKey = useStudentMode ? ("STUDENT_" + student.getId()) : (accountId != null ? ("TEACHER_" + accountId) : null);
        Map<Long, Integer> progressMap = new HashMap<>();
        Map<Long, Boolean> completedMap = new HashMap<>();
        Map<Long, List<LessonTranscript>> transcriptsMap = new HashMap<>();
        int completedLessonsCount = 0;

        for (Lesson l : lessons) {
            Optional<LessonProgress> lp = lessonProgressRepository.findByLessonIdAndUserKey(l.getId(), userKey);
            if (lp.isPresent()) {
                progressMap.put(l.getId(), lp.get().getProgressPercentage());
                boolean done = lp.get().isCompleted();
                completedMap.put(l.getId(), done);
                if (done) completedLessonsCount++;
            } else {
                progressMap.put(l.getId(), 0);
                completedMap.put(l.getId(), false);
            }

            List<LessonTranscript> transcripts = lessonTranscriptRepository.findByLessonIdOrderByOrderIndexAsc(l.getId());
            transcriptsMap.put(l.getId(), transcripts);
        }

        int totalLessonsCount = lessons.size();
        boolean isAllCompleted = totalLessonsCount > 0 && completedLessonsCount >= totalLessonsCount;

        model.addAttribute("lessons", lessons);
        model.addAttribute("progressMap", progressMap);
        model.addAttribute("completedMap", completedMap);
        model.addAttribute("transcriptsMap", transcriptsMap);
        model.addAttribute("selectedClass", className);
        model.addAttribute("selectedSubject", subjectName);
        model.addAttribute("isStudent", useStudentMode);
        model.addAttribute("totalLessonsCount", totalLessonsCount);
        model.addAttribute("completedLessonsCount", completedLessonsCount);
        model.addAttribute("isAllCompleted", isAllCompleted);

        return "lessons";
    }

    @GetMapping("/api/lessons/{id}/transcripts")
    @ResponseBody
    public ResponseEntity<?> getLessonTranscripts(@PathVariable Long id) {
        List<LessonTranscript> transcripts = lessonTranscriptRepository.findByLessonIdOrderByOrderIndexAsc(id);
        return ResponseEntity.ok(transcripts);
    }

    @PostMapping("/api/lessons/save")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> saveLesson(
            @RequestParam(required = false) String id,
            @RequestParam String title,
            @RequestParam String className,
            @RequestParam String subjectName,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String khmerContent,
            @RequestParam(required = false) String audioUrl,
            @RequestParam(required = false) MultipartFile audioFile,
            @RequestParam(required = false) List<String> tsTitle,
            @RequestParam(required = false) List<String> tsContent,
            @RequestParam(required = false) List<String> tsKhmerContent,
            @RequestParam(required = false) List<String> tsAudioUrl,
            @RequestParam(required = false) List<MultipartFile> tsAudioFile) {

        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "សូមចូលប្រើប្រាស់ប្រព័ន្ធជាមុនសិន"));
        }

        Lesson lesson;
        if (id != null && !id.trim().isEmpty()) {
            try {
                Long lessonId = Long.parseLong(id.trim());
                lesson = lessonRepository.findById(lessonId).orElse(new Lesson());
            } catch (Exception e) {
                lesson = new Lesson();
                lesson.setAccountId(accountId);
            }
        } else {
            lesson = new Lesson();
            lesson.setAccountId(accountId);
        }

        if (lesson.getAccountId() == null) {
            lesson.setAccountId(accountId != null ? accountId : 97L);
        }

        lesson.setTitle(title);
        lesson.setClassName(className);
        lesson.setSubjectName(subjectName);
        lesson.setContent(content);
        lesson.setKhmerContent(khmerContent);

        // Handle Main MP3 File Upload if provided
        if (audioFile != null && !audioFile.isEmpty()) {
            String uploadedPath = saveMp3File(audioFile);
            if (uploadedPath != null) {
                lesson.setAudioUrl(uploadedPath);
            }
        } else if (audioUrl != null && !audioUrl.trim().isEmpty()) {
            lesson.setAudioUrl(audioUrl.trim());
        }

        lessonRepository.save(lesson);

        // Save & update multiple Transcripts
        if (tsTitle != null && !tsTitle.isEmpty()) {
            List<LessonTranscript> existingTs = lessonTranscriptRepository.findByLessonIdOrderByOrderIndexAsc(lesson.getId());
            lessonTranscriptRepository.deleteByLessonId(lesson.getId());

            for (int i = 0; i < tsTitle.size(); i++) {
                String tTitle = tsTitle.get(i);
                if (tTitle == null || tTitle.trim().isEmpty()) continue;

                String tContent = (tsContent != null && i < tsContent.size()) ? tsContent.get(i) : "";
                String tKhmerContent = (tsKhmerContent != null && i < tsKhmerContent.size()) ? tsKhmerContent.get(i) : "";
                String tAudioUrl = (tsAudioUrl != null && i < tsAudioUrl.size()) ? tsAudioUrl.get(i) : "";
                
                // Preserve existing audioUrl if not re-uploaded/changed
                if ((tAudioUrl == null || tAudioUrl.trim().isEmpty()) && i < existingTs.size()) {
                    tAudioUrl = existingTs.get(i).getAudioUrl();
                }

                // Check for new uploaded file for this transcript
                if (tsAudioFile != null && i < tsAudioFile.size()) {
                    MultipartFile f = tsAudioFile.get(i);
                    if (f != null && !f.isEmpty()) {
                        String uploaded = saveMp3File(f);
                        if (uploaded != null) {
                            tAudioUrl = uploaded;
                        }
                    }
                }

                LessonTranscript lt = new LessonTranscript(lesson.getId(), tTitle.trim(), tContent, tKhmerContent, tAudioUrl, i + 1);
                lessonTranscriptRepository.save(lt);
            }
        }

        lessonRepository.flush();
        return ResponseEntity.ok(Map.of("success", true, "message", "រក្សាទុកមេរៀន និង Transcripts ជោគជ័យ"));
    }

    @PostMapping("/api/lessons/delete/{id}")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> deleteLesson(@PathVariable Long id) {
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "សូមចូលប្រើប្រាស់ប្រព័ន្ធ"));
        }

        lessonProgressRepository.deleteByLessonId(id);
        lessonTranscriptRepository.deleteByLessonId(id);
        if (lessonRepository.existsById(id)) {
            lessonRepository.deleteById(id);
            lessonRepository.flush();
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/api/lessons/progress")
    @ResponseBody
    public ResponseEntity<?> updateProgress(
            @RequestParam Long lessonId,
            @RequestParam int progressPercentage) {

        String userKey = getUserKey();
        if (userKey == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        int percentage = Math.min(100, Math.max(0, progressPercentage));
        boolean isCompleted = percentage >= 100;

        Optional<LessonProgress> existing = lessonProgressRepository.findByLessonIdAndUserKey(lessonId, userKey);
        LessonProgress lp = existing.orElse(new LessonProgress(lessonId, userKey, percentage, isCompleted));

        if (percentage > lp.getProgressPercentage()) {
            lp.setProgressPercentage(percentage);
        }
        if (isCompleted) {
            lp.setCompleted(true);
        }
        lp.setUpdatedAt(java.time.LocalDateTime.now());

        lessonProgressRepository.save(lp);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "progressPercentage", lp.getProgressPercentage(),
                "completed", lp.isCompleted()
        ));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseBody
    public ResponseEntity<?> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        return ResponseEntity.status(400).body(Map.of("error", "ឯកសារ MP3 ធំពេក! សូមជ្រើសរើសឯកសារទំហំតូចជាង 100MB"));
    }

    private String saveMp3File(MultipartFile file) {
        try {
            String uploadDir = "./uploads/audio/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String filename = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + "_" + file.getOriginalFilename().replaceAll("[^a-zA-Z0-9.-]", "_");
            Path filePath = Paths.get(uploadDir + filename);
            Files.write(filePath, file.getBytes());
            return "/uploads/audio/" + filename;
        } catch (IOException e) {
            return null;
        }
    }

    private String getUserKey() {
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        Student student = (Student) session.getAttribute("loggedInStudent");
        String activeMode = (String) session.getAttribute("ACTIVE_USER_MODE");
        boolean useStudentMode = (student != null) && !(accountId != null && "TEACHER".equals(activeMode));
        if (useStudentMode) {
            return "STUDENT_" + student.getId();
        }
        if (accountId != null) {
            return "TEACHER_" + accountId;
        }
        return null;
    }
}

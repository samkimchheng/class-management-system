package com.nubb.classroom.controller;

import com.nubb.classroom.model.AdminSheetClass;
import com.nubb.classroom.model.AdminSheetStudent;
import com.nubb.classroom.model.Student;
import com.nubb.classroom.repository.AdminSheetClassRepository;
import com.nubb.classroom.repository.AdminSheetStudentRepository;
import com.nubb.classroom.repository.FeedbackRepository;
import com.nubb.classroom.repository.StudentRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AdminScoreSheetController {

    @Autowired
    private HttpSession session;

    @Autowired
    private AdminSheetClassRepository sheetClassRepository;

    @Autowired
    private AdminSheetStudentRepository sheetStudentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired(required = false)
    private FeedbackRepository feedbackRepository;

    private boolean isAdmin() {
        return "ADMIN".equals(session.getAttribute("ACCOUNT_ROLE"));
    }

    @GetMapping("/admin/score-sheets")
    public String adminScoreSheetsPage(Model model) {
        if (!isAdmin()) {
            return "redirect:/login";
        }
        long pendingFeedbacks = 0;
        if (feedbackRepository != null) {
            try {
                pendingFeedbacks = feedbackRepository.countByStatus("PENDING");
            } catch (Exception ignored) {}
        }
        model.addAttribute("pendingFeedbacks", pendingFeedbacks);
        return "admin/score-sheets";
    }

    @GetMapping("/admin/api/score-sheets/classes")
    @ResponseBody
    public ResponseEntity<?> listClasses() {
        if (!isAdmin()) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));

        List<AdminSheetClass> classes = sheetClassRepository.findAllByOrderByIdDesc();
        List<Map<String, Object>> classDtos = new ArrayList<>();
        for (AdminSheetClass c : classes) {
            long total = sheetStudentRepository.countBySheetClassId(c.getId());
            long female = sheetStudentRepository.countBySheetClassIdAndGender(c.getId(), "ស");
            Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("id", c.getId());
            dto.put("className", c.getClassName());
            dto.put("schoolName", c.getSchoolName());
            dto.put("academicYear", c.getAcademicYear());
            dto.put("totalStudents", total);
            dto.put("femaleStudents", female);
            classDtos.add(dto);
        }

        // Also provide distinct existing system class names for 1-click student import (read-only!)
        List<String> existingSystemClasses = studentRepository.findAll().stream()
                .map(Student::getClassName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "classes", classDtos,
                "existingSystemClasses", existingSystemClasses
        ));
    }

    @PostMapping("/admin/api/score-sheets/classes")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> saveClass(@RequestBody Map<String, Object> payload) {
        if (!isAdmin()) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));

        String className = String.valueOf(payload.getOrDefault("className", "")).trim();
        String schoolName = String.valueOf(payload.getOrDefault("schoolName", "វិទ្យាល័យហ៊ុនសែន១០មករា")).trim();
        String academicYear = String.valueOf(payload.getOrDefault("academicYear", "២០២៥-២០២៦")).trim();

        if (className.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "សូមបញ្ចូលឈ្មោះថ្នាក់រៀន"));
        }

        AdminSheetClass entity;
        if (payload.get("id") != null && !String.valueOf(payload.get("id")).trim().isEmpty()) {
            Long id = Long.parseLong(String.valueOf(payload.get("id")).trim());
            entity = sheetClassRepository.findById(id).orElse(new AdminSheetClass());
        } else {
            entity = new AdminSheetClass();
        }
        entity.setClassName(className);
        entity.setSchoolName(schoolName.isEmpty() ? "វិទ្យាល័យហ៊ុនសែន១០មករា" : schoolName);
        entity.setAcademicYear(academicYear.isEmpty() ? "២០២៥-២០២៦" : academicYear);
        sheetClassRepository.save(entity);

        return ResponseEntity.ok(Map.of("success", true, "sheetClass", entity));
    }

    @DeleteMapping("/admin/api/score-sheets/classes/{id}")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> deleteClass(@PathVariable Long id) {
        if (!isAdmin()) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        sheetStudentRepository.deleteBySheetClassId(id);
        sheetClassRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/admin/api/score-sheets/classes/{classId}/students")
    @ResponseBody
    public ResponseEntity<?> listStudents(@PathVariable Long classId) {
        if (!isAdmin()) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        AdminSheetClass cls = sheetClassRepository.findById(classId).orElse(null);
        if (cls == null) return ResponseEntity.notFound().build();

        List<AdminSheetStudent> students = sheetStudentRepository.findBySheetClassIdOrderByOrderNoAscIdAsc(classId);
        return ResponseEntity.ok(Map.of("sheetClass", cls, "students", students));
    }

    @PostMapping("/admin/api/score-sheets/classes/{classId}/students")
    @ResponseBody
    @Transactional
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> addStudents(@PathVariable Long classId, @RequestBody Map<String, Object> payload) {
        if (!isAdmin()) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        if (!sheetClassRepository.existsById(classId)) return ResponseEntity.notFound().build();

        List<AdminSheetStudent> existing = sheetStudentRepository.findBySheetClassIdOrderByOrderNoAscIdAsc(classId);
        int nextOrder = existing.size() + 1;
        int added = 0;

        if (payload.containsKey("students") && payload.get("students") instanceof List) {
            List<Map<String, Object>> list = (List<Map<String, Object>>) payload.get("students");
            for (Map<String, Object> row : list) {
                String name = String.valueOf(row.getOrDefault("fullName", "")).trim();
                String gender = String.valueOf(row.getOrDefault("gender", "ប")).trim();
                if (!name.isEmpty()) {
                    sheetStudentRepository.save(new AdminSheetStudent(classId, nextOrder++, name, gender));
                    added++;
                }
            }
        } else {
            String name = String.valueOf(payload.getOrDefault("fullName", "")).trim();
            String gender = String.valueOf(payload.getOrDefault("gender", "ប")).trim();
            if (name.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "សូមបញ្ចូលឈ្មោះសិស្ស"));
            }
            sheetStudentRepository.save(new AdminSheetStudent(classId, nextOrder, name, gender));
            added = 1;
        }

        List<AdminSheetStudent> updated = sheetStudentRepository.findBySheetClassIdOrderByOrderNoAscIdAsc(classId);
        return ResponseEntity.ok(Map.of("success", true, "added", added, "students", updated));
    }

    @PostMapping("/admin/api/score-sheets/classes/{classId}/import-existing")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> importFromExistingClass(@PathVariable Long classId, @RequestBody Map<String, Object> payload) {
        if (!isAdmin()) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        if (!sheetClassRepository.existsById(classId)) return ResponseEntity.notFound().build();

        String sourceClassName = String.valueOf(payload.getOrDefault("sourceClassName", "")).trim();
        if (sourceClassName.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "សូមជ្រើសរើសថ្នាក់ដែលចង់ទាញសិស្ស"));
        }

        List<Student> sourceStudents = studentRepository.findByClassName(sourceClassName);
        // Deduplicate by name in case multiple teachers have same className
        Map<String, Student> uniqueByName = new LinkedHashMap<>();
        for (Student s : sourceStudents) {
            if (s.getName() != null && !s.getName().trim().isEmpty()) {
                uniqueByName.putIfAbsent(s.getName().trim(), s);
            }
        }

        List<AdminSheetStudent> existing = sheetStudentRepository.findBySheetClassIdOrderByOrderNoAscIdAsc(classId);
        Set<String> existingNames = existing.stream().map(s -> s.getFullName().trim()).collect(Collectors.toSet());
        int nextOrder = existing.size() + 1;
        int imported = 0;

        for (Student s : uniqueByName.values()) {
            String nm = s.getName().trim();
            if (!existingNames.contains(nm)) {
                sheetStudentRepository.save(new AdminSheetStudent(classId, nextOrder++, nm, s.getGender()));
                imported++;
            }
        }

        List<AdminSheetStudent> updated = sheetStudentRepository.findBySheetClassIdOrderByOrderNoAscIdAsc(classId);
        return ResponseEntity.ok(Map.of("success", true, "imported", imported, "students", updated));
    }

    @PostMapping("/admin/api/score-sheets/classes/{classId}/sort")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> sortStudentsKhmer(@PathVariable Long classId) {
        if (!isAdmin()) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        List<AdminSheetStudent> students = sheetStudentRepository.findBySheetClassIdOrderByOrderNoAscIdAsc(classId);
        Collator khCollator = Collator.getInstance(Locale.forLanguageTag("km-KH"));
        students.sort((a, b) -> khCollator.compare(a.getFullName(), b.getFullName()));
        for (int i = 0; i < students.size(); i++) {
            students.get(i).setOrderNo(i + 1);
            sheetStudentRepository.save(students.get(i));
        }
        return ResponseEntity.ok(Map.of("success", true, "students", students));
    }

    @PutMapping("/admin/api/score-sheets/students/{studentId}")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> updateStudent(@PathVariable Long studentId, @RequestBody Map<String, Object> payload) {
        if (!isAdmin()) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        AdminSheetStudent st = sheetStudentRepository.findById(studentId).orElse(null);
        if (st == null) return ResponseEntity.notFound().build();

        if (payload.containsKey("fullName")) {
            String nm = String.valueOf(payload.get("fullName")).trim();
            if (!nm.isEmpty()) st.setFullName(nm);
        }
        if (payload.containsKey("gender")) {
            st.setGender(String.valueOf(payload.get("gender")));
        }
        if (payload.containsKey("orderNo")) {
            try { st.setOrderNo(Integer.parseInt(String.valueOf(payload.get("orderNo")))); } catch (Exception ignored) {}
        }
        sheetStudentRepository.save(st);
        return ResponseEntity.ok(Map.of("success", true, "student", st));
    }

    @DeleteMapping("/admin/api/score-sheets/students/{studentId}")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> deleteStudent(@PathVariable Long studentId) {
        if (!isAdmin()) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        AdminSheetStudent st = sheetStudentRepository.findById(studentId).orElse(null);
        if (st == null) return ResponseEntity.notFound().build();
        Long clsId = st.getSheetClassId();
        sheetStudentRepository.delete(st);
        // Re-index remaining order numbers cleanly
        List<AdminSheetStudent> remaining = sheetStudentRepository.findBySheetClassIdOrderByOrderNoAscIdAsc(clsId);
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setOrderNo(i + 1);
            sheetStudentRepository.save(remaining.get(i));
        }
        return ResponseEntity.ok(Map.of("success", true, "students", remaining));
    }

    @DeleteMapping("/admin/api/score-sheets/classes/{classId}/students")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> clearClassStudents(@PathVariable Long classId) {
        if (!isAdmin()) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        sheetStudentRepository.deleteBySheetClassId(classId);
        return ResponseEntity.ok(Map.of("success", true));
    }
}

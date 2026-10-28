package com.nubb.classroom.controller;

import com.nubb.classroom.model.Classroom;
import com.nubb.classroom.repository.ClassroomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/classrooms")
public class ClassroomApiController {

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private HttpSession session;

    @PostMapping("/add")
    public ResponseEntity<?> addClass(@RequestParam String className) {
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) return ResponseEntity.status(401).build();
        if (accountId == 1L) {
            accountId = 97L;
        }

        String trimmedName = className.trim();
        if (trimmedName.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "ឈ្មោះថ្នាក់មិនអាចទទេបានទេ"));
        }

        String academicYear = (String) session.getAttribute("CURRENT_ACADEMIC_YEAR");
        if (academicYear == null || academicYear.trim().isEmpty()) {
            academicYear = "២០២៦-២០២៧";
        } else {
            academicYear = academicYear.trim();
        }

        if (classroomRepository.existsByClassNameAndAccountIdAndAcademicYear(trimmedName, accountId, academicYear)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "ឈ្មោះថ្នាក់នេះមានរួចហើយក្នុងឆ្នាំសិក្សា " + academicYear));
        }

        Classroom c = new Classroom();
        c.setClassName(trimmedName);
        c.setAccountId(accountId);
        c.setAcademicYear(academicYear);
        classroomRepository.save(c);

        return ResponseEntity.ok(Map.of("success", true, "message", "បានបន្ថែមថ្នាក់រៀនដោយជោគជ័យ"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteClass(@PathVariable Long id) {
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) return ResponseEntity.status(401).build();

        Classroom c = classroomRepository.findById(id).orElse(null);
        if (c != null && (c.getAccountId().equals(accountId) || ((accountId == 1L || accountId == 97L) && (c.getAccountId() == 1L || c.getAccountId() == 97L)))) {
            classroomRepository.delete(c);
            return ResponseEntity.ok(Map.of("success", true));
        }
        return ResponseEntity.notFound().build();
    }
}

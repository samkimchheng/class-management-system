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
    public ResponseEntity<?> addClass(@RequestParam String className,
                                      @RequestParam(required = false) String track) {
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

        if (track != null && !track.trim().isEmpty() && !track.equalsIgnoreCase("AUTO")) {
            c.setTrack(track.trim());
        } else {
            String upper = trimmedName.toUpperCase();
            if (trimmedName.contains("សង្គម") || upper.contains("SOC")) {
                c.setTrack("វិទ្យាសាស្ត្រសង្គម");
            } else if (trimmedName.contains("វិទ្យា") || upper.contains("SCI")) {
                c.setTrack("វិទ្យាសាស្ត្រ");
            } else if (upper.startsWith("11") || upper.startsWith("១១") ||
                       upper.startsWith("12") || upper.startsWith("១២")) {
                c.setTrack("វិទ្យាសាស្ត្រ");
            } else {
                c.setTrack("ទូទៅ");
            }
        }

        classroomRepository.save(c);

        return ResponseEntity.ok(Map.of("success", true, "message", "បានបន្ថែមថ្នាក់រៀនដោយជោគជ័យ"));
    }

    @PostMapping("/update-track")
    public ResponseEntity<?> updateTrack(@RequestParam Long id, @RequestParam String track) {
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) return ResponseEntity.status(401).build();
        if (accountId == 1L) accountId = 97L;

        Classroom c = classroomRepository.findById(id).orElse(null);
        if (c != null && (c.getAccountId().equals(accountId) || (c.getAccountId() == 1L || c.getAccountId() == 97L))) {
            String cleanTrack = (track != null && !track.trim().isEmpty()) ? track.trim() : "ទូទៅ";
            c.setTrack(cleanTrack);
            classroomRepository.save(c);
            return ResponseEntity.ok(Map.of("success", true, "message", "បានផ្លាស់ប្តូរប្រភេទថ្នាក់ដោយជោគជ័យ"));
        }
        return ResponseEntity.notFound().build();
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

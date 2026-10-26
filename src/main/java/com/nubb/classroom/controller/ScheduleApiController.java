package com.nubb.classroom.controller;

import com.nubb.classroom.model.Schedule;
import com.nubb.classroom.repository.ScheduleRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleApiController {

    @Autowired
    private HttpSession session;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @GetMapping("/get")
    public ResponseEntity<List<Schedule>> getSchedule(@RequestParam(required = false) String className) {
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        // 1. Look for weekly schedule for this account
        List<Schedule> weekly = scheduleRepository.findByClassNameAndAccountId("WEEKLY", accountId);
        if (!weekly.isEmpty()) {
            return ResponseEntity.ok(weekly);
        }

        // 2. If className was specifically provided, look by that class
        if (className != null && !className.trim().isEmpty() && !className.equals("WEEKLY")) {
            List<Schedule> byClass = scheduleRepository.findByClassNameAndAccountId(className.trim(), accountId);
            if (!byClass.isEmpty()) {
                return ResponseEntity.ok(byClass);
            }
        }

        // 3. Fallback: If teacher previously saved a schedule under any classroom, load it
        List<Schedule> allAccountSchedules = scheduleRepository.findByAccountId(accountId);
        if (!allAccountSchedules.isEmpty()) {
            return ResponseEntity.ok(allAccountSchedules);
        }

        return ResponseEntity.ok(Collections.emptyList());
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveSchedule(@RequestBody SavePayload payload) {
        Long accountId = (Long) session.getAttribute("ACCOUNT_ID");
        if (accountId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        String targetClass = (payload.getClassName() != null && !payload.getClassName().trim().isEmpty())
                ? payload.getClassName().trim()
                : "WEEKLY";

        // Clean up previous schedule for this account to keep one overall weekly schedule
        List<Schedule> oldSchedule = scheduleRepository.findByClassNameAndAccountId(targetClass, accountId);
        if (!oldSchedule.isEmpty()) {
            scheduleRepository.deleteAll(oldSchedule);
        } else {
            List<Schedule> legacy = scheduleRepository.findByAccountId(accountId);
            if (!legacy.isEmpty()) {
                scheduleRepository.deleteAll(legacy);
            }
        }

        // Save new weekly schedule
        if (payload.getSubjects() != null) {
            for (int i = 0; i < payload.getSubjects().size(); i++) {
                Schedule s = new Schedule();
                s.setClassName(targetClass);
                s.setBlockIndex(i);
                s.setSubjectName(payload.getSubjects().get(i));
                s.setAccountId(accountId);
                scheduleRepository.save(s);
            }
        }

        return ResponseEntity.ok(Map.of("success", true));
    }

    public static class SavePayload {
        private String className;
        private List<String> subjects;

        public String getClassName() { return className; }
        public void setClassName(String className) { this.className = className; }
        public List<String> getSubjects() { return subjects; }
        public void setSubjects(List<String> subjects) { this.subjects = subjects; }
    }
}

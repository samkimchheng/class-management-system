package com.nubb.classroom.controller;

import com.nubb.classroom.model.Schedule;
import com.nubb.classroom.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleApiController {
    @org.springframework.beans.factory.annotation.Autowired
    private jakarta.servlet.http.HttpSession session;


    @Autowired
    private ScheduleRepository scheduleRepository;

    @GetMapping("/get")
    public ResponseEntity<List<Schedule>> getSchedule(@RequestParam String className) {
        return ResponseEntity.ok(scheduleRepository.findByClassNameAndAccountId(className, (Long) session.getAttribute("ACCOUNT_ID")));
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveSchedule(@RequestBody SavePayload payload) {
        // Delete old schedule for this class
        List<Schedule> oldSchedule = scheduleRepository.findByClassNameAndAccountId(payload.getClassName(), (Long) session.getAttribute("ACCOUNT_ID"));
        scheduleRepository.deleteAll(oldSchedule);

        // Save new schedule
        for (int i = 0; i < payload.getSubjects().size(); i++) {
            Schedule s = new Schedule();
            s.setClassName(payload.getClassName());
            s.setBlockIndex(i);
            s.setSubjectName(payload.getSubjects().get(i));
            s.setAccountId((Long) session.getAttribute("ACCOUNT_ID")); scheduleRepository.save(s);
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



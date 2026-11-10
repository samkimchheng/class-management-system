package com.nubb.classroom.controller;

import com.nubb.classroom.model.Account;
import com.nubb.classroom.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@RestController
public class DiagnosticApiController {

    @Autowired private AccountRepository accountRepository;
    @Autowired private ClassroomRepository classroomRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private LessonRepository lessonRepository;
    @Autowired private LeaveRequestRepository leaveRequestRepository;
    @Autowired private ExamRepository examRepository;
    @Autowired private ScoreRepository scoreRepository;
    @Autowired private DataSource dataSource;

    @GetMapping("/api/diagnostic")
    public Map<String, Object> diagnostic() {
        Map<String, Object> res = new LinkedHashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            res.put("dbProduct", conn.getMetaData().getDatabaseProductName());
            res.put("dbVersion", conn.getMetaData().getDatabaseProductVersion());
            res.put("dbStatus", "CONNECTED");
        } catch (Exception e) {
            res.put("dbError", e.toString());
        }

        check(res, "accountCount", () -> accountRepository.count());
        check(res, "samkimchheng", () -> accountRepository.findByUsername("samkimchheng").map(a -> a.getId() + ":" + a.getFullName() + ":" + a.getTeachingClasses()).orElse("NOT_FOUND"));
        check(res, "seungsomuny", () -> accountRepository.findByUsername("Teacher1001").map(a -> a.getId() + ":" + a.getFullName() + ":" + a.getTeachingClasses()).orElse("NOT_FOUND"));
        check(res, "totalStudentsInDb", () -> studentRepository.count());
        check(res, "sampleStudents", () -> studentRepository.findAll().stream().limit(3).map(s -> s.getId() + ":" + s.getName() + ":" + s.getClassName() + ":" + s.getAcademicYear() + ":acc=" + s.getAccountId()).collect(Collectors.toList()));
        check(res, "kimchhengStudentsCount", () -> studentRepository.findByAccountIdAndAcademicYear(97L, "២០២៦-២០២៧").size());
        check(res, "somunyStudentsCount", () -> studentRepository.findByAccountIdAndAcademicYear(33L, "២០២៦-២០២៧").size());
        check(res, "classroomsList", () -> classroomRepository.findAll().stream().map(c -> c.getAccountId() + ":" + c.getClassName() + ":" + c.getAcademicYear()).collect(Collectors.toList()));
        check(res, "kimchhengLessonsCount", () -> lessonRepository.findByAccountIdOrderByIdAsc(97L).size());
        check(res, "somunyLessonsCount", () -> lessonRepository.findByAccountIdOrderByIdAsc(33L).size());
        check(res, "somunyClassroomsCount", () -> classroomRepository.findByAccountIdAndAcademicYear(33L, "២០២៦-២០២៧").size());
        check(res, "examsCount", () -> examRepository.findByAccountId(97L).size());

        return res;
    }

    @GetMapping("/api/diagnostic/fix-sequences")
    public Map<String, Object> fixSequences() {
        Map<String, Object> res = new LinkedHashMap<>();
        try {
            com.nubb.classroom.config.DataInitializer.alignAllSequences(dataSource);
            res.put("status", "SUCCESS");
            res.put("message", "All database table sequences aligned successfully!");
        } catch (Exception e) {
            res.put("status", "ERROR");
            res.put("error", e.getMessage());
        }
        return res;
    }

    @GetMapping("/api/diagnostic/reseed-students")
    public Map<String, Object> reseedStudents() {
        Map<String, Object> res = new LinkedHashMap<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            int deleted = stmt.executeUpdate("DELETE FROM students WHERE id >= 101 AND id <= 500");
            try { stmt.executeUpdate("DELETE FROM students WHERE class_name = '7A' OR class_name LIKE '%7%'"); } catch (Exception ignored) {}
            res.put("deletedOld", deleted);

            ResourceDatabasePopulator pop = new ResourceDatabasePopulator();
            pop.setSqlScriptEncoding("UTF-8");
            pop.addScript(new ClassPathResource("students_insert.sql"));
            pop.setContinueOnError(true);
            pop.execute(dataSource);
            try { stmt.executeUpdate("DELETE FROM students WHERE class_name = '7A' OR class_name LIKE '%7%'"); } catch (Exception ignored) {}

            com.nubb.classroom.config.DataInitializer.alignTableSequence(conn, "students", "id");

            res.put("status", "SUCCESS");
            res.put("totalStudentsNow", studentRepository.count());
            res.put("kimchhengStudents", studentRepository.findByAccountIdAndAcademicYear(97L, "២០២៦-២០២៧").size());
            res.put("somunyStudents", studentRepository.findByAccountIdAndAcademicYear(33L, "២០២៦-២០២៧").size());
        } catch (Exception e) {
            res.put("status", "ERROR");
            res.put("error", e.getMessage());
        }
        return res;
    }

    private void check(Map<String, Object> res, String name, Callable<?> callable) {
        try {
            res.put(name, callable.call());
        } catch (Exception e) {
            res.put(name + "_ERROR", e.getMessage() != null ? e.getMessage() : e.toString());
        }
    }
}

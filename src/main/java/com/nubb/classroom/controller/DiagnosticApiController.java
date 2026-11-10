package com.nubb.classroom.controller;

import com.nubb.classroom.model.Account;
import com.nubb.classroom.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

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
        check(res, "samkimchheng", () -> accountRepository.findByUsername("samkimchheng").map(Account::getFullName).orElse("NOT_FOUND"));
        check(res, "myClassroomsCount", () -> classroomRepository.findByAccountIdAndAcademicYear(97L, "២០២៦-២០២៧").size());
        check(res, "studentsCount", () -> studentRepository.findByAccountIdAndAcademicYear(97L, "២០២៦-២០२៧").size());
        check(res, "recentAbsentsCount", () -> attendanceRepository.findByDateAndPresentFalseAndStudentAccountId(LocalDate.now(), 97L).size());
        check(res, "lessonsCount", () -> lessonRepository.findByAccountIdOrderByIdAsc(97L).size());
        check(res, "leaveRequestsCount", () -> leaveRequestRepository.findAllByOrderByCreatedAtDesc().size());
        check(res, "examsCount", () -> examRepository.findByAccountId(97L).size());
        check(res, "scoresCount", () -> scoreRepository.findByAccountId(97L).size());

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

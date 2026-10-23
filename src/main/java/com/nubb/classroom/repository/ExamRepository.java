package com.nubb.classroom.repository;

import com.nubb.classroom.model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    Optional<Exam> findByMonthAndClassNameAndSubjectNameAndAccountId(String month, String className, String subjectName, Long accountId);
    java.util.List<Exam> findByMonthAndClassNameAndAccountId(String month, String className, Long accountId);
    java.util.List<com.nubb.classroom.model.Exam> findByAccountId(Long accountId);
}



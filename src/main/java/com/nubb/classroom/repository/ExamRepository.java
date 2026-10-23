package com.nubb.classroom.repository;

import com.nubb.classroom.model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamRepository extends JpaRepository<Exam, Long> {
    Optional<Exam> findByIdAndAccountId(Long id, Long accountId);
    List<Exam> findByAccountId(Long accountId);
    List<Exam> findByMonthAndAccountId(String month, Long accountId);
    List<Exam> findByMonthAndSubjectNameAndAccountId(String month, String subjectName, Long accountId);
    Optional<Exam> findByMonthAndClassNameAndSubjectNameAndAccountId(String month, String className, String subjectName, Long accountId);
    List<Exam> findByMonthAndClassNameAndAccountId(String month, String className, Long accountId);
}

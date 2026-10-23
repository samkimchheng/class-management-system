package com.nubb.classroom.repository;

import com.nubb.classroom.model.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findByMonthAndClassNameAndSubjectNameAndAccountId(String month, String className, String subjectName, Long accountId);
    Optional<Score> findByStudentIdAndMonthAndClassNameAndSubjectNameAndAccountId(Long studentId, String month, String className, String subjectName, Long accountId);
    List<Score> findByMonth(String month);
    List<Score> findByClassName(String className);
    java.util.List<com.nubb.classroom.model.Score> findByMonthAndAccountId(String month, Long accountId);
    java.util.Optional<com.nubb.classroom.model.Score> findByStudentIdAndMonthAndAccountId(Long studentId, String month, Long accountId);
    java.util.List<com.nubb.classroom.model.Score> findByAccountId(Long accountId);
    List<Score> findByMonthAndClassNameAndAccountId(String month, String className, Long accountId);
    List<Score> findByStudentIdAndMonthAndClassNameAndAccountId(Long studentId, String month, String className, Long accountId);
    @org.springframework.transaction.annotation.Transactional
    void deleteByStudentId(Long studentId);
}




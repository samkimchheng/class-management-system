package com.nubb.classroom.repository;

import com.nubb.classroom.model.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ScoreRepository extends JpaRepository<Score, Long> {
    @Query("SELECT s FROM Score s WHERE s.month = :month AND s.className = :className AND s.subjectName = :subjectName AND (:accountId IS NULL OR s.accountId = :accountId OR s.accountId = 97 OR s.accountId = 1 OR s.accountId IS NULL)")
    List<Score> findByMonthAndClassNameAndSubjectNameAndAccountId(@Param("month") String month, @Param("className") String className, @Param("subjectName") String subjectName, @Param("accountId") Long accountId);

    @Query("SELECT s FROM Score s WHERE s.month = :month AND s.className = :className AND s.subjectName = :subjectName AND (:academicYear IS NULL OR s.academicYear = :academicYear OR (:academicYear = '២០២៦-២០២៧' AND (s.academicYear IS NULL OR s.academicYear = ''))) AND (:accountId IS NULL OR s.accountId = :accountId OR s.accountId = 97 OR s.accountId = 1 OR s.accountId IS NULL)")
    List<Score> findByMonthAndClassNameAndSubjectNameAndAccountIdAndAcademicYear(
            @Param("month") String month, 
            @Param("className") String className, 
            @Param("subjectName") String subjectName, 
            @Param("accountId") Long accountId,
            @Param("academicYear") String academicYear);

    @Query("SELECT s FROM Score s WHERE s.studentId = :studentId AND s.month = :month AND s.className = :className AND s.subjectName = :subjectName AND (:accountId IS NULL OR s.accountId = :accountId OR s.accountId = 97 OR s.accountId = 1 OR s.accountId IS NULL)")
    Optional<Score> findByStudentIdAndMonthAndClassNameAndSubjectNameAndAccountId(@Param("studentId") Long studentId, @Param("month") String month, @Param("className") String className, @Param("subjectName") String subjectName, @Param("accountId") Long accountId);

    List<Score> findByMonth(String month);
    List<Score> findByClassName(String className);

    @Query("SELECT s FROM Score s WHERE s.month = :month AND (:accountId IS NULL OR s.accountId = :accountId OR s.accountId = 97 OR s.accountId = 1 OR s.accountId IS NULL)")
    List<Score> findByMonthAndAccountId(@Param("month") String month, @Param("accountId") Long accountId);

    @Query("SELECT s FROM Score s WHERE s.studentId = :studentId AND s.month = :month AND (:accountId IS NULL OR s.accountId = :accountId OR s.accountId = 97 OR s.accountId = 1 OR s.accountId IS NULL)")
    Optional<Score> findByStudentIdAndMonthAndAccountId(@Param("studentId") Long studentId, @Param("month") String month, @Param("accountId") Long accountId);

    @Query("SELECT s FROM Score s WHERE (:accountId IS NULL OR s.accountId = :accountId OR s.accountId = 97 OR s.accountId = 1 OR s.accountId IS NULL)")
    List<Score> findByAccountId(@Param("accountId") Long accountId);

    @Query("SELECT s FROM Score s WHERE s.month = :month AND s.className = :className AND (:accountId IS NULL OR s.accountId = :accountId OR s.accountId = 97 OR s.accountId = 1 OR s.accountId IS NULL)")
    List<Score> findByMonthAndClassNameAndAccountId(@Param("month") String month, @Param("className") String className, @Param("accountId") Long accountId);

    @Query("SELECT s FROM Score s WHERE s.month = :month AND s.className = :className AND (:academicYear IS NULL OR s.academicYear = :academicYear OR (:academicYear = '២០២៦-២០២៧' AND (s.academicYear IS NULL OR s.academicYear = ''))) AND (:accountId IS NULL OR s.accountId = :accountId OR s.accountId = 97 OR s.accountId = 1 OR s.accountId IS NULL)")
    List<Score> findByMonthAndClassNameAndAccountIdAndAcademicYear(
            @Param("month") String month, 
            @Param("className") String className, 
            @Param("accountId") Long accountId,
            @Param("academicYear") String academicYear);

    @Query("SELECT s FROM Score s WHERE s.studentId = :studentId AND s.month = :month AND s.className = :className AND (:accountId IS NULL OR s.accountId = :accountId OR s.accountId = 97 OR s.accountId = 1 OR s.accountId IS NULL)")
    List<Score> findByStudentIdAndMonthAndClassNameAndAccountId(@Param("studentId") Long studentId, @Param("month") String month, @Param("className") String className, @Param("accountId") Long accountId);

    @org.springframework.transaction.annotation.Transactional
    void deleteByStudentId(Long studentId);

    @Query("SELECT s FROM Score s WHERE s.studentId = :studentId AND s.month = :month AND s.className = :className AND s.subjectName = :subjectName AND (:academicYear IS NULL OR s.academicYear = :academicYear OR (:academicYear = '២០២៦-២០២៧' AND (s.academicYear IS NULL OR s.academicYear = ''))) AND (:accountId IS NULL OR s.accountId = :accountId OR s.accountId = 97 OR s.accountId = 1 OR s.accountId IS NULL)")
    Optional<Score> findByStudentIdAndMonthAndClassNameAndSubjectNameAndAccountIdAndAcademicYear(
            @Param("studentId") Long studentId, 
            @Param("month") String month, 
            @Param("className") String className, 
            @Param("subjectName") String subjectName, 
            @Param("accountId") Long accountId,
            @Param("academicYear") String academicYear);

    @Query("SELECT s FROM Score s WHERE s.month = :month AND (:academicYear IS NULL OR s.academicYear = :academicYear OR (:academicYear = '២០២៦-២០២៧' AND (s.academicYear IS NULL OR s.academicYear = ''))) AND (:accountId IS NULL OR s.accountId = :accountId OR s.accountId = 97 OR s.accountId = 1 OR s.accountId IS NULL)")
    List<Score> findByMonthAndAccountIdAndAcademicYear(
            @Param("month") String month, 
            @Param("accountId") Long accountId,
            @Param("academicYear") String academicYear);

    @Query("SELECT s FROM Score s WHERE s.studentId = :studentId AND s.month = :month AND s.className = :className AND (:academicYear IS NULL OR s.academicYear = :academicYear OR (:academicYear = '២០២៦-២០២៧' AND (s.academicYear IS NULL OR s.academicYear = ''))) AND (:accountId IS NULL OR s.accountId = :accountId OR s.accountId = 97 OR s.accountId = 1 OR s.accountId IS NULL)")
    List<Score> findByStudentIdAndMonthAndClassNameAndAccountIdAndAcademicYear(
            @Param("studentId") Long studentId, 
            @Param("month") String month, 
            @Param("className") String className, 
            @Param("accountId") Long accountId,
            @Param("academicYear") String academicYear);

    @Query("SELECT s FROM Score s WHERE s.studentId = :studentId AND s.month = :month AND (:academicYear IS NULL OR s.academicYear = :academicYear OR (:academicYear = '២០២៦-២០២៧' AND (s.academicYear IS NULL OR s.academicYear = ''))) AND (:accountId IS NULL OR s.accountId = :accountId OR s.accountId = 97 OR s.accountId = 1 OR s.accountId IS NULL)")
    List<Score> findByStudentIdAndMonthAndAccountIdAndAcademicYear(
            @Param("studentId") Long studentId, 
            @Param("month") String month, 
            @Param("accountId") Long accountId,
            @Param("academicYear") String academicYear);

    @Query("SELECT s FROM Score s WHERE s.studentId = :studentId AND s.month = :month AND s.subjectName = :subjectName AND (:academicYear IS NULL OR s.academicYear = :academicYear OR (:academicYear = '២០២៦-២០២៧' AND (s.academicYear IS NULL OR s.academicYear = ''))) AND (:accountId IS NULL OR s.accountId = :accountId OR s.accountId = 97 OR s.accountId = 1 OR s.accountId IS NULL)")
    List<Score> findByStudentIdAndMonthAndSubjectNameAndAccountIdAndAcademicYear(
            @Param("studentId") Long studentId, 
            @Param("month") String month, 
            @Param("subjectName") String subjectName, 
            @Param("accountId") Long accountId,
            @Param("academicYear") String academicYear);

    @Query("SELECT s FROM Score s WHERE s.month = :month AND s.subjectName = :subjectName AND (:academicYear IS NULL OR s.academicYear = :academicYear OR (:academicYear = '២០២៦-២០២៧' AND (s.academicYear IS NULL OR s.academicYear = ''))) AND (:accountId IS NULL OR s.accountId = :accountId OR s.accountId = 97 OR s.accountId = 1 OR s.accountId IS NULL)")
    List<Score> findByMonthAndSubjectNameAndAccountIdAndAcademicYear(
            @Param("month") String month, 
            @Param("subjectName") String subjectName, 
            @Param("accountId") Long accountId,
            @Param("academicYear") String academicYear);

    @Query("SELECT DISTINCT s.subjectName FROM Score s WHERE (:accountId IS NULL OR s.accountId = :accountId OR s.accountId = 97 OR s.accountId = 1 OR s.accountId IS NULL) AND s.subjectName IS NOT NULL AND s.subjectName != ''")
    List<String> findDistinctSubjectNamesByAccountId(@Param("accountId") Long accountId);
}

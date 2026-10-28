package com.nubb.classroom.repository;

import com.nubb.classroom.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentCode(String studentCode);
    List<Student> findByClassName(String className);

    @Query("SELECT s FROM Student s WHERE (:accountId IS NULL OR (:accountId IN (1, 97) AND (s.accountId IN (1, 97) OR s.accountId IS NULL)) OR s.accountId = :accountId)")
    List<Student> findByAccountId(@Param("accountId") Long accountId);

    @Query("SELECT s FROM Student s WHERE s.className = :className AND (:accountId IS NULL OR (:accountId IN (1, 97) AND (s.accountId IN (1, 97) OR s.accountId IS NULL)) OR s.accountId = :accountId)")
    List<Student> findByClassNameAndAccountId(@Param("className") String className, @Param("accountId") Long accountId);

    @Query("SELECT s FROM Student s WHERE (:academicYear IS NULL OR s.academicYear = :academicYear OR (:academicYear = '២០២៦-២០២៧' AND (s.academicYear IS NULL OR s.academicYear = ''))) AND (:accountId IS NULL OR (:accountId IN (1, 97) AND (s.accountId IN (1, 97) OR s.accountId IS NULL)) OR s.accountId = :accountId)")
    List<Student> findByAccountIdAndAcademicYear(@Param("accountId") Long accountId, @Param("academicYear") String academicYear);

    @Query("SELECT s FROM Student s WHERE s.className = :className AND (:academicYear IS NULL OR s.academicYear = :academicYear OR (:academicYear = '២០២៦-២០២៧' AND (s.academicYear IS NULL OR s.academicYear = ''))) AND (:accountId IS NULL OR (:accountId IN (1, 97) AND (s.accountId IN (1, 97) OR s.accountId IS NULL)) OR s.accountId = :accountId)")
    List<Student> findByClassNameAndAccountIdAndAcademicYear(@Param("className") String className, @Param("accountId") Long accountId, @Param("academicYear") String academicYear);
}

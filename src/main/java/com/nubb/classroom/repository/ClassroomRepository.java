package com.nubb.classroom.repository;

import com.nubb.classroom.model.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    @Query("SELECT c FROM Classroom c WHERE (:accountId IS NULL OR (:accountId IN (1, 97) AND (c.accountId IN (1, 97) OR c.accountId IS NULL)) OR c.accountId = :accountId)")
    List<Classroom> findByAccountId(@Param("accountId") Long accountId);

    @Query("SELECT c FROM Classroom c WHERE (:academicYear IS NULL OR c.academicYear = :academicYear OR (:academicYear = '២០២៦-២០២៧' AND (c.academicYear IS NULL OR c.academicYear = ''))) AND (:accountId IS NULL OR (:accountId IN (1, 97) AND (c.accountId IN (1, 97) OR c.accountId IS NULL)) OR c.accountId = :accountId)")
    List<Classroom> findByAccountIdAndAcademicYear(@Param("accountId") Long accountId, @Param("academicYear") String academicYear);

    @Query("SELECT COUNT(c) > 0 FROM Classroom c WHERE c.className = :className AND ((:accountId IN (1, 97) AND (c.accountId IN (1, 97) OR c.accountId IS NULL)) OR c.accountId = :accountId)")
    boolean existsByClassNameAndAccountId(@Param("className") String className, @Param("accountId") Long accountId);

    @Query("SELECT COUNT(c) > 0 FROM Classroom c WHERE c.className = :className AND ((:accountId IN (1, 97) AND (c.accountId IN (1, 97) OR c.accountId IS NULL)) OR c.accountId = :accountId) AND (:academicYear IS NULL OR c.academicYear = :academicYear OR (:academicYear = '២០២៦-២០២៧' AND (c.academicYear IS NULL OR c.academicYear = '')))")
    boolean existsByClassNameAndAccountIdAndAcademicYear(@Param("className") String className, @Param("accountId") Long accountId, @Param("academicYear") String academicYear);
}

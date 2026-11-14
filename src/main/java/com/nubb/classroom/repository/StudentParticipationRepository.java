package com.nubb.classroom.repository;

import com.nubb.classroom.model.Student;
import com.nubb.classroom.model.StudentParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentParticipationRepository extends JpaRepository<StudentParticipation, Long> {

    List<StudentParticipation> findByAccountIdAndAcademicYearAndClassNameOrderByCreatedAtDesc(Long accountId, String academicYear, String className);

    List<StudentParticipation> findByAccountIdAndAcademicYearOrderByCreatedAtDesc(Long accountId, String academicYear);

    List<StudentParticipation> findByStudentIdAndAcademicYear(Long studentId, String academicYear);

    long countByStudentIdAndAcademicYear(Long studentId, String academicYear);

    void deleteByStudent(Student student);

    void deleteByAccountIdAndAcademicYearAndClassName(Long accountId, String academicYear, String className);

    @Query("SELECT sp.student.id, COUNT(sp) FROM StudentParticipation sp WHERE sp.accountId = :accountId AND sp.academicYear = :academicYear AND sp.className = :className GROUP BY sp.student.id")
    List<Object[]> countParticipationByClass(@Param("accountId") Long accountId, @Param("academicYear") String academicYear, @Param("className") String className);

    @Query("SELECT sp.student.id, COUNT(sp) FROM StudentParticipation sp WHERE sp.accountId = :accountId AND sp.academicYear = :academicYear GROUP BY sp.student.id")
    List<Object[]> countParticipationByAccountAndYear(@Param("accountId") Long accountId, @Param("academicYear") String academicYear);
}

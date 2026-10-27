package com.nubb.classroom.repository;

import com.nubb.classroom.model.Attendance;
import com.nubb.classroom.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByStudentIdAndDateBetween(Long studentId, LocalDate start, LocalDate end);
    List<Attendance> findByDateBetween(LocalDate start, LocalDate end);
    List<Attendance> findByStudentInAndDateBetween(List<Student> students, LocalDate start, LocalDate end);
    List<Attendance> findByStudentIn(List<Student> students);
    
    List<Attendance> findByDateAndPresentFalse(LocalDate date);
    List<Attendance> findByDateAndPresentFalseAndStudentClassName(LocalDate date, String className);
    List<Attendance> findByDateAndPresentTrue(LocalDate date);
    List<Attendance> findByDateAndPresentTrueAndStudentClassName(LocalDate date, String className);

    @Transactional
    void deleteByStudent(Student student);

    @Transactional
    @Modifying
    @Query("DELETE FROM Attendance a WHERE a.student IN :students AND a.date >= :start AND a.date <= :end")
    void deleteByStudentInAndDateBetween(@Param("students") List<Student> students, @Param("start") LocalDate start, @Param("end") LocalDate end);
}

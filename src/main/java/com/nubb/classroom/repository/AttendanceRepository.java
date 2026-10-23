package com.nubb.classroom.repository;

import com.nubb.classroom.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByStudentIdAndDateBetween(Long studentId, LocalDate start, LocalDate end);
    List<Attendance> findByDateBetween(LocalDate start, LocalDate end);
    
    List<Attendance> findByDateAndPresentFalse(LocalDate date);
    List<Attendance> findByDateAndPresentFalseAndStudentClassName(LocalDate date, String className);
    List<Attendance> findByDateAndPresentTrue(LocalDate date);
    List<Attendance> findByDateAndPresentTrueAndStudentClassName(LocalDate date, String className);
    @org.springframework.transaction.annotation.Transactional
    void deleteByStudent(com.nubb.classroom.model.Student student);
}

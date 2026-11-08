package com.nubb.classroom.repository;

import com.nubb.classroom.model.LeaveRequest;
import com.nubb.classroom.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<LeaveRequest> findAllByOrderByCreatedAtDesc();

    @Transactional
    @Modifying
    @Query("DELETE FROM LeaveRequest lr WHERE lr.student = :student")
    void deleteByStudent(@Param("student") Student student);
}

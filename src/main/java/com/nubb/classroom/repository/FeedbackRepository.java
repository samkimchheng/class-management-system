package com.nubb.classroom.repository;

import com.nubb.classroom.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findAllByOrderByCreatedAtDesc();

    List<Feedback> findByTeacherAccountIdOrderByCreatedAtDesc(Long teacherAccountId);

    List<Feedback> findByStatusOrderByCreatedAtDesc(String status);

    long countByStatus(String status);
}

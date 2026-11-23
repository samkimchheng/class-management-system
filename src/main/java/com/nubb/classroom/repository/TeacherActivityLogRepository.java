package com.nubb.classroom.repository;

import com.nubb.classroom.model.TeacherActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherActivityLogRepository extends JpaRepository<TeacherActivityLog, Long> {
    Optional<TeacherActivityLog> findByAccountIdAndActivityDateAndFeatureKey(Long accountId, LocalDate activityDate, String featureKey);
    List<TeacherActivityLog> findByActivityDateOrderByLastUsedAtDesc(LocalDate activityDate);
    List<TeacherActivityLog> findByActivityDateBetweenOrderByLastUsedAtDesc(LocalDate startDate, LocalDate endDate);
    List<TeacherActivityLog> findAllByOrderByLastUsedAtDesc();
}

package com.nubb.classroom.repository;

import com.nubb.classroom.model.DailyTeachingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyTeachingLogRepository extends JpaRepository<DailyTeachingLog, Long> {

    List<DailyTeachingLog> findByAccountIdAndLogDateOrderBySlotIndexAsc(Long accountId, LocalDate logDate);

    List<DailyTeachingLog> findByAccountIdAndLogDateBetweenOrderByLogDateAscSlotIndexAsc(Long accountId, LocalDate start, LocalDate end);

    Optional<DailyTeachingLog> findByAccountIdAndLogDateAndSlotIndex(Long accountId, LocalDate logDate, Integer slotIndex);

    List<DailyTeachingLog> findByAccountIdAndAcademicYearOrderByLogDateDescSlotIndexAsc(Long accountId, String academicYear);

    List<DailyTeachingLog> findByAccountIdOrderByLogDateDescSlotIndexAsc(Long accountId);

    void deleteByAccountIdAndLogDate(Long accountId, LocalDate logDate);
}

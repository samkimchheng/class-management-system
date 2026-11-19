package com.nubb.classroom.repository;

import com.nubb.classroom.model.HomeroomScore;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HomeroomScoreRepository extends JpaRepository<HomeroomScore, Long> {
    List<HomeroomScore> findByAccountIdAndClassNameAndAcademicYearAndPeriod(
            Long accountId, String className, String academicYear, String period);
    List<HomeroomScore> findByAccountIdAndClassNameAndAcademicYear(
            Long accountId, String className, String academicYear);
    Optional<HomeroomScore> findByAccountIdAndStudentIdAndClassNameAndAcademicYearAndPeriodAndSubjectId(
            Long accountId, Long studentId, String className, String academicYear, String period, Long subjectId);
    void deleteBySubjectId(Long subjectId);
}

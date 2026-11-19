package com.nubb.classroom.repository;

import com.nubb.classroom.model.HomeroomSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HomeroomSubjectRepository extends JpaRepository<HomeroomSubject, Long> {
    List<HomeroomSubject> findByAccountIdAndClassNameAndAcademicYearOrderByDisplayOrderAscIdAsc(
            Long accountId, String className, String academicYear);
    List<HomeroomSubject> findByAccountIdAndClassNameOrderByDisplayOrderAscIdAsc(
            Long accountId, String className);
    void deleteByAccountIdAndClassNameAndAcademicYear(Long accountId, String className, String academicYear);
}

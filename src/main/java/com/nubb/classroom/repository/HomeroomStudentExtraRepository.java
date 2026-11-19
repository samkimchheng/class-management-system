package com.nubb.classroom.repository;

import com.nubb.classroom.model.HomeroomStudentExtra;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HomeroomStudentExtraRepository extends JpaRepository<HomeroomStudentExtra, Long> {
    List<HomeroomStudentExtra> findByAccountIdAndClassName(Long accountId, String className);
    List<HomeroomStudentExtra> findByAccountId(Long accountId);
    Optional<HomeroomStudentExtra> findByAccountIdAndStudentId(Long accountId, Long studentId);
}

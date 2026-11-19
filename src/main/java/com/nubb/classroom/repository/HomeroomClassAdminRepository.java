package com.nubb.classroom.repository;

import com.nubb.classroom.model.HomeroomClassAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface HomeroomClassAdminRepository extends JpaRepository<HomeroomClassAdmin, Long> {
    Optional<HomeroomClassAdmin> findByAccountIdAndClassNameAndAcademicYear(
            Long accountId, String className, String academicYear);
}

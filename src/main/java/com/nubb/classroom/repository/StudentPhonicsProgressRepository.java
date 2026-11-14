package com.nubb.classroom.repository;

import com.nubb.classroom.model.StudentPhonicsProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentPhonicsProgressRepository extends JpaRepository<StudentPhonicsProgress, Long> {
    List<StudentPhonicsProgress> findByStudentId(Long studentId);
    List<StudentPhonicsProgress> findByStudentIdIn(Collection<Long> studentIds);
    Optional<StudentPhonicsProgress> findByStudentIdAndPhonicsItemId(Long studentId, Long phonicsItemId);

    @Transactional
    void deleteByStudentId(Long studentId);
}

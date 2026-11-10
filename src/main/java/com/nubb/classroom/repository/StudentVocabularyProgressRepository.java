package com.nubb.classroom.repository;

import com.nubb.classroom.model.StudentVocabularyProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentVocabularyProgressRepository extends JpaRepository<StudentVocabularyProgress, Long> {

    List<StudentVocabularyProgress> findByStudentId(Long studentId);

    Optional<StudentVocabularyProgress> findByStudentIdAndWordId(Long studentId, Long wordId);

    List<StudentVocabularyProgress> findByStudentIdAndMasteredTrue(Long studentId);

    long countByStudentIdAndMasteredTrue(Long studentId);

    long countByStudentId(Long studentId);

    void deleteByStudentId(Long studentId);
}

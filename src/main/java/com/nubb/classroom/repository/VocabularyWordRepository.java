package com.nubb.classroom.repository;

import com.nubb.classroom.model.VocabularyWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VocabularyWordRepository extends JpaRepository<VocabularyWord, Long> {

    List<VocabularyWord> findByGradeLevelOrderByWordNumberAsc(Integer gradeLevel);

    List<VocabularyWord> findByGradeLevelAndActiveTrueOrderByWordNumberAsc(Integer gradeLevel);

    Optional<VocabularyWord> findByWordNumber(Integer wordNumber);

    long countByGradeLevel(Integer gradeLevel);

    List<VocabularyWord> findByGradeLevelAndUnitNameIgnoreCase(Integer gradeLevel, String unitName);
}

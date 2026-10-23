package com.nubb.classroom.repository;

import com.nubb.classroom.model.ScoreRule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ScoreRuleRepository extends JpaRepository<ScoreRule, Long> {
    Optional<ScoreRule> findByAccountIdAndClassNameAndSubjectName(Long accountId, String className, String subjectName);
}
package com.nubb.classroom.repository;

import com.nubb.classroom.model.ScoreRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ScoreRuleRepository extends JpaRepository<ScoreRule, Long> {
    @Query("SELECT r FROM ScoreRule r WHERE r.className = :className AND r.subjectName = :subjectName AND (:accountId IS NULL OR r.accountId = :accountId)")
    List<ScoreRule> findRules(@Param("className") String className, @Param("subjectName") String subjectName, @Param("accountId") Long accountId);

    Optional<ScoreRule> findByAccountIdAndClassNameAndSubjectName(Long accountId, String className, String subjectName);
    List<ScoreRule> findByClassNameAndSubjectName(String className, String subjectName);
}

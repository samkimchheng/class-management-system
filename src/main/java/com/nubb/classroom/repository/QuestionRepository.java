package com.nubb.classroom.repository;

import com.nubb.classroom.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByExamId(Long examId);
    java.util.List<com.nubb.classroom.model.Question> findByExamIdAndAccountId(Long examId, Long accountId);
}


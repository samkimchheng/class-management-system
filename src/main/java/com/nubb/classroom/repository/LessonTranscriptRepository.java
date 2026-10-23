package com.nubb.classroom.repository;

import com.nubb.classroom.model.LessonTranscript;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonTranscriptRepository extends JpaRepository<LessonTranscript, Long> {
    List<LessonTranscript> findByLessonIdOrderByOrderIndexAsc(Long lessonId);
    void deleteByLessonId(Long lessonId);
}

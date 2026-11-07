package com.nubb.classroom.repository;

import com.nubb.classroom.model.LessonTranscript;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface LessonTranscriptRepository extends JpaRepository<LessonTranscript, Long> {
    List<LessonTranscript> findByLessonIdOrderByOrderIndexAsc(Long lessonId);

    @Modifying
    @Transactional
    @Query("DELETE FROM LessonTranscript lt WHERE lt.lessonId = :lessonId")
    void deleteByLessonId(@Param("lessonId") Long lessonId);
}

package com.nubb.classroom.repository;

import com.nubb.classroom.model.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    Optional<LessonProgress> findByLessonIdAndUserKey(Long lessonId, String userKey);
    List<LessonProgress> findByUserKey(String userKey);
}

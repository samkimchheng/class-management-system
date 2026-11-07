package com.nubb.classroom.repository;

import com.nubb.classroom.model.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    Optional<LessonProgress> findByLessonIdAndUserKey(Long lessonId, String userKey);
    List<LessonProgress> findByUserKey(String userKey);

    @Modifying
    @Transactional
    @Query("DELETE FROM LessonProgress lp WHERE lp.lessonId = :lessonId")
    void deleteByLessonId(@Param("lessonId") Long lessonId);
}

package com.nubb.classroom.repository;

import com.nubb.classroom.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByAccountId(Long accountId);

    @Query("SELECT l FROM Lesson l WHERE l.accountId = :accountId AND (l.className = :className OR l.className = 'គ្រប់ថ្នាក់' OR l.className = 'ថ្នាក់រៀនទាំងអស់')")
    List<Lesson> findByClassNameAndAccountId(@Param("className") String className, @Param("accountId") Long accountId);

    @Query("SELECT l FROM Lesson l WHERE l.accountId = :accountId AND l.subjectName = :subjectName AND (l.className = :className OR l.className = 'គ្រប់ថ្នាក់' OR l.className = 'ថ្នាក់រៀនទាំងអស់')")
    List<Lesson> findByClassNameAndSubjectNameAndAccountId(@Param("className") String className, @Param("subjectName") String subjectName, @Param("accountId") Long accountId);

    @Query("SELECT l FROM Lesson l WHERE l.className = :className OR l.className = 'គ្រប់ថ្នាក់' OR l.className = 'ថ្នាក់រៀនទាំងអស់'")
    List<Lesson> findByClassNameForStudent(@Param("className") String className);

    List<Lesson> findByClassName(String className);
}

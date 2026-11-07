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

    @Query("SELECT l FROM Lesson l WHERE (:accountId IS NULL OR l.accountId = :accountId OR (:accountId IN (1, 97) AND (l.accountId IN (1, 97) OR l.accountId IS NULL))) ORDER BY l.id ASC")
    List<Lesson> findByAccountIdOrderByIdAsc(@Param("accountId") Long accountId);

    @Query("SELECT l FROM Lesson l WHERE (:accountId IS NULL OR l.accountId = :accountId OR (:accountId IN (1, 97) AND (l.accountId IN (1, 97) OR l.accountId IS NULL)) OR l.accountId = 1) ORDER BY l.id ASC")
    List<Lesson> findLessonsForStudent(@Param("accountId") Long accountId);

    @Query("SELECT l FROM Lesson l WHERE l.accountId = :accountId AND (l.className = :className OR l.className LIKE CONCAT('%', :className, '%')) ORDER BY l.id ASC")
    List<Lesson> findByClassNameAndAccountId(@Param("className") String className, @Param("accountId") Long accountId);

    @Query("SELECT l FROM Lesson l WHERE l.accountId = :accountId AND l.subjectName = :subjectName AND (l.className = :className OR l.className LIKE CONCAT('%', :className, '%')) ORDER BY l.id ASC")
    List<Lesson> findByClassNameAndSubjectNameAndAccountId(@Param("className") String className, @Param("subjectName") String subjectName, @Param("accountId") Long accountId);

    @Query("SELECT l FROM Lesson l WHERE (l.className = :className OR l.className LIKE CONCAT('%', :className, '%') OR l.className = 'គ្រប់ថ្នាក់' OR l.className = 'ថ្នាក់រៀនទាំងអស់' OR UPPER(l.className) = 'ALL') ORDER BY l.id ASC")
    List<Lesson> findByClassNameForStudent(@Param("className") String className);

    @Query("SELECT l FROM Lesson l WHERE (l.accountId = :accountId OR :accountId IS NULL) AND (l.className = :className OR l.className LIKE CONCAT('%', :className, '%') OR l.className = 'គ្រប់ថ្នាក់' OR l.className = 'ថ្នាក់រៀនទាំងអស់' OR UPPER(l.className) = 'ALL') ORDER BY l.id ASC")
    List<Lesson> findByClassNameAndAccountIdForStudent(@Param("className") String className, @Param("accountId") Long accountId);

    List<Lesson> findByClassName(String className);
    List<Lesson> findByClassNameOrderByIdAsc(String className);
}

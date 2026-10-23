package com.nubb.classroom.repository;

import com.nubb.classroom.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByAccountId(Long accountId);
    List<Lesson> findByClassNameAndAccountId(String className, Long accountId);
    List<Lesson> findByClassNameAndSubjectNameAndAccountId(String className, String subjectName, Long accountId);
    List<Lesson> findByClassName(String className);
}

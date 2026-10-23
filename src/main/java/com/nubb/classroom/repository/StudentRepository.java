package com.nubb.classroom.repository;

import com.nubb.classroom.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentCode(String studentCode);
    java.util.List<Student> findByClassName(String className);
    java.util.List<com.nubb.classroom.model.Student> findByAccountId(Long accountId);
    java.util.List<com.nubb.classroom.model.Student> findByClassNameAndAccountId(String className, Long accountId);
}


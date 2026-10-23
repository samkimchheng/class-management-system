package com.nubb.classroom.repository;

import com.nubb.classroom.model.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    List<Classroom> findByAccountId(Long accountId);
    boolean existsByClassNameAndAccountId(String className, Long accountId);
}
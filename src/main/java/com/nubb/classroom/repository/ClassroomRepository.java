package com.nubb.classroom.repository;

import com.nubb.classroom.model.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    @Query("SELECT c FROM Classroom c WHERE (:accountId IS NULL OR c.accountId = :accountId OR c.accountId = 97 OR c.accountId = 1 OR c.accountId IS NULL)")
    List<Classroom> findByAccountId(@Param("accountId") Long accountId);

    boolean existsByClassNameAndAccountId(String className, Long accountId);
}

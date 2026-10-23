package com.nubb.classroom.repository;

import com.nubb.classroom.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByClassName(String className);
    java.util.List<com.nubb.classroom.model.Schedule> findByClassNameAndAccountId(String className, Long accountId);
}


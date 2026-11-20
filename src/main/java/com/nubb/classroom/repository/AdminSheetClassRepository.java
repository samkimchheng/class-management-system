package com.nubb.classroom.repository;

import com.nubb.classroom.model.AdminSheetClass;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdminSheetClassRepository extends JpaRepository<AdminSheetClass, Long> {
    List<AdminSheetClass> findAllByOrderByIdDesc();
}

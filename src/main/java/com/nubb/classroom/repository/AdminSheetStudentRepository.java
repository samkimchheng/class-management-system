package com.nubb.classroom.repository;

import com.nubb.classroom.model.AdminSheetStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdminSheetStudentRepository extends JpaRepository<AdminSheetStudent, Long> {
    List<AdminSheetStudent> findBySheetClassIdOrderByOrderNoAscIdAsc(Long sheetClassId);
    long countBySheetClassId(Long sheetClassId);
    long countBySheetClassIdAndGender(Long sheetClassId, String gender);
    void deleteBySheetClassId(Long sheetClassId);
}

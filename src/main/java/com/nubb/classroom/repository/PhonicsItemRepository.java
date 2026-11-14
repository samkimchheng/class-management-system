package com.nubb.classroom.repository;

import com.nubb.classroom.model.PhonicsItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhonicsItemRepository extends JpaRepository<PhonicsItem, Long> {
    List<PhonicsItem> findByPhonicsLevelOrderByItemNumberAsc(Integer phonicsLevel);
    List<PhonicsItem> findByPhonicsLevelAndActiveTrueOrderByItemNumberAsc(Integer phonicsLevel);
    List<PhonicsItem> findByActiveTrueOrderByPhonicsLevelAscItemNumberAsc();
    List<PhonicsItem> findAllByOrderByPhonicsLevelAscItemNumberAsc();
    long countByPhonicsLevel(Integer phonicsLevel);
    long countByActiveTrue();
}

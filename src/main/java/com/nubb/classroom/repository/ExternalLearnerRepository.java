package com.nubb.classroom.repository;

import com.nubb.classroom.model.ExternalLearner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExternalLearnerRepository extends JpaRepository<ExternalLearner, Long> {
    Optional<ExternalLearner> findByLearnerCodeIgnoreCase(String learnerCode);
    List<ExternalLearner> findAllByOrderByIdDesc();
}

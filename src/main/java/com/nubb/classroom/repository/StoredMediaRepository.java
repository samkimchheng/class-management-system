package com.nubb.classroom.repository;

import com.nubb.classroom.model.StoredMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoredMediaRepository extends JpaRepository<StoredMedia, Long> {
    Optional<StoredMedia> findByStorageKey(String storageKey);
    boolean existsByStorageKey(String storageKey);
}

package com.nubb.classroom.repository;

import com.nubb.classroom.model.CertificateRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRecordRepository extends JpaRepository<CertificateRecord, Long> {
    Optional<CertificateRecord> findBySerialNumber(String serialNumber);
    Optional<CertificateRecord> findByStudentId(Long studentId);
    List<CertificateRecord> findByClassName(String className);
    Optional<CertificateRecord> findFirstByClassNameOrderByIdDesc(String className);
}

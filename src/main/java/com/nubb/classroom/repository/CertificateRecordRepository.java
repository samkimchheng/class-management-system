package com.nubb.classroom.repository;

import com.nubb.classroom.model.CertificateRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateRecordRepository extends JpaRepository<CertificateRecord, Long> {
    Optional<CertificateRecord> findFirstBySerialNumberOrderByIdDesc(String serialNumber);
    Optional<CertificateRecord> findFirstByStudentIdOrderByIdDesc(Long studentId);
    List<CertificateRecord> findByClassName(String className);
    Optional<CertificateRecord> findFirstByClassNameOrderByIdDesc(String className);
    List<CertificateRecord> findAllByStudentId(Long studentId);
    List<CertificateRecord> findAllBySerialNumber(String serialNumber);
}

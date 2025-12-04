package com.example.backend.repository;

import com.example.backend.entity.ContactRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContactRecordRepository extends JpaRepository<ContactRecord, Long> {
    List<ContactRecord> findBySalesmanId(Long salesmanId);
    List<ContactRecord> findByCustomerId(Long customerId);
    List<ContactRecord> findByContactMethod(String contactMethod);
    List<ContactRecord> findByContactTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    List<ContactRecord> findBySalesmanIdAndContactTimeBetween(Long salesmanId, LocalDateTime startTime, LocalDateTime endTime);
}

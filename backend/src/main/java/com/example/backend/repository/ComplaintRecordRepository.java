package com.example.backend.repository;

import com.example.backend.entity.ComplaintRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRecordRepository extends JpaRepository<ComplaintRecord, Long> {
    List<ComplaintRecord> findBySalesman_Id(Long salesmanId);
    List<ComplaintRecord> findByCustomer_Id(Long customerId);
    List<ComplaintRecord> findByStatus(String status);
    List<ComplaintRecord> findBySeverity(String severity);
    List<ComplaintRecord> findByComplaintType(String complaintType);
    
    // 统计投诉数量
    @Query("SELECT COUNT(c) FROM ComplaintRecord c WHERE c.salesman.id = :salesmanId")
    Long countBySalesman(@Param("salesmanId") Long salesmanId);
    
    // 统计各状态数量
    Long countByStatus(String status);
    Long countBySeverity(String severity);
}

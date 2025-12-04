package com.example.backend.repository;

import com.example.backend.entity.ComplaintRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ComplaintRecordRepository extends JpaRepository<ComplaintRecord, Long> {
    List<ComplaintRecord> findBySalesmanId(Long salesmanId);
    List<ComplaintRecord> findByCustomerId(Long customerId);
    List<ComplaintRecord> findByStatus(String status);
    List<ComplaintRecord> findByComplaintDateBetween(LocalDate startDate, LocalDate endDate);
    
    // 统计投诉数量
    @Query("SELECT COUNT(c) FROM ComplaintRecord c WHERE c.salesman.id = :salesmanId")
    Long countBySalesman(@Param("salesmanId") Long salesmanId);
}

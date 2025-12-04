package com.example.backend.repository;

import com.example.backend.entity.ServiceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ServiceRecordRepository extends JpaRepository<ServiceRecord, Long> {
    List<ServiceRecord> findBySalesmanId(Long salesmanId);
    List<ServiceRecord> findByCustomerId(Long customerId);
    List<ServiceRecord> findByServiceDateBetween(LocalDate startDate, LocalDate endDate);
    
    // 统计平均满意度
    @Query("SELECT AVG(s.satisfactionRating) FROM ServiceRecord s WHERE s.salesman.id = :salesmanId")
    Double getAverageSatisfactionBySalesman(@Param("salesmanId") Long salesmanId);
}

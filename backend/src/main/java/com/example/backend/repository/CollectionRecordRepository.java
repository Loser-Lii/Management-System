package com.example.backend.repository;

import com.example.backend.entity.CollectionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CollectionRecordRepository extends JpaRepository<CollectionRecord, Long> {
    List<CollectionRecord> findBySalesmanId(Long salesmanId);
    List<CollectionRecord> findByCustomerId(Long customerId);
    List<CollectionRecord> findByStatus(String status);
    List<CollectionRecord> findByCollectionDateBetween(LocalDate startDate, LocalDate endDate);
    
    // 统计回款率
    @Query("SELECT SUM(c.amountReceived) FROM CollectionRecord c WHERE c.salesman.id = :salesmanId")
    BigDecimal sumReceivedAmountBySalesman(@Param("salesmanId") Long salesmanId);
    
    @Query("SELECT SUM(c.amountDue) FROM CollectionRecord c WHERE c.salesman.id = :salesmanId")
    BigDecimal sumDueAmountBySalesman(@Param("salesmanId") Long salesmanId);
}

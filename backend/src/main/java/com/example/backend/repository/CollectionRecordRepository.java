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
    // ✅ 查询销售员的催款记录
    List<CollectionRecord> findBySalesman_Id(Long salesmanId);

    // ✅ 查询客户的催款记录
    List<CollectionRecord> findByCustomer_Id(Long customerId);
    List<CollectionRecord> findByCollectionStatus(String collectionStatus);
    List<CollectionRecord> findByCollectionDateBetween(LocalDate startDate, LocalDate endDate);
    List<CollectionRecord> findByOrderNo(String orderNo);
    
    // 统计总回款金额（本次回款）
    @Query("SELECT SUM(c.currentAmount) FROM CollectionRecord c WHERE c.salesman.id = :salesmanId")
    BigDecimal sumCurrentAmountBySalesman(@Param("salesmanId") Long salesmanId);

    // 统计指定日期的催款记录数
    @Query("SELECT COUNT(c) FROM CollectionRecord c WHERE DATE(c.createTime) = :createDate")
    Long countByDate(@Param("createDate") LocalDate createDate);
}

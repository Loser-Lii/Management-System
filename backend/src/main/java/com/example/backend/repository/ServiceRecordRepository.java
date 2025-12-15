package com.example.backend.repository;

import com.example.backend.entity.ServiceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRecordRepository extends JpaRepository<ServiceRecord, Long> {
    Optional<ServiceRecord> findByServiceNo(String serviceNo);
    List<ServiceRecord> findBySalesman_Id(Long salesmanId);
    List<ServiceRecord> findByCustomer_Id(Long customerId);
    List<ServiceRecord> findByServiceType(String serviceType);
    List<ServiceRecord> findByStatus(String status);
    List<ServiceRecord> findByStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    // 统计平均满意度
    @Query("SELECT AVG(s.satisfaction) FROM ServiceRecord s WHERE s.salesman.id = :salesmanId")
    Double getAverageSatisfactionBySalesman(@Param("salesmanId") Long salesmanId);
    
    // 按状态和销售员查询
    @Query("SELECT s FROM ServiceRecord s WHERE s.salesman.id = :salesmanId AND s.status = :status")
    List<ServiceRecord> findBySalesmanIdAndStatus(@Param("salesmanId") Long salesmanId, @Param("status") String status);

    // 统计指定日期的服务记录数
    @Query("SELECT COUNT(s) FROM ServiceRecord s WHERE DATE(s.createTime) = :createDate")
    Long countByDate(@Param("createDate") LocalDate createDate);
}

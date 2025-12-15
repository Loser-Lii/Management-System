package com.example.backend.repository;

import com.example.backend.entity.ContactRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContactRecordRepository extends JpaRepository<ContactRecord, Long> {
    List<ContactRecord> findBySalesman_Id(Long salesmanId);
    List<ContactRecord> findByCustomer_Id(Long customerId);
    List<ContactRecord> findByContactWay(String contactWay);
    List<ContactRecord> findByContactDateBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    @Query("SELECT c FROM ContactRecord c WHERE c.salesman.id = :salesmanId AND c.contactDate BETWEEN :startTime AND :endTime")
    List<ContactRecord> findBySalesmanIdAndContactDateBetween(@Param("salesmanId") Long salesmanId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}

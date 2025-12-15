package com.example.backend.repository;

import com.example.backend.entity.SalesRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesRecordRepository extends JpaRepository<SalesRecord, Long> {

    // 根据订单号查询
    Optional<SalesRecord> findByOrderNo(String orderNo);

    // ✅ 根据销售员查询
    @Query("SELECT s FROM SalesRecord s WHERE s.salesman.id = :salesmanId")
    List<SalesRecord> findBySalesmanId(@Param("salesmanId") Long salesmanId);

    // ✅ 根据客户查询
    @Query("SELECT s FROM SalesRecord s WHERE s.customer.id = :customerId")
    List<SalesRecord> findByCustomerId(@Param("customerId") Long customerId);

    // 根据产品查询
    @Query("SELECT s FROM SalesRecord s WHERE s.product.id = :productId")
    List<SalesRecord> findByProductId(@Param("productId") Long productId);

    // 根据时间段查询
    List<SalesRecord> findBySaleDateBetween(LocalDate startDate, LocalDate endDate);

    // 根据销售员和时间段查询
    List<SalesRecord> findBySalesman_IdAndSaleDateBetween(Long salesmanId, LocalDate startDate, LocalDate endDate);

    // 统计销售员的总销售额（只统计已确认的记录）
    @Query("SELECT SUM(s.totalAmount) FROM SalesRecord s WHERE s.salesman.id = :salesmanId AND s.status = 'approved'")
    BigDecimal sumTotalAmountBySalesman(@Param("salesmanId") Long salesmanId);

    // 统计销售员的总提成（只统计已确认的记录）
    @Query("SELECT SUM(s.commissionAmount) FROM SalesRecord s WHERE s.salesman.id = :salesmanId AND s.status = 'approved'")
    BigDecimal sumCommissionAmountBySalesman(@Param("salesmanId") Long salesmanId);

    // 统计销售员在时间段内的销售额（只统计已确认的记录）
    @Query("SELECT SUM(s.totalAmount) FROM SalesRecord s WHERE s.salesman.id = :salesmanId AND s.saleDate BETWEEN :startDate AND :endDate AND s.status = 'approved'")
    BigDecimal sumTotalAmountBySalesmanAndDateRange(@Param("salesmanId") Long salesmanId, 
                                                      @Param("startDate") LocalDate startDate, 
                                                      @Param("endDate") LocalDate endDate);

    // 查询最新的销售记录（用于生成订单号）
    SalesRecord findFirstByOrderByIdDesc();

       // 按销售日期倒序获取最新一条记录
       SalesRecord findTopByOrderBySaleDateDesc();

    // 按销售日期倒序查询所有记录
    List<SalesRecord> findAllByOrderBySaleDateDesc();

    // 按销售员和日期倒序查询
    List<SalesRecord> findBySalesman_IdOrderBySaleDateDesc(Long salesmanId);

    // 根据客户查询
    List<SalesRecord> findByCustomer_Id(Long customerId);

    // 根据状态查询
    List<SalesRecord> findByStatusOrderBySaleDateDesc(String status);

    // 根据销售员和状态查询
    List<SalesRecord> findBySalesman_IdAndStatusOrderBySaleDateDesc(Long salesmanId, String status);

    // 根据客户和状态查询
    List<SalesRecord> findByCustomer_IdAndStatusOrderBySaleDateDesc(Long customerId, String status);

    // 根据订单号查询（返回列表以支持可能的重复订单号）
    List<SalesRecord> findByOrderNoOrderBySaleDateDesc(String orderNo);

    // 统计所有销售员的销售数据（用于排名）（只统计已确认的记录）
    @Query("SELECT s.salesman.id, s.salesman.name, SUM(s.totalAmount), SUM(s.commissionAmount) " +
           "FROM SalesRecord s WHERE s.status = 'approved' GROUP BY s.salesman.id, s.salesman.name ORDER BY SUM(s.totalAmount) DESC")
    List<Object[]> getSalesRanking();

    // 统计销售员在时间段内的销售额（用于业绩建议）（只统计已确认的记录）
    @Query("SELECT s.salesman.id, s.salesman.name, s.salesman.level, s.salesman.avatar, SUM(s.totalAmount) " +
           "FROM SalesRecord s WHERE s.saleDate BETWEEN :startDate AND :endDate AND s.status = 'approved' " +
           "AND (s.salesman.status IS NULL OR s.salesman.status = 'active') " +
           "GROUP BY s.salesman.id, s.salesman.name, s.salesman.level, s.salesman.avatar " +
           "ORDER BY SUM(s.totalAmount) DESC")
    List<Object[]> getPerformanceRanking(@Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);

    // 统计在指定时间段内有销售记录的销售员数量（活跃销售员）（只统计已确认的记录）
    @Query("SELECT COUNT(DISTINCT s.salesman.id) FROM SalesRecord s WHERE s.saleDate BETWEEN :startDate AND :endDate AND s.status = 'approved'")
    Long countActiveSalesmen(@Param("startDate") LocalDate startDate, 
                              @Param("endDate") LocalDate endDate);

    // 统计指定日期的销售记录数
    @Query("SELECT COUNT(s) FROM SalesRecord s WHERE DATE(s.saleDate) = :saleDate")
    Long countByDate(@Param("saleDate") LocalDate saleDate);

       // 查找某销售员为某客户的销售记录涉及的产品（去重）
       @Query("SELECT DISTINCT s.product FROM SalesRecord s WHERE s.salesman.id = :salesmanId AND s.customer.id = :customerId AND s.product IS NOT NULL")
       List<com.example.backend.entity.Product> findDistinctProductsBySalesmanAndCustomer(@Param("salesmanId") Long salesmanId,
                                                                                                                                                     @Param("customerId") Long customerId);
}

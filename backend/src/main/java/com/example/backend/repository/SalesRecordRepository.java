package com.example.backend.repository;

import com.example.backend.entity.SalesRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SalesRecordRepository extends JpaRepository<SalesRecord, Long> {

    // 根据销售员查询
    List<SalesRecord> findBySalesmanId(Long salesmanId);

    // 根据客户查询
    List<SalesRecord> findByCustomerId(Long customerId);

    // 根据产品查询
    List<SalesRecord> findByProductId(Long productId);

    // 根据时间段查询
    List<SalesRecord> findBySaleDateBetween(LocalDate startDate, LocalDate endDate);

    // 根据销售员和时间段查询
    List<SalesRecord> findBySalesmanIdAndSaleDateBetween(Long salesmanId, LocalDate startDate, LocalDate endDate);

    // 统计销售员的总销售额
    @Query("SELECT SUM(s.totalAmount) FROM SalesRecord s WHERE s.salesman.id = :salesmanId")
    BigDecimal sumTotalAmountBySalesman(@Param("salesmanId") Long salesmanId);

    // 统计销售员的总提成
    @Query("SELECT SUM(s.commissionAmount) FROM SalesRecord s WHERE s.salesman.id = :salesmanId")
    BigDecimal sumCommissionAmountBySalesman(@Param("salesmanId") Long salesmanId);

    // 统计销售员在时间段内的销售额
    @Query("SELECT SUM(s.totalAmount) FROM SalesRecord s WHERE s.salesman.id = :salesmanId AND s.saleDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalAmountBySalesmanAndDateRange(@Param("salesmanId") Long salesmanId, 
                                                     @Param("startDate") LocalDate startDate, 
                                                     @Param("endDate") LocalDate endDate);

    // 统计所有销售员的销售数据（用于排名）
    @Query("SELECT s.salesman.id, s.salesman.name, SUM(s.totalAmount), SUM(s.commissionAmount) " +
           "FROM SalesRecord s GROUP BY s.salesman.id, s.salesman.name ORDER BY SUM(s.totalAmount) DESC")
    List<Object[]> getSalesRanking();

    // 统计销售员在时间段内的销售额（用于业绩建议）
    @Query("SELECT s.salesman.id, s.salesman.name, s.salesman.level, SUM(s.totalAmount) " +
           "FROM SalesRecord s WHERE s.saleDate BETWEEN :startDate AND :endDate " +
           "GROUP BY s.salesman.id, s.salesman.name, s.salesman.level " +
           "ORDER BY SUM(s.totalAmount) DESC")
    List<Object[]> getPerformanceRanking(@Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);

    // 统计在指定时间段内有销售记录的销售员数量（活跃销售员）
    @Query("SELECT COUNT(DISTINCT s.salesman.id) FROM SalesRecord s WHERE s.saleDate BETWEEN :startDate AND :endDate")
    Long countActiveSalesmen(@Param("startDate") LocalDate startDate, 
                              @Param("endDate") LocalDate endDate);
}

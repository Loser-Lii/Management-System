package com.example.backend.service;

import com.example.backend.dto.PerformanceStatsDTO;
import com.example.backend.entity.Salesman;
import com.example.backend.repository.SalesRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 绩效统计服务
 */
@Service
public class PerformanceService {

    @Autowired
    private SalesmanService salesmanService;

    @Autowired
    private SalesRecordService salesRecordService;

    @Autowired
    private ServiceRecordService serviceRecordService;

    @Autowired
    private CollectionRecordService collectionRecordService;

    @Autowired
    private ComplaintRecordService complaintRecordService;

    @Autowired
    private SalesRecordRepository salesRecordRepository;

    /**
     * 获取单个销售员的综合绩效统计
     */
    public PerformanceStatsDTO getSalesmanPerformance(Long salesmanId) {
        Salesman salesman = salesmanService.findById(salesmanId);
        
        PerformanceStatsDTO stats = new PerformanceStatsDTO();
        stats.setSalesmanId(salesmanId);
        stats.setSalesmanName(salesman.getName());
        stats.setTotalSales(salesRecordService.getTotalSalesBySalesman(salesmanId));
        stats.setTotalCommission(salesRecordService.getTotalCommissionBySalesman(salesmanId));
        stats.setAverageSatisfaction(serviceRecordService.getAverageSatisfaction(salesmanId));
        stats.setCollectionRate(collectionRecordService.getCollectionRate(salesmanId));
        stats.setComplaintCount(complaintRecordService.countBySalesman(salesmanId));
        stats.setSalesCount((long) salesRecordService.findBySalesman(salesmanId).size());
        
        return stats;
    }

    /**
     * 获取所有销售员的绩效统计列表
     */
    public List<PerformanceStatsDTO> getAllSalesmenPerformance() {
        List<Salesman> salesmen = salesmanService.findAll();
        List<PerformanceStatsDTO> statsList = new ArrayList<>();
        
        for (Salesman salesman : salesmen) {
            statsList.add(getSalesmanPerformance(salesman.getId()));
        }
        
        return statsList;
    }

    /**
     * 获取指定时间段内的销售员绩效
     */
    public PerformanceStatsDTO getSalesmanPerformanceByDateRange(Long salesmanId, LocalDate startDate, LocalDate endDate) {
        Salesman salesman = salesmanService.findById(salesmanId);
        
        PerformanceStatsDTO stats = new PerformanceStatsDTO();
        stats.setSalesmanId(salesmanId);
        stats.setSalesmanName(salesman.getName());
        
        // 计算时间段内的销售额
        BigDecimal totalSales = salesRecordRepository.sumTotalAmountBySalesmanAndDateRange(salesmanId, startDate, endDate);
        stats.setTotalSales(totalSales != null ? totalSales : BigDecimal.ZERO);
        
        // 其他指标（可以继续扩展）
        stats.setSalesCount((long) salesRecordService.findBySalesmanAndDateRange(salesmanId, startDate, endDate).size());
        
        return stats;
    }

    /**
     * 获取活跃销售员数量（近三个月内有销售记录）
     */
    public Long getActiveSalesmenCount() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(3);
        return salesRecordRepository.countActiveSalesmen(startDate, endDate);
    }
}

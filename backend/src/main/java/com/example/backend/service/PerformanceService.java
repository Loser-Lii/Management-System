package com.example.backend.service;

import com.example.backend.dto.PerformanceStatsDTO;
import com.example.backend.entity.Salesman;
import com.example.backend.entity.ComplaintRecord;
import com.example.backend.repository.PerformanceViewRepository;
import com.example.backend.repository.PerformanceViewRow;
import com.example.backend.repository.SalesRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 绩效统计服务
 */
@Service
public class PerformanceService {

    private static final Logger log = LoggerFactory.getLogger(PerformanceService.class);

    @Autowired
    private SalesmanService salesmanService;

    @Autowired
    private SalesRecordService salesRecordService;

    @Autowired
    private ServiceRecordService serviceRecordService;

    @Autowired
    private ComplaintRecordService complaintRecordService;

    @Autowired
    private SalesRecordRepository salesRecordRepository;

    @Autowired
    private PerformanceViewRepository performanceViewRepository;

    /**
     * 获取单个销售员的综合绩效统计
     */
    public PerformanceStatsDTO getSalesmanPerformance(Long salesmanId) {
        Salesman salesman = salesmanService.findById(salesmanId);
        PerformanceStatsDTO stats = new PerformanceStatsDTO();
        stats.setSalesmanId(salesmanId);
        stats.setSalesmanName(salesman.getName());
        stats.setCollectionRate(BigDecimal.ZERO); // 简化：不再计算回款率
        stats.setComplaintCount(complaintRecordService.countBySalesman(salesmanId));

        try {
            PerformanceViewRow row = performanceViewRepository.findBySalesmanId(salesmanId);
            if (row != null) {
                fillFromView(stats, row);
            } else {
                fillEmpty(stats);
            }
        } catch (DataAccessException ex) {
            log.warn("vw_salesman_performance not available, fallback to legacy aggregation: {}", ex.getMessage());
            fillFromLegacy(stats, salesmanId);
        }

        return stats;
    }

    /**
     * 获取所有销售员的绩效统计列表（仅在职）
     */
    public List<PerformanceStatsDTO> getAllSalesmenPerformance() {
        try {
            List<PerformanceViewRow> rows = performanceViewRepository.findAllView();
            List<PerformanceStatsDTO> statsList = new ArrayList<>();
            for (PerformanceViewRow row : rows) {
                PerformanceStatsDTO dto = new PerformanceStatsDTO();
                fillFromView(dto, row);
                // 补投诉总数（含非态度类）
                dto.setComplaintCount(complaintRecordService.countBySalesman(row.getSalesmanId()));
                dto.setCollectionRate(BigDecimal.ZERO);
                statsList.add(dto);
            }
            return statsList;
        } catch (DataAccessException ex) {
            log.warn("vw_salesman_performance not available, fallback to legacy aggregation: {}", ex.getMessage());
            List<Salesman> salesmen = salesmanService.findAllActive();
            List<PerformanceStatsDTO> statsList = new ArrayList<>();
            for (Salesman s : salesmen) {
                statsList.add(getSalesmanPerformanceLegacy(s.getId(), s.getName()));
            }
            return statsList;
        }
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
        applyAttitudePenalty(stats, complaintRecordService.findBySalesman(salesmanId));
        stats.setComplaintCount(complaintRecordService.countBySalesman(salesmanId));
        stats.setCollectionRate(BigDecimal.ZERO);
        
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

    private void applyAttitudePenalty(PerformanceStatsDTO stats, List<ComplaintRecord> complaints) {
        Map<String, Integer> fines = new HashMap<>();
        fines.put("critical", 2000);
        fines.put("high", 1000);
        fines.put("normal", 500);
        fines.put("low", 200);

        long critical = 0, high = 0, normal = 0, low = 0, total = 0;
        BigDecimal penalty = BigDecimal.ZERO;

        for (ComplaintRecord c : complaints) {
            if (c.getComplaintType() == null || !"attitude".equalsIgnoreCase(c.getComplaintType())) {
                continue;
            }
            String sev = c.getSeverity() == null ? "" : c.getSeverity().toLowerCase();
            Integer fine = fines.get(sev);
            if (fine == null) {
                continue;
            }
            switch (sev) {
                case "critical": critical++; break;
                case "high": high++; break;
                case "normal": normal++; break;
                case "low": low++; break;
                default: break;
            }
            total++;
            penalty = penalty.add(BigDecimal.valueOf(fine));
        }

        stats.setAttitudeComplaintCount(total);
        stats.setAttitudeCriticalCount(critical);
        stats.setAttitudeHighCount(high);
        stats.setAttitudeNormalCount(normal);
        stats.setAttitudeLowCount(low);
        stats.setAttitudePenalty(penalty);
    }

    private void fillFromView(PerformanceStatsDTO dto, PerformanceViewRow row) {
        dto.setSalesmanId(row.getSalesmanId());
        dto.setSalesmanName(row.getSalesmanName());
        dto.setTotalSales(nullSafe(row.getTotalSales()));
        dto.setTotalCommission(nullSafe(row.getTotalCommission()));
        dto.setSalesCount(row.getSalesCount() != null ? row.getSalesCount() : 0L);
        dto.setAverageSatisfaction(row.getAverageSatisfaction() != null ? row.getAverageSatisfaction() : 0.0);
        dto.setAttitudeComplaintCount(row.getAttitudeComplaintCount() != null ? row.getAttitudeComplaintCount() : 0L);
        dto.setAttitudeCriticalCount(row.getAttitudeCriticalCount() != null ? row.getAttitudeCriticalCount() : 0L);
        dto.setAttitudeHighCount(row.getAttitudeHighCount() != null ? row.getAttitudeHighCount() : 0L);
        dto.setAttitudeNormalCount(row.getAttitudeNormalCount() != null ? row.getAttitudeNormalCount() : 0L);
        dto.setAttitudeLowCount(row.getAttitudeLowCount() != null ? row.getAttitudeLowCount() : 0L);
        dto.setAttitudePenalty(nullSafe(row.getAttitudePenalty()));
    }

    private void fillEmpty(PerformanceStatsDTO dto) {
        dto.setTotalSales(BigDecimal.ZERO);
        dto.setTotalCommission(BigDecimal.ZERO);
        dto.setSalesCount(0L);
        dto.setAverageSatisfaction(0.0);
        dto.setAttitudeComplaintCount(0L);
        dto.setAttitudeCriticalCount(0L);
        dto.setAttitudeHighCount(0L);
        dto.setAttitudeNormalCount(0L);
        dto.setAttitudeLowCount(0L);
        dto.setAttitudePenalty(BigDecimal.ZERO);
    }

    private void fillFromLegacy(PerformanceStatsDTO stats, Long salesmanId) {
        BigDecimal totalSales = salesRecordService.getTotalSalesBySalesman(salesmanId);
        BigDecimal totalCommission = salesRecordService.getTotalCommissionBySalesman(salesmanId);
        Double avgSatisfaction = serviceRecordService.getAverageSatisfaction(salesmanId);
        List<ComplaintRecord> complaints = complaintRecordService.findBySalesman(salesmanId);

        stats.setTotalSales(totalSales != null ? totalSales : BigDecimal.ZERO);
        stats.setTotalCommission(totalCommission != null ? totalCommission : BigDecimal.ZERO);
        stats.setAverageSatisfaction(avgSatisfaction != null ? avgSatisfaction : 0.0);
        stats.setSalesCount((long) salesRecordService.findBySalesman(salesmanId).size());
        applyAttitudePenalty(stats, complaints);
    }

    private PerformanceStatsDTO getSalesmanPerformanceLegacy(Long salesmanId, String name) {
        PerformanceStatsDTO dto = new PerformanceStatsDTO();
        dto.setSalesmanId(salesmanId);
        dto.setSalesmanName(name);
        dto.setCollectionRate(BigDecimal.ZERO);
        dto.setComplaintCount(complaintRecordService.countBySalesman(salesmanId));
        fillFromLegacy(dto, salesmanId);
        return dto;
    }

    private BigDecimal nullSafe(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}

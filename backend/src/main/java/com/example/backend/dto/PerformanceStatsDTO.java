package com.example.backend.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 绩效统计响应DTO
 */
@Data
public class PerformanceStatsDTO {
    private Long salesmanId;
    private String salesmanName;
    private BigDecimal totalSales;              // 总销售额
    private BigDecimal totalCommission;         // 总提成
    private Double averageSatisfaction;         // 平均满意度
    private BigDecimal collectionRate;          // 回款率
    private Long complaintCount;                // 投诉数量
    private Long salesCount;                    // 销售单数
}

package com.example.backend.repository;

import java.math.BigDecimal;

/**
 * 数据行映射：vw_salesman_performance
 */
public class PerformanceViewRow {
    private Long salesmanId;
    private String salesmanName;
    private BigDecimal totalSales;
    private BigDecimal totalCommission;
    private Long salesCount;
    private Double averageSatisfaction;
    private Long attitudeComplaintCount;
    private Long attitudeCriticalCount;
    private Long attitudeHighCount;
    private Long attitudeNormalCount;
    private Long attitudeLowCount;
    private BigDecimal attitudePenalty;

    public Long getSalesmanId() {
        return salesmanId;
    }

    public void setSalesmanId(Long salesmanId) {
        this.salesmanId = salesmanId;
    }

    public String getSalesmanName() {
        return salesmanName;
    }

    public void setSalesmanName(String salesmanName) {
        this.salesmanName = salesmanName;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }

    public BigDecimal getTotalCommission() {
        return totalCommission;
    }

    public void setTotalCommission(BigDecimal totalCommission) {
        this.totalCommission = totalCommission;
    }

    public Long getSalesCount() {
        return salesCount;
    }

    public void setSalesCount(Long salesCount) {
        this.salesCount = salesCount;
    }

    public Double getAverageSatisfaction() {
        return averageSatisfaction;
    }

    public void setAverageSatisfaction(Double averageSatisfaction) {
        this.averageSatisfaction = averageSatisfaction;
    }

    public Long getAttitudeComplaintCount() {
        return attitudeComplaintCount;
    }

    public void setAttitudeComplaintCount(Long attitudeComplaintCount) {
        this.attitudeComplaintCount = attitudeComplaintCount;
    }

    public Long getAttitudeCriticalCount() {
        return attitudeCriticalCount;
    }

    public void setAttitudeCriticalCount(Long attitudeCriticalCount) {
        this.attitudeCriticalCount = attitudeCriticalCount;
    }

    public Long getAttitudeHighCount() {
        return attitudeHighCount;
    }

    public void setAttitudeHighCount(Long attitudeHighCount) {
        this.attitudeHighCount = attitudeHighCount;
    }

    public Long getAttitudeNormalCount() {
        return attitudeNormalCount;
    }

    public void setAttitudeNormalCount(Long attitudeNormalCount) {
        this.attitudeNormalCount = attitudeNormalCount;
    }

    public Long getAttitudeLowCount() {
        return attitudeLowCount;
    }

    public void setAttitudeLowCount(Long attitudeLowCount) {
        this.attitudeLowCount = attitudeLowCount;
    }

    public BigDecimal getAttitudePenalty() {
        return attitudePenalty;
    }

    public void setAttitudePenalty(BigDecimal attitudePenalty) {
        this.attitudePenalty = attitudePenalty;
    }
}

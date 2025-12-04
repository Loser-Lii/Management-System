package com.example.backend.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 业绩建议DTO
 */
public class PerformanceSuggestionDTO {
    
    private List<SalesmanPerformance> topPerformers;  // 业绩最好的2个销售员
    private List<SalesmanPerformance> bottomPerformers;  // 业绩最差的2个销售员
    
    public static class SalesmanPerformance {
        private Long id;
        private String name;
        private String level;
        private BigDecimal totalAmount;
        
        public SalesmanPerformance(Long id, String name, String level, BigDecimal totalAmount) {
            this.id = id;
            this.name = name;
            this.level = level;
            this.totalAmount = totalAmount;
        }
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getLevel() {
            return level;
        }
        
        public void setLevel(String level) {
            this.level = level;
        }
        
        public BigDecimal getTotalAmount() {
            return totalAmount;
        }
        
        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }
    }
    
    public List<SalesmanPerformance> getTopPerformers() {
        return topPerformers;
    }
    
    public void setTopPerformers(List<SalesmanPerformance> topPerformers) {
        this.topPerformers = topPerformers;
    }
    
    public List<SalesmanPerformance> getBottomPerformers() {
        return bottomPerformers;
    }
    
    public void setBottomPerformers(List<SalesmanPerformance> bottomPerformers) {
        this.bottomPerformers = bottomPerformers;
    }
}

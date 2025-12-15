package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * 服务记录实体
 */
@Data
@Entity
@Table(name = "service_record")
public class ServiceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_no", nullable = false, length = 50, unique = true)
    private String serviceNo;               // 服务单号

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "salesman_id", nullable = false)
    private Salesman salesman;              // 执行销售员

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;              // 客户

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private Product product;                // 关联产品（可选）

    @Column(name = "related_order_no", length = 50)
    private String relatedOrderNo;          // 关联销售单号

    @Column(name = "service_type", nullable = false, length = 20)
    private String serviceType;             // 服务类型: repair/maintenance/training/consult/installation

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;        // 开始时间

    @Column(name = "end_time")
    private LocalDateTime endTime;          // 结束时间

    @Column(length = 500, nullable = false)
    private String content;                 // 服务内容描述

    @Column(length = 500)
    private String solution;                // 解决方案

    @Column(name = "satisfaction")
    private Integer satisfaction;           // 满意度评分（1-5星）

    @Column(nullable = false, length = 20)
    private String status;                  // 状态: pending/processing/completed/cancelled

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (status == null) {
            status = "pending";
        }
    }
    
    // 虚拟字段：返回销售员ID（用于前端过滤）
    @JsonProperty("salesmanId")
    public Long getSalesmanId() {
        return salesman != null ? salesman.getId() : null;
    }
    
    @JsonProperty("salesmanId")
    public void setSalesmanId(Long salesmanId) {
        if (salesmanId != null) {
            if (salesman == null) {
                salesman = new Salesman();
            }
            salesman.setId(salesmanId);
        }
    }
    
    // 虚拟字段：返回客户ID（用于前端过滤）
    @JsonProperty("customerId")
    public Long getCustomerId() {
        return customer != null ? customer.getId() : null;
    }
    
    @JsonProperty("customerId")
    public void setCustomerId(Long customerId) {
        if (customerId != null) {
            if (customer == null) {
                customer = new Customer();
            }
            customer.setId(customerId);
        }
    }
}


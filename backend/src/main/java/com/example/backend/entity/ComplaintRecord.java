package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * 客户投诉风险记录实体
 */
@Data
@Entity
@Table(name = "complaint_record")
public class ComplaintRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "complaint_no", nullable = false, unique = true, length = 50)
    private String complaintNo;             // 投诉单号 (CMP-yyyyMMdd-xxx)

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;              // 投诉的客户

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "salesman_id", nullable = false)
    private Salesman salesman;              // 负责处理的销售员

    @Column(name = "related_order_no", length = 50)
    private String relatedOrderNo;          // 关联订单号

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "related_product_id")
    private Product relatedProduct;         // 关联产品

    @Column(name = "complaint_type", nullable = false, length = 20)
    private String complaintType;           // 类型: quality/attitude/logistics/price
    
    @Column(length = 10, nullable = false)
    private String severity;                // 严重等级: Low/Normal/High/Critical

    @Column(length = 1000, nullable = false)
    private String content;                 // 投诉详情描述

    @Column(name = "evidence_image", length = 255)
    private String evidenceImage;           // 图片凭证路径

    @Column(length = 1000)
    private String solution;                // 处理方案

    @Column(name = "customer_feedback", length = 200)
    private String customerFeedback;        // 客户反馈

    @Column(length = 20, nullable = false)
    private String status;                  // pending/processing/resolved/closed

    @Column(name = "complaint_time", nullable = false)
    private LocalDateTime complaintTime;    // 投诉时间

    @Column(name = "resolve_time")
    private LocalDateTime resolveTime;      // 解决时间

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (complaintTime == null) {
            complaintTime = LocalDateTime.now();
        }
        if (status == null) status = "pending";
        if (severity == null) severity = "Normal";
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
    
    // 虚拟字段：用于JSON序列化/反序列化销售员ID
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
    
    // 虚拟字段：用于JSON序列化/反序列化客户ID
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

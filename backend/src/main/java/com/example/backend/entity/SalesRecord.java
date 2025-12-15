package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sales_record")
public class SalesRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String orderNo;

    // --- 销售员关联 ---
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "salesman_id", nullable = false)
    private Salesman salesman;

    // --- 产品关联 ---
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // --- 客户关联 ---
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "commission_rate", precision = 5, scale = 2)
    private BigDecimal commissionRate;

    @Column(name = "commission_amount", precision = 10, scale = 2)
    private BigDecimal commissionAmount;

    @Column(name = "sale_date", nullable = false)
    private LocalDate saleDate;

    @Column(length = 500)
    private String remark;

    @Column(length = 20, nullable = false)
    private String status = "draft";

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "approved_by", length = 50)
    private String approvedBy;

    @Column(name = "approved_time")
    private LocalDateTime approvedTime;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Transient
    @JsonProperty("fromWithdrawn")
    private boolean fromWithdrawn = false; // 是否由撤回回到草稿（仅用于响应，不入库）

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        calculateCommission();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
        calculateCommission();
    }

    private void calculateCommission() {
        if (totalAmount != null && commissionRate != null) {
            this.commissionAmount = totalAmount.multiply(commissionRate);
        }
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
    
    // 虚拟字段：用于JSON序列化/反序列化产品ID
    @JsonProperty("productId")
    public Long getProductId() {
        return product != null ? product.getId() : null;
    }
    
    @JsonProperty("productId")
    public void setProductId(Long productId) {
        if (productId != null) {
            if (product == null) {
                product = new Product();
            }
            product.setId(productId);
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

    public boolean isFromWithdrawn() {
        return fromWithdrawn;
    }

    public void setFromWithdrawn(boolean fromWithdrawn) {
        this.fromWithdrawn = fromWithdrawn;
    }
}
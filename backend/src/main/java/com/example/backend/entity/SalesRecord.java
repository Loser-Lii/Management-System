package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 销售记录实体
 */
@Data
@Entity
@Table(name = "sales_record")
public class SalesRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "salesman_id", nullable = false)
    private Salesman salesman;              // 销售员

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;                // 产品

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;              // 客户

    @Column(nullable = false)
    private Integer quantity;               // 销售数量

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;           // 单价

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;         // 总金额

    @Column(name = "commission_rate", precision = 5, scale = 2)
    private BigDecimal commissionRate;      // 提成比例

    @Column(name = "commission_amount", precision = 10, scale = 2)
    private BigDecimal commissionAmount;    // 提成金额（自动计算）

    @Column(name = "sale_date", nullable = false)
    private LocalDate saleDate;             // 销售日期

    @Column(length = 500)
    private String remark;                  // 备注

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

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

    // 自动计算提成
    private void calculateCommission() {
        if (totalAmount != null && commissionRate != null) {
            this.commissionAmount = totalAmount.multiply(commissionRate);
        }
    }
}

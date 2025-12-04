package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 催款记录实体
 */
@Data
@Entity
@Table(name = "collection_record")
public class CollectionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "salesman_id", nullable = false)
    private Salesman salesman;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sales_record_id")
    private SalesRecord salesRecord;        // 关联的销售记录

    @Column(name = "collection_date", nullable = false)
    private LocalDate collectionDate;       // 催款日期

    @Column(name = "collection_method", length = 20)
    private String collectionMethod;        // 催款方式

    @Column(name = "amount_due", precision = 10, scale = 2)
    private BigDecimal amountDue;           // 应收金额

    @Column(name = "amount_received", precision = 10, scale = 2)
    private BigDecimal amountReceived;      // 已收金额

    @Column(length = 20)
    private String status;                  // 回款状态：未回款/部分回款/已回款

    @Column(name = "due_date")
    private LocalDate dueDate;              // 应收日期

    @Column(length = 500)
    private String remark;

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (amountReceived == null) {
            amountReceived = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}

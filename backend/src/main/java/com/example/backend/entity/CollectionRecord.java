package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @Column(name = "collection_no", nullable = false, length = 50, unique = true)
    private String collectionNo;            // 催款单号 (COL-xxx)

    @Column(name = "order_no", nullable = false, length = 50)
    private String orderNo;                 // 关联销售订单号

    // ✅ 使用 EAGER 加载，避免 LAZY 代理序列化问题
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;              // 客户

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "salesman_id", nullable = false)
    private Salesman salesman;              // 负责人

    @Column(name = "collection_date", nullable = false)
    private LocalDate collectionDate;       // 催款日期

    @Column(name = "current_amount", precision = 10, scale = 2)
    private BigDecimal currentAmount;       // 本次回款金额

    @Column(name = "remaining_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal remainingAmount;     // 剩余待回款金额

    @Column(name = "collection_status", length = 20, insertable = false, updatable = false)
    private String collectionStatus;        // 状态: completed(已完成), pending(待回款) - 数据库自动生成

    @Column(length = 500)
    private String remark;                  // 备注

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (currentAmount == null) {
            currentAmount = BigDecimal.ZERO;
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

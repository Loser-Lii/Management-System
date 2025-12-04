package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "salesman_id", nullable = false)
    private Salesman salesman;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;          // 服务日期

    @Column(name = "service_type", length = 50)
    private String serviceType;             // 服务类型

    @Column(length = 1000)
    private String content;                 // 服务内容

    @Column(name = "satisfaction_rating")
    private Integer satisfactionRating;     // 满意度评分（1-5）

    @Column(length = 500)
    private String customerFeedback;        // 客户反馈

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
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}

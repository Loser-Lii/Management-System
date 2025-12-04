package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 投诉记录实体
 */
@Data
@Entity
@Table(name = "complaint_record")
public class ComplaintRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;              // 投诉客户

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "salesman_id", nullable = false)
    private Salesman salesman;              // 被投诉销售员

    @Column(name = "complaint_date", nullable = false)
    private LocalDate complaintDate;        // 投诉日期

    @Column(name = "complaint_type", length = 50)
    private String complaintType;           // 投诉类型

    @Column(length = 1000)
    private String content;                 // 投诉内容

    @Column(length = 50)
    private String handler;                 // 处理人员

    @Column(length = 20)
    private String status;                  // 处理状态：待处理/处理中/已处理

    @Column(length = 1000)
    private String result;                  // 处理结果

    @Column(name = "handle_date")
    private LocalDate handleDate;           // 处理日期

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
        if (status == null) {
            status = "待处理";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}

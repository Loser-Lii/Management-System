package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 联络记录实体
 */
@Data
@Entity
@Table(name = "contact_record")
public class ContactRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "salesman_id", nullable = false)
    private Salesman salesman;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "contact_time", nullable = false)
    private LocalDateTime contactTime;      // 联络时间

    @Column(name = "contact_method", length = 20)
    private String contactMethod;           // 联络方式：电话/邮件/微信/面谈

    @Column(length = 1000)
    private String summary;                 // 沟通摘要

    @Column(length = 500)
    private String feedback;                // 客户反馈

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

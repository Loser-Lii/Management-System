package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品实体
 */
@Data
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String productNo;               // 产品编号

    @Column(nullable = false, length = 100)
    private String name;                    // 产品名称

    @Column(length = 50)
    private String category;                // 产品类别

    @Column(length = 50)
    private String specification;           // 规格

    @Column(length = 20)
    private String unit;                    // 单位

    @Column(precision = 10, scale = 2)
    private BigDecimal price;               // 单价

    @Column(length = 500)
    private String description;             // 产品描述

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

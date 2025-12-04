package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 客户实体
 */
@Data
@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;                    // 客户名称

    @Column(length = 20)
    private String contactPerson;           // 联系人

    @Column(length = 20)
    private String phone;                   // 联系电话

    @Column(length = 100)
    private String email;                   // 邮箱

    @Column(length = 200)
    private String address;                 // 地址

    @Column(length = 50)
    private String customerType;            // 客户类型：医院/诊所/药店等

    @Column(length = 20)
    private String level;                   // 客户等级：A/B/C

    @Column(name = "salesman_id")
    private Long salesmanId;                // 负责的销售员ID

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
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}

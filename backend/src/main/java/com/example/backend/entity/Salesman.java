package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 销售员实体
 */
@Data
@Entity
@Table(name = "salesman")
public class Salesman {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;                    // 姓名

    @Column(nullable = false, unique = true, length = 20)
    private String employeeNo;              // 员工编号

    @Column(length = 20)
    private String phone;                   // 联系电话

    @Column(length = 50)
    private String qq;                      // QQ号

    @Column(length = 50)
    private String wechat;                  // 微信号

    @Column(length = 255)
    private String avatar;                  // 头像URL

    @Column(length = 100)
    private String email;                   // 邮箱

    @Column(name = "hire_date")
    private LocalDate hireDate;             // 入职日期

    @Column(length = 20)
    private String level;                   // 等级：junior(初级4%), intermediate(中级5%), senior(高级6%), expert(资深8%)

    @Column(name = "commission_rate", precision = 5, scale = 4)
    private BigDecimal commissionRate;      // 基础提成比例（如0.04表示4%）

    @Column(length = 20)
    private String status;                  // 状态：active(在职)/resigned(离职)

    @Column(name = "resignation_date")
    private LocalDate resignationDate;      // 离职日期

    @Column(length = 500)
    private String remark;                  // 备注

    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;       // 创建时间

    @Column(name = "update_time")
    private LocalDateTime updateTime;       // 更新时间

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        if (status == null) {
            status = "active";  // 默认在职状态
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}

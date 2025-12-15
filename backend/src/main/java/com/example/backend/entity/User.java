package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;  // 用户名

    @Column(nullable = false, length = 255)
    private String password;  // 密码（加密存储）

    @Column(nullable = false, length = 20)
    private String role;  // 角色：admin 或 salesman

    @Column(name = "salesman_id")
    private Long salesmanId;  // 关联的销售员ID（如果是销售员角色）

    @Column(name = "active_token", length = 100)
    private String activeToken;  // 当前会话的Token

    @Column(name = "is_logged_in")
    private Boolean loggedIn = false; // 是否已登录（限制重复登录）

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime; // 最近登录时间

    @Column(name = "create_time")
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

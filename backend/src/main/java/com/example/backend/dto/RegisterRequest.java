package com.example.backend.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String role;  // admin 或 salesman
    private String adminKey;  // 管理员注册密钥（仅管理员注册时需要）
    private String name;  // 销售员姓名（仅销售员注册时需要）
}

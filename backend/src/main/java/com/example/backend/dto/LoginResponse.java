package com.example.backend.dto;

import lombok.Data;

/**
 * 登录响应DTO
 */
@Data
public class LoginResponse {
    private String token;
    private Long salesmanId;
    private Long userId;
    private String role;
    private String name;
    private String username;
    private Boolean needCompleteProfile;  // 是否需要完善联系方式
}

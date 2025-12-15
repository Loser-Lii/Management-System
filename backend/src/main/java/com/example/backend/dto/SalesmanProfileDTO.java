package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 销售员档案 DTO - 映射自 view_salesman_profile
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesmanProfileDTO {
    
    private Long id;
    
    private String employeeNo;
    
    private String name;
    
    private String avatar;
    
    // 职业信息
    private String level;
    
    private BigDecimal commissionRate;
    
    private LocalDate hireDate;
    
    private String status;
    
    // 联系方式
    private String phone;
    
    private String email;
    
    private String wechat;
    
    private String qq;
    
    // 备注
    private String remark;
    
    // 关联用户信息
    private Long linkedUserId;
    
    private String loginUsername;
    
    // 时间戳
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
}

package com.example.backend.dto;

import lombok.Data;

/**
 * 销售员档案更新表单
 * 用于更新销售员可编辑的字段
 */
@Data
public class SalesmanUpdateForm {
    
    private String phone;
    
    private String email;
    
    private String wechat;
    
    private String qq;
    
    private String avatar;
    
    private String remark;
}

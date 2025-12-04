package com.example.backend.controller;

import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.LoginResponse;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.entity.Salesman;
import com.example.backend.entity.User;
import com.example.backend.repository.SalesmanRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SalesmanRepository salesmanRepository;

    // 管理员注册密钥（实际项目应该放在配置文件中）
    private static final String ADMIN_REGISTER_KEY = "RHODESISLAND2024";

    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterRequest request) {
        // 验证用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            return Result.error("用户名已存在");
        }

        // 验证必填字段
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }
        if (request.getRole() == null || request.getRole().trim().isEmpty()) {
            return Result.error("角色不能为空");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());  // 实际项目应该加密
        user.setRole(request.getRole());

        if ("admin".equals(request.getRole())) {
            // 管理员注册需要验证密钥
            if (!ADMIN_REGISTER_KEY.equals(request.getAdminKey())) {
                return Result.error("管理员注册密钥错误");
            }
            userRepository.save(user);
            return Result.success("管理员注册成功", null);

        } else if ("salesman".equals(request.getRole())) {
            // 销售员注册需要创建销售员记录
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return Result.error("销售员姓名不能为空");
            }

            // 创建销售员记录
            Salesman salesman = new Salesman();
            salesman.setName(request.getName());
            salesman.setEmployeeNo("EMP" + System.currentTimeMillis());  // 自动生成员工编号
            salesman.setHireDate(LocalDate.now());  // 注册日期作为入职日期
            salesman.setLevel("junior");  // 默认初级销售员
            salesman.setCommissionRate(new BigDecimal("0.04"));  // 4%提成率
            Salesman savedSalesman = salesmanRepository.save(salesman);

            // 关联用户和销售员
            user.setSalesmanId(savedSalesman.getId());
            userRepository.save(user);

            return Result.success("销售员注册成功", null);
        } else {
            return Result.error("无效的角色类型");
        }
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        // 查找用户
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);
        if (user == null || !user.getPassword().equals(request.getPassword())) {
            return Result.error("用户名或密码错误");
        }

        LoginResponse response = new LoginResponse();
        response.setToken(UUID.randomUUID().toString());
        response.setUserId(user.getId());
        response.setRole(user.getRole());
        response.setUsername(user.getUsername());

        if ("salesman".equals(user.getRole()) && user.getSalesmanId() != null) {
            Salesman salesman = salesmanRepository.findById(user.getSalesmanId()).orElse(null);
            if (salesman != null) {
                response.setSalesmanId(salesman.getId());
                response.setName(salesman.getName());
                
                // 检查是否需要完善联系方式
                boolean needComplete = (salesman.getQq() == null || salesman.getQq().trim().isEmpty()) &&
                                      (salesman.getWechat() == null || salesman.getWechat().trim().isEmpty()) &&
                                      (salesman.getPhone() == null || salesman.getPhone().trim().isEmpty());
                response.setNeedCompleteProfile(needComplete);
            }
        } else if ("admin".equals(user.getRole())) {
            response.setName("管理员");
            response.setNeedCompleteProfile(false);
        }

        return Result.success("登录成功", response);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success("退出成功", null);
    }
}

package com.example.backend.controller;

import com.example.backend.dto.PerformanceSuggestionDTO;
import com.example.backend.entity.Salesman;
import com.example.backend.service.SalesmanService;
import com.example.backend.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 销售员控制器
 */
@RestController
@RequestMapping("/api/salesmen")
@CrossOrigin(origins = "http://localhost:5173")
public class SalesmanController {

    @Autowired
    private SalesmanService salesmanService;

    // 查询所有销售员
    @GetMapping
    public Result<List<Salesman>> findAll() {
        return Result.success(salesmanService.findAll());
    }

    // 根据ID查询
    @GetMapping("/{id}")
    public Result<Salesman> findById(@PathVariable Long id) {
        return Result.success(salesmanService.findById(id));
    }

    // 搜索销售员（姓名或编号模糊查询）
    @GetMapping("/search")
    public Result<List<Salesman>> search(@RequestParam(required = false) String keyword) {
        return Result.success(salesmanService.search(keyword));
    }

    // 升职操作
    @PostMapping("/{id}/promote")
    public Result<Salesman> promote(@PathVariable Long id) {
        return Result.success("升职成功", salesmanService.promote(id));
    }

    // 降职操作
    @PostMapping("/{id}/demote")
    public Result<Salesman> demote(@PathVariable Long id) {
        return Result.success("降职成功", salesmanService.demote(id));
    }

    // 获取职位变化建议
    @GetMapping("/performance-suggestion")
    public Result<PerformanceSuggestionDTO> getPerformanceSuggestion() {
        return Result.success(salesmanService.getPerformanceSuggestion());
    }

    // 创建销售员
    @PostMapping
    public Result<Salesman> create(@RequestBody Salesman salesman) {
        return Result.success("创建成功", salesmanService.create(salesman));
    }

    // 更新销售员
    @PutMapping("/{id}")
    public Result<Salesman> update(@PathVariable Long id, @RequestBody Salesman salesman) {
        return Result.success("更新成功", salesmanService.update(id, salesman));
    }

    // 更新销售员联系方式（销售员个人使用）
    @PatchMapping("/{id}/contact")
    public Result<Salesman> updateContact(@PathVariable Long id, @RequestBody Salesman salesman) {
        Salesman existing = salesmanService.findById(id);
        if (existing == null) {
            return Result.error("销售员不存在");
        }
        
        // 只允许更新联系方式和头像
        if (salesman.getPhone() != null) existing.setPhone(salesman.getPhone());
        if (salesman.getQq() != null) existing.setQq(salesman.getQq());
        if (salesman.getWechat() != null) existing.setWechat(salesman.getWechat());
        if (salesman.getEmail() != null) existing.setEmail(salesman.getEmail());
        if (salesman.getAvatar() != null) existing.setAvatar(salesman.getAvatar());
        
        return Result.success("联系方式更新成功", salesmanService.update(id, existing));
    }

    // 删除销售员
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        salesmanService.delete(id);
        return Result.success("删除成功", null);
    }
}

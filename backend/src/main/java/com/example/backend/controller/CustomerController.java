package com.example.backend.controller;

import com.example.backend.dto.CustomerDTO;
import com.example.backend.entity.Customer;
import com.example.backend.service.CustomerService;
import com.example.backend.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "http://localhost:5173")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    // 新增：获取带销售员名称的客户列表
    @GetMapping("/with-salesman")
    public Result<List<CustomerDTO>> findAllWithSalesman(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long salesmanId,
            @RequestParam(required = false) String level) {
        List<CustomerDTO> customers = customerService.findAllWithSalesman();
        
        // 按角色和销售员筛选
        if ("salesman".equals(role) && salesmanId != null) {
            customers = customers.stream()
                    .filter(c -> salesmanId.equals(c.getSalesmanId()))
                    .collect(java.util.stream.Collectors.toList());
        } else if (salesmanId != null) {
            customers = customers.stream()
                    .filter(c -> salesmanId.equals(c.getSalesmanId()))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        // 按等级筛选
        if (level != null && !level.trim().isEmpty()) {
            customers = customers.stream()
                    .filter(c -> level.equals(c.getLevel()))
                    .collect(java.util.stream.Collectors.toList());
        }
        
        return Result.success(customers);
    }

    @GetMapping
    public Result<List<Customer>> findAll(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long salesmanId) {
        // 如果是销售员角色，只返回该销售员的客户
        if ("salesman".equals(role) && salesmanId != null) {
            return Result.success(customerService.findBySalesmanId(salesmanId));
        }
        // 管理员可以选择性地按销售员筛选，也可以查看全部
        if (salesmanId != null) {
            return Result.success(customerService.findBySalesmanId(salesmanId));
        }
        return Result.success(customerService.findAll());
    }

    @GetMapping("/{id}")
    public Result<Customer> findById(@PathVariable Long id) {
        return Result.success(customerService.findById(id));
    }

    @GetMapping("/search")
    public Result<List<Customer>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long salesmanId) {
        // 如果是销售员角色，只搜索该销售员的客户
        if ("salesman".equals(role) && salesmanId != null) {
            return Result.success(customerService.searchBySalesman(salesmanId, keyword));
        }
        // 管理员可以选择性地按销售员筛选搜索
        if (salesmanId != null) {
            return Result.success(customerService.searchBySalesman(salesmanId, keyword));
        }
        return Result.success(customerService.search(keyword));
    }

    @GetMapping("/type/{type}")
    public Result<List<Customer>> findByType(@PathVariable String type) {
        return Result.success(customerService.findByType(type));
    }

    @GetMapping("/level/{level}")
    public Result<List<Customer>> findByLevel(@PathVariable String level) {
        return Result.success(customerService.findByLevel(level));
    }

    // 查询无负责人的客户
    @GetMapping("/without-salesman")
    public Result<List<Customer>> findWithoutSalesman() {
        return Result.success(customerService.findWithoutSalesman());
    }

    @PostMapping
    public Result<Customer> create(@RequestBody Customer customer) {
        return Result.success("创建成功", customerService.create(customer));
    }

    @PutMapping("/{id}")
    public Result<Customer> update(@PathVariable Long id, @RequestBody Customer customer) {
        return Result.success("更新成功", customerService.update(id, customer));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return Result.success("删除成功", null);
    }

    // 解绑客户（将负责销售员设为空）
    @PutMapping("/{id}/unbind")
    public Result<Customer> unbindSalesman(@PathVariable Long id) {
        return Result.success("客户已解绑", customerService.unbindSalesman(id));
    }
}

package com.example.backend.controller;

import com.example.backend.entity.Product;
import com.example.backend.service.ProductService;
import com.example.backend.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:5173")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public Result<List<Product>> findAll() {
        return Result.success(productService.findAll());
    }

    @GetMapping("/{id}")
    public Result<Product> findById(@PathVariable Long id) {
        return Result.success(productService.findById(id));
    }

    @GetMapping("/search")
    public Result<List<Product>> search(@RequestParam(required = false) String keyword) {
        return Result.success(productService.search(keyword));
    }

    @GetMapping("/category/{category}")
    public Result<List<Product>> findByCategory(@PathVariable String category) {
        return Result.success(productService.findByCategory(category));
    }

    @PostMapping
    public Result<Product> create(@RequestBody Product product) {
        return Result.success("创建成功", productService.create(product));
    }

    @PutMapping("/{id}")
    public Result<Product> update(@PathVariable Long id, @RequestBody Product product) {
        return Result.success("更新成功", productService.update(id, product));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.success("删除成功", null);
    }
}

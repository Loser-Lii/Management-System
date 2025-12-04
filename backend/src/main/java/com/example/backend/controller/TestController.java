package com.example.backend.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5173") // 允许前端访问（解决跨域）
public class TestController {

    @GetMapping("/api/test")
    public Map<String, Object> test() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Hello from Spring Boot!");
        result.put("time", LocalDateTime.now().toString());
        return result;
    }
}

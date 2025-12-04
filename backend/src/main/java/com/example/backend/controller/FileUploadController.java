package com.example.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/upload")
@CrossOrigin
public class FileUploadController {

    // 文件上传目录（可以在application.properties中配置）
    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public ResponseEntity<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 验证文件
            if (file.isEmpty()) {
                response.put("code", 400);
                response.put("message", "文件不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("code", 400);
                response.put("message", "只能上传图片文件");
                return ResponseEntity.badRequest().body(response);
            }

            // 验证文件大小（限制5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                response.put("code", 400);
                response.put("message", "文件大小不能超过5MB");
                return ResponseEntity.badRequest().body(response);
            }

            // 创建上传目录
            File uploadDir = new File(uploadPath + "/avatars");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = Paths.get(uploadDir.getAbsolutePath(), filename);
            Files.write(filePath, file.getBytes());

            // 返回文件URL（这里返回相对路径，前端需要拼接服务器地址）
            String fileUrl = "/uploads/avatars/" + filename;

            response.put("code", 200);
            response.put("message", "上传成功");
            response.put("data", fileUrl);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("code", 500);
            response.put("message", "文件上传失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 删除头像
     */
    @DeleteMapping("/avatar")
    public ResponseEntity<Map<String, Object>> deleteAvatar(@RequestParam("url") String url) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 从URL中提取文件名
            String filename = url.substring(url.lastIndexOf("/") + 1);
            File file = new File(uploadPath + "/avatars/" + filename);
            
            if (file.exists()) {
                file.delete();
                response.put("code", 200);
                response.put("message", "删除成功");
            } else {
                response.put("code", 404);
                response.put("message", "文件不存在");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "删除失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}

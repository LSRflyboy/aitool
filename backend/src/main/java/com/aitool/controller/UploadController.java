package com.aitool.controller;

import com.aitool.service.StorageService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final StorageService storageService;
    private static final long MAX_FILE_SIZE = 500 * 1024 * 1024; // 500MB

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        log.info("接收到测试请求");
        return ResponseEntity.ok(Map.of(
            "status", "ok",
            "message", "服务正常运行",
            "timestamp", System.currentTimeMillis()
        ));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> upload(@RequestPart("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "上传的文件不能为空");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new MaxUploadSizeExceededException(MAX_FILE_SIZE);
        }
        
        log.info("接收文件上传请求: {} (大小: {}MB)", 
                file.getOriginalFilename(), file.getSize() / (1024.0 * 1024.0));
        
        String id = storageService.saveMultipartFile(file);
        
        log.info("文件上传成功: {} (ID: {})", file.getOriginalFilename(), id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", id, "filename", file.getOriginalFilename(), "size", file.getSize()));
    }

    @PostMapping("/remote")
    public ResponseEntity<Map<String, Object>> remoteFetch(@RequestParam("url") @NotBlank String url) throws IOException {
        log.info("接收远程下载请求: {}", url);
        
        String id = storageService.saveRemoteFile(url);
        
        log.info("远程文件下载成功: {} (ID: {})", url, id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", id, "sourceUrl", url));
    }
} 
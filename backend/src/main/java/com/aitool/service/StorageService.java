package com.aitool.service;

import com.aitool.config.StorageProperties;
import com.aitool.model.FileRecord;
import com.aitool.model.FileStatus;
import com.aitool.repository.FileRecordRepository;
import com.aitool.service.ParseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private final StorageProperties properties;
    private final FileRecordRepository repository;
    private final ParseService parseService;

    /**
     * Save uploaded multipart file to local storage.
     */
    public String saveMultipartFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传的文件不能为空");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            filename = "unknown_file";
        }
        
        log.info("开始处理上传文件: {} (大小: {}KB)", filename, file.getSize() / 1024);
        String uuid = UUID.randomUUID().toString();
        Path dir = prepareDir(uuid);
        String cleanName = StringUtils.cleanPath(filename);
        Path target = dir.resolve(cleanName);
        
        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            log.info("文件保存成功: {} (UUID: {})", cleanName, uuid);
        } catch (IOException e) {
            log.error("文件保存失败: {}", cleanName, e);
            throw new IOException("文件保存失败: " + e.getMessage(), e);
        }
        repository.save(FileRecord.builder()
                .uuid(uuid)
                .filename(cleanName)
                .storagePath(target.toString())
                .status(FileStatus.STORED)
                .createdAt(LocalDateTime.now())
                .build());
        log.info("文件已保存 ({}), 开始异步解析", uuid);
        parseService.extractAndParseAsync(uuid);
        return uuid;
    }

    /**
     * Download remote file and save.
     */
    public String saveRemoteFile(String urlStr) throws IOException {
        if (urlStr == null || urlStr.isBlank()) {
            throw new IllegalArgumentException("下载URL不能为空");
        }
        
        log.info("开始远程下载文件: {}", urlStr);
        String uuid = UUID.randomUUID().toString();
        Path dir = prepareDir(uuid);
        
        String fileName;
        Path target;
        try {
            URL url = new URL(urlStr);
            fileName = Path.of(url.getPath()).getFileName().toString();
            if (fileName.isBlank()) {
                fileName = "remote_file_" + System.currentTimeMillis();
            }
            
            target = dir.resolve(fileName);
            
            // 设置超时，连接10秒，读取5分钟
            FileUtils.copyURLToFile(url, target.toFile(), 10000, 300000);
            log.info("远程文件下载成功: {} -> {} (UUID: {})", urlStr, fileName, uuid);
        } catch (IOException e) {
            log.error("远程文件下载失败: {}", urlStr, e);
            throw new IOException("远程文件下载失败: " + e.getMessage(), e);
        }
        repository.save(FileRecord.builder()
                .uuid(uuid)
                .filename(fileName)
                .storagePath(target.toString())
                .status(FileStatus.STORED)
                .createdAt(LocalDateTime.now())
                .build());
        log.info("文件已保存 ({}), 开始异步解析", uuid);
        parseService.extractAndParseAsync(uuid);
        return uuid;
    }

    private Path prepareDir(String uuid) throws IOException {
        Path root = Path.of(properties.rootDir());
        if (!Files.exists(root)) {
            Files.createDirectories(root);
        }
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Path dir = root.resolve(datePrefix).resolve(uuid);
        Files.createDirectories(dir);
        return dir;
    }
} 
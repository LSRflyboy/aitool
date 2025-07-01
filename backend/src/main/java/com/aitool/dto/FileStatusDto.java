package com.aitool.dto;

import com.aitool.model.FileStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FileStatusDto {
    private String uuid;
    private String filename;
    private FileStatus status;
    private LocalDateTime createdAt;
    private String message;
    private long logCount;
} 
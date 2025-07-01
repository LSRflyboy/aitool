package com.aitool.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String uuid;

    private String filename;

    private String storagePath;

    private String extractedPath;

    @Enumerated(EnumType.STRING)
    private FileStatus status;

    private LocalDateTime createdAt;

    private String message;
} 
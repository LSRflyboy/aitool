package com.aitool.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "log_entry", indexes = {
        @Index(columnList = "timestamp"),
        @Index(columnList = "level"),
        @Index(columnList = "tag")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_record_id")
    private FileRecord fileRecord;

    private LocalDateTime timestamp;

    private String level;

    private String tag;

    @Column(length = 2048)
    private String message;

    @Column(length = 4096)
    private String rawLine;
} 
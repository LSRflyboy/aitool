package com.aitool.controller;

import com.aitool.dto.FileStatusDto;
import com.aitool.model.FileRecord;
import com.aitool.repository.FileRecordRepository;
import com.aitool.repository.LogEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class FileRecordController {

    private final FileRecordRepository repository;
    private final LogEntryRepository logRepo;

    @GetMapping("/{uuid}/status")
    public FileStatusDto status(@PathVariable String uuid) {
        FileRecord rec = repository.findByUuid(uuid).orElseThrow();
        long count = logRepo.count((root, q, cb) -> cb.equal(root.get("fileRecord"), rec));
        return FileStatusDto.builder()
                .uuid(rec.getUuid())
                .filename(rec.getFilename())
                .status(rec.getStatus())
                .createdAt(rec.getCreatedAt())
                .message(rec.getMessage())
                .logCount(count)
                .build();
    }

    @GetMapping("/{uuid}/logs")
    public Page<Map<String, Object>> logs(@PathVariable String uuid,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "100") int size) {
        FileRecord rec = repository.findByUuid(uuid).orElseThrow();
        Pageable pageable = PageRequest.of(page, size);
        return logRepo.findByFileRecord(rec, pageable)
                .map(e -> Map.of(
                        "timestamp", e.getTimestamp(),
                        "level", e.getLevel(),
                        "tag", e.getTag(),
                        "message", e.getMessage()
                ));
    }
} 
package com.aitool.controller;

import com.aitool.model.FileRecord;
import com.aitool.model.FileStatus;
import com.aitool.repository.FileRecordRepository;
import com.aitool.repository.LogEntryRepository;
import com.aitool.dto.LogQuery;
import com.aitool.service.LogQueryService;
import com.aitool.service.ParseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileRecordRepository recordRepo;
    private final LogEntryRepository logRepo;
    private final LogQueryService logQueryService;
    private final ParseService parseService;

    @GetMapping("/{uuid}")
    public ResponseEntity<?> getStatus(@PathVariable String uuid) {
        return recordRepo.findByUuid(uuid)
                .<ResponseEntity<?>>map(record -> {
                    var map = new java.util.LinkedHashMap<String, Object>();
                    map.put("uuid", uuid);
                    map.put("filename", record.getFilename());
                    map.put("status", record.getStatus());
                    map.put("createdAt", record.getCreatedAt());
                    if (record.getMessage() != null) {
                        map.put("message", record.getMessage());
                    }
                    return ResponseEntity.ok(map);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{uuid}/logs")
    public ResponseEntity<?> getLogs(@PathVariable String uuid,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "100") int size,
                                     @RequestParam(required = false) String level,
                                     @RequestParam(required = false) String tag,
                                     @RequestParam(required = false) String from,
                                     @RequestParam(required = false) String to) {
        return recordRepo.findByUuid(uuid)
                .<ResponseEntity<?>>map(record -> {
                    PageRequest pr = PageRequest.of(page, size, Sort.by("timestamp").ascending());
                    LogQuery q = new LogQuery(level, tag,
                            from != null ? LocalDateTime.parse(from) : null,
                            to != null ? LocalDateTime.parse(to) : null);
                    Page<?> logPage = logQueryService.query(record, q, pr).map(le -> Map.of(
                            "timestamp", le.getTimestamp(),
                            "level", le.getLevel(),
                            "tag", le.getTag(),
                            "message", le.getMessage(),
                            "rawLine", le.getRawLine()
                    ));
                    return ResponseEntity.ok(Map.of(
                            "total", logPage.getTotalElements(),
                            "pages", logPage.getTotalPages(),
                            "data", logPage.getContent()
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<?> listFiles() {
        return ResponseEntity.ok(recordRepo.findAll(Sort.by("createdAt").descending()).stream().map(r -> Map.of(
                "uuid", r.getUuid(),
                "filename", r.getFilename(),
                "status", r.getStatus(),
                "createdAt", r.getCreatedAt()
        )).toList());
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> delete(@PathVariable String uuid) {
        return recordRepo.findByUuid(uuid)
                .map(rec -> {
                    // 删除日志条目
                    logRepo.deleteByFileRecord(rec);
                    // 删除文件记录
                    recordRepo.delete(rec);
                    // 删除存储目录
                    if (rec.getStoragePath() != null) {
                        Path p = Paths.get(rec.getStoragePath()).getParent();
                        try {
                            if (Files.exists(p)) {
                                Files.walk(p)
                                        .sorted((a,b)->b.compareTo(a)) // delete children first
                                        .forEach(path -> {
                                            try { Files.deleteIfExists(path); } catch (Exception ignored) {}
                                        });
                            }
                        } catch (Exception ignored) {}
                    }
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Trigger asynchronous parsing for a stored file.
     */
    @PostMapping("/{uuid}/parse")
    public ResponseEntity<?> triggerParse(@PathVariable String uuid) {
        return recordRepo.findByUuid(uuid)
                .<ResponseEntity<?>>map(rec -> {
                    parseService.extractAndParseAsync(uuid);
                    return ResponseEntity.accepted().body(Map.of("message", "Parse started"));
                })
                .orElse(ResponseEntity.notFound().build());
    }
} 
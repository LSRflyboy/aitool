package com.aitool.controller;

import com.aitool.model.FileRecord;
import com.aitool.repository.FileRecordRepository;
import com.aitool.repository.LogEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class BatchLogController {

    private final FileRecordRepository fileRepo;
    private final LogEntryRepository logRepo;

    @GetMapping
    public ResponseEntity<?> batchLogs(@RequestParam List<String> uuids,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "1000") int size,
                                        @RequestParam(required = false) String level,
                                        @RequestParam(required = false) String tag,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        if (uuids.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "uuids param required"));
        }
        List<FileRecord> recs = fileRepo.findByUuidIn(uuids);
        if (recs.isEmpty()) {
            return ResponseEntity.ok(Map.of("total", 0, "pages", 0, "data", List.of()));
        }
        Specification<com.aitool.model.LogEntry> spec = (root, q, cb) -> {
            var predicates = cb.conjunction();
            predicates.getExpressions().add(root.get("fileRecord").in(recs));
            if (level != null && !level.isBlank()) {
                predicates.getExpressions().add(cb.equal(root.get("level"), level));
            }
            if (tag != null && !tag.isBlank()) {
                predicates.getExpressions().add(cb.equal(root.get("tag"), tag));
            }
            if (from != null) {
                predicates.getExpressions().add(cb.greaterThanOrEqualTo(root.get("timestamp"), from));
            }
            if (to != null) {
                predicates.getExpressions().add(cb.lessThanOrEqualTo(root.get("timestamp"), to));
            }
            return predicates;
        };
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").ascending());
        Page<com.aitool.model.LogEntry> p = logRepo.findAll(spec, pageable);
        return ResponseEntity.ok(Map.of(
                "total", p.getTotalElements(),
                "pages", p.getTotalPages(),
                "data", p.getContent().stream().map(e -> Map.of(
                        "timestamp", e.getTimestamp(),
                        "level", e.getLevel(),
                        "tag", e.getTag(),
                        "message", e.getMessage(),
                        "rawLine", e.getRawLine()
                )).toList()
        ));
    }
} 
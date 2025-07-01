package com.aitool.service;

import com.aitool.dto.LogQuery;
import com.aitool.model.FileRecord;
import com.aitool.model.LogEntry;
import com.aitool.repository.LogEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LogQueryService {

    private final LogEntryRepository repo;

    public Page<LogEntry> query(FileRecord record, LogQuery q, Pageable pageable) {
        if (q == null || (q.level() == null && q.tag() == null && q.from() == null && q.to() == null)) {
            return repo.findByFileRecord(record, pageable);
        }
        return repo.findAll((root, query, cb) -> {
            var predicates = cb.conjunction();
            predicates = cb.and(predicates, cb.equal(root.get("fileRecord"), record));
            if (Objects.nonNull(q.level())) {
                // 处理级别的简写和全称对应关系
                String level = q.level();
                String shortLevel = null;
                
                // 从全称映射到简写
                if ("Error".equalsIgnoreCase(level)) {
                    shortLevel = "E";
                } else if ("Warn".equalsIgnoreCase(level)) {
                    shortLevel = "W";
                } else if ("Info".equalsIgnoreCase(level)) {
                    shortLevel = "I";
                } else if ("Debug".equalsIgnoreCase(level)) {
                    shortLevel = "D";
                }
                
                // 如果有对应的简写，创建OR条件
                if (shortLevel != null) {
                    predicates = cb.and(predicates, 
                        cb.or(
                            cb.equal(root.get("level"), level),
                            cb.equal(root.get("level"), shortLevel)
                        )
                    );
                } else {
                    // 没有对应简写，直接使用原值
                    predicates = cb.and(predicates, cb.equal(root.get("level"), level));
                }
            }
            if (Objects.nonNull(q.tag())) {
                predicates = cb.and(predicates, cb.equal(root.get("tag"), q.tag()));
            }
            if (Objects.nonNull(q.from())) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("timestamp"), q.from()));
            }
            if (Objects.nonNull(q.to())) {
                predicates = cb.and(predicates, cb.lessThanOrEqualTo(root.get("timestamp"), q.to()));
            }
            return predicates;
        }, pageable);
    }
} 
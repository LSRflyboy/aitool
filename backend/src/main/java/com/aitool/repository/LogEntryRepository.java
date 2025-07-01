package com.aitool.repository;

import com.aitool.model.FileRecord;
import com.aitool.model.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LogEntryRepository extends JpaRepository<LogEntry, Long>, JpaSpecificationExecutor<LogEntry> {
    Page<LogEntry> findByFileRecord(FileRecord file, Pageable pageable);
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying
    long deleteByFileRecord(FileRecord fileRecord);

    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying
    void deleteByFileRecord_UuidIn(java.util.List<String> uuids);
} 
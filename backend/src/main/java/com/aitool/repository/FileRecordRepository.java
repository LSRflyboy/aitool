package com.aitool.repository;

import com.aitool.model.FileRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileRecordRepository extends JpaRepository<FileRecord, Long> {
    Optional<FileRecord> findByUuid(String uuid);
} 
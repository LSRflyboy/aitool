package com.aitool.repository;

import com.aitool.model.FileRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FileRecordRepository extends JpaRepository<FileRecord, Long> {
    Optional<FileRecord> findByUuid(String uuid);

    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying
    void deleteByUuidIn(List<String> uuids);

    java.util.List<FileRecord> findByUuidIn(List<String> uuids);
} 
package com.aitool.service;

import com.aitool.model.FileRecord;
import com.aitool.model.FileStatus;
import com.aitool.model.LogEntry;
import com.aitool.repository.FileRecordRepository;
import com.aitool.repository.LogEntryRepository;
import com.aitool.parser.LogParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParseService {

    private final FileRecordRepository repository;
    private final LogEntryRepository logRepo;
    private final List<LogParser> parsers;

    @Async("parseExecutor")
    public void extractAndParseAsync(String uuid) {
        Optional<FileRecord> optional = repository.findByUuid(uuid);
        if (optional.isEmpty()) {
            log.error("No file record found for uuid {}", uuid);
            return;
        }
        FileRecord record = optional.get();
        try {
            Path extractedDir = extractArchive(Path.of(record.getStoragePath()));
            record.setExtractedPath(extractedDir.toString());
            log.info("Available log parsers: {}", parsers.size());
            record.setStatus(FileStatus.EXTRACTED);
            repository.save(record);

            // 递归解压并解析
            processDirectoryRecursively(extractedDir, record);
            record.setStatus(FileStatus.PARSED);
            repository.save(record);
        } catch (Exception e) {
            log.error("Failed to extract/parse file {}", uuid, e);
            record.setStatus(FileStatus.FAILED);
            record.setMessage(e.getMessage());
            repository.save(record);
        }
    }

    private Path extractArchive(Path archivePath) throws IOException {
        String fileName = archivePath.getFileName().toString().toLowerCase();
        Path destDir = archivePath.getParent().resolve("extracted");
        Files.createDirectories(destDir);
        if (fileName.endsWith(".zip")) {
            try (InputStream fis = Files.newInputStream(archivePath);
                 ArchiveInputStream ais = new ZipArchiveInputStream(fis)) {
                unpackArchiveStream(ais, destDir);
            }
        } else if (fileName.endsWith(".tar.gz") || fileName.endsWith(".tgz")) {
            try (InputStream fis = Files.newInputStream(archivePath);
                 GzipCompressorInputStream gis = new GzipCompressorInputStream(fis);
                 ArchiveInputStream ais = new TarArchiveInputStream(gis)) {
                unpackArchiveStream(ais, destDir);
            }
        } else {
            // not an archive, copy directly
            Path target = destDir.resolve(archivePath.getFileName());
            Files.copy(archivePath, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return destDir;
    }

    private void unpackArchiveStream(ArchiveInputStream ais, Path destDir) throws IOException {
        ArchiveEntry entry;
        while ((entry = ais.getNextEntry()) != null) {
            if (!ais.canReadEntryData(entry)) {
                continue;
            }
            Path targetPath = destDir.resolve(entry.getName()).normalize();
            if (entry.isDirectory()) {
                Files.createDirectories(targetPath);
            } else {
                Files.createDirectories(targetPath.getParent());
                Files.copy(ais, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    /**
     * Walk through directory tree, extract any nested archive (zip / tar.gz / tgz) and parse supported log files.
     */
    private void processDirectoryRecursively(Path dir, FileRecord record) throws IOException {
        Files.walk(dir).filter(Files::isRegularFile).forEach(path -> {
            String name = path.getFileName().toString().toLowerCase();
            try {
                if (name.endsWith(".zip") || name.endsWith(".tar.gz") || name.endsWith(".tgz")) {
                    Path subDir = extractArchive(path);
                    // 继续处理子目录
                    try {
                        processDirectoryRecursively(subDir, record);
                    } catch (IOException e) {
                        log.error("Failed to recurse into {}", subDir, e);
                    }
                } else {
                    for (LogParser parser : parsers) {
                        if (!parser.supports(path)) continue;
                        try {
                            List<LogEntry> entries = parser.parse(path);
                            if (!entries.isEmpty()) {
                                entries.forEach(e -> e.setFileRecord(record));
                                logRepo.saveAll(entries);
                                break; // 已成功解析，停止尝试其他解析器
                            }
                        } catch (IOException e) {
                            log.error("parser failed for file {} by {}", path, parser.getClass().getSimpleName(), e);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error processing file {}", path, e);
            }
        });
    }
} 
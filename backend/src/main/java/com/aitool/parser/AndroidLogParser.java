package com.aitool.parser;

import com.aitool.model.LogEntry;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class AndroidLogParser implements LogParser {

    // 支持两种格式：
    // 1. [I]|2025-06-30 15:08:56.004|TAG|message (老格式，带方括号)
    // 2. I|2025-06-30 15:08:56.004||0|M:Module|T:TAG|Q:queue|D:actual message (新格式，无方括号)
    private static final Pattern PATTERN_V1 = Pattern.compile("^\\[([A-Z])]\\|(?<time>\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3})\\|.*?(?<tag>[A-Za-z0-9_]+).*?\\|(?<msg>.*)$");
    private static final Pattern PATTERN_V2 = Pattern.compile("^([A-Z])\\|(?<time>\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3})\\|.*?\\|T:(?<tag>[^|]+)\\|.*?D:(?<msg>.*)$");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public boolean supports(Path filePath) {
        String name = filePath.getFileName().toString().toLowerCase();
        return name.endsWith(".log") || name.endsWith(".txt");
    }

    @Override
    public List<LogEntry> parse(Path filePath) throws IOException {
        List<LogEntry> list = new ArrayList<>();
        try (var lines = Files.lines(filePath)) {
            lines.forEach(line -> {
                Matcher m = PATTERN_V1.matcher(line);
                if (!m.find()) {
                    m = PATTERN_V2.matcher(line);
                }
                if (m.find()) {
                    String level = m.group(1);
                    String tsStr = m.group("time");
                    String tag = m.group("tag");
                    String msg = m.group("msg");
                    LocalDateTime ts;
                    try {
                        ts = LocalDateTime.parse(tsStr, FORMATTER);
                    } catch (Exception e) {
                        ts = null;
                    }
                    list.add(LogEntry.builder()
                            .timestamp(ts)
                            .level(level)
                            .tag(tag)
                            .message(msg)
                            .rawLine(line)
                            .build());
                }
            });
        }
        log.info("Android parser produced {} entries from {}", list.size(), filePath);
        return list;
    }
} 
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
public class IosLogParser implements LogParser {

    // Example: 2025-06-25 12:30:04.094 MyApp[123:456] <Error>: message text
    private static final Pattern PATTERN = Pattern.compile("^(?<time>\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}) .*?<(?<level>[A-Za-z]+)>: (?<msg>.*)$");
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
                Matcher m = PATTERN.matcher(line);
                if (m.find()) {
                    String tsStr = m.group("time");
                    String level = m.group("level");
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
                            .tag("iOS")
                            .message(msg)
                            .rawLine(line)
                            .build());
                }
            });
        }
        log.info("iOS parser produced {} entries from {}", list.size(), filePath);
        return list;
    }
} 
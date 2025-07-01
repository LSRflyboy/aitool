package com.aitool.parser;

import com.aitool.model.LogEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface LogParser {
    boolean supports(Path filePath);

    List<LogEntry> parse(Path filePath) throws IOException;
} 
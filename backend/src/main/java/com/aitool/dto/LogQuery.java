package com.aitool.dto;

import java.time.LocalDateTime;

public record LogQuery(String level, String tag, LocalDateTime from, LocalDateTime to) {
} 
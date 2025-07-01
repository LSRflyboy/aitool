package com.aitool.parser;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ParserRegistry {

    @Bean
    public List<LogParser> logParsers() {
        return List.of(
                new AndroidLogParser(),
                new IosLogParser()
        );
    }
} 
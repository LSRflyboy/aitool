package com.aitool.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
 
@ConfigurationProperties(prefix = "aitool.storage")
public record StorageProperties(@DefaultValue("${user.home}/aitool-storage") String rootDir) {
} 
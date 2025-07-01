package com.aitool.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有来源
        config.addAllowedOriginPattern("*");
        // 允许所有头信息
        config.addAllowedHeader("*");
        // 允许所有方法
        config.addAllowedMethod("*");
        // 允许凭证
        config.setAllowCredentials(true);
        // 暴露所有头信息
        config.addExposedHeader("*");
        // 预检请求的有效期
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
} 
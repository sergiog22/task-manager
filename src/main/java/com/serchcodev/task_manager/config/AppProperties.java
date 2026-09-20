package com.serchcodev.task_manager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Cors cors, Jwt jwt) {

    public record Cors(List<String> allowedOrigins) {
    }

    public record Jwt(String secret, long expirationMinutes) {
    }
}

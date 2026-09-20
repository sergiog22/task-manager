package com.serchcodev.task_manager.auth.api;

public record AuthResponse(String token, String tokenType, long expiresIn) {
}
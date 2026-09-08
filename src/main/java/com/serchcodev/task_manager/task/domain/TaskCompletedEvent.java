package com.serchcodev.task_manager.task.domain;

import java.time.LocalDateTime;

public record TaskCompletedEvent(Long taskId, LocalDateTime completedAt) {
}

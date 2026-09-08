package com.serchcodev.task_manager.task.application;

import com.serchcodev.task_manager.task.api.TaskResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public record TaskPageResponse(
        List<TaskResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    static TaskPageResponse from(Page<TaskResponse> tasks) {
        return new TaskPageResponse(tasks.getContent(), tasks.getNumber(), tasks.getSize(),
                tasks.getTotalElements(), tasks.getTotalPages(), tasks.isFirst(), tasks.isLast());
    }
}

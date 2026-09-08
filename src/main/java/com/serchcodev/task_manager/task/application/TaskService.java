package com.serchcodev.task_manager.task.application;

import com.serchcodev.task_manager.task.api.TaskRequest;
import com.serchcodev.task_manager.task.api.TaskResponse;
import org.springframework.data.domain.Pageable;

public interface TaskService {
    TaskPageResponse findAll(Pageable pageable);

    TaskResponse findById(Long id);

    TaskResponse create(TaskRequest request);

    TaskResponse update(Long id, TaskRequest request);

    void delete(Long id);
}

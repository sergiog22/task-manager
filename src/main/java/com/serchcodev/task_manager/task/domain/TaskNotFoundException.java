package com.serchcodev.task_manager.task.domain;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(Long id) {
        super("No existe una tarea con id " + id);
    }
}

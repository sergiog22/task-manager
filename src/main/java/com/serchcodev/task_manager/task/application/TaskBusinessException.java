package com.serchcodev.task_manager.task.application;

public class TaskBusinessException extends RuntimeException {
    public TaskBusinessException(String message) {
        super(message);
    }
}

package com.serchcodev.task_manager.task.infrastructure;

import com.serchcodev.task_manager.task.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}

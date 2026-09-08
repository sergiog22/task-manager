package com.serchcodev.task_manager.task.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/** Outbound port that keeps application logic independent from persistence technology. */
public interface TaskPort {
    Page<Task> findAll(Pageable pageable);

    Optional<Task> findById(Long id);

    Task save(Task task);

    void delete(Task task);
}

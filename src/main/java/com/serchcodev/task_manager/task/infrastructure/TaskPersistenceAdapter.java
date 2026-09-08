package com.serchcodev.task_manager.task.infrastructure;

import com.serchcodev.task_manager.task.domain.Task;
import com.serchcodev.task_manager.task.domain.TaskPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TaskPersistenceAdapter implements TaskPort {

    private final TaskRepository repository;

    @Override
    public Page<Task> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Optional<Task> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Task save(Task task) {
        return repository.save(task);
    }

    @Override
    public void delete(Task task) {
        repository.delete(task);
    }
}

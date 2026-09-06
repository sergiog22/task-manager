package com.serchcodev.task_manager.task.application;

import com.serchcodev.task_manager.task.api.TaskRequest;
import com.serchcodev.task_manager.task.api.TaskResponse;
import com.serchcodev.task_manager.task.domain.Task;
import com.serchcodev.task_manager.task.domain.TaskNotFoundException;
import com.serchcodev.task_manager.task.infrastructure.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public List<TaskResponse> findAll() {
        return taskRepository.findAll().stream()
                .map(TaskResponse::from)
                .toList();
    }

    public TaskResponse findById(Long id) {
        return TaskResponse.from(findTask(id));
    }

    public TaskResponse create(TaskRequest request) {
        Task task = new Task();
        copyRequest(request, task);
        return TaskResponse.from(taskRepository.save(task));
    }

    public TaskResponse update(Long id, TaskRequest request) {
        Task task = findTask(id);
        copyRequest(request, task);
        return TaskResponse.from(taskRepository.save(task));
    }

    public void delete(Long id) {
        taskRepository.delete(findTask(id));
    }

    private Task findTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    private void copyRequest(TaskRequest request, Task task) {
        task.setTitulo(request.titulo());
        task.setDescripcion(request.descripcion());
        task.setEstado(request.estado());
        task.setPrioridad(request.prioridad());
        task.setFechaLimite(request.fechaLimite());
    }
}

package com.serchcodev.task_manager.task.application;

import com.serchcodev.task_manager.task.api.TaskRequest;
import com.serchcodev.task_manager.task.api.TaskResponse;
import com.serchcodev.task_manager.task.domain.Task;
import com.serchcodev.task_manager.task.domain.TaskCompletedEvent;
import com.serchcodev.task_manager.task.domain.TaskNotFoundException;
import com.serchcodev.task_manager.task.domain.TaskPort;
import com.serchcodev.task_manager.task.domain.TaskPriority;
import com.serchcodev.task_manager.task.domain.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private final TaskPort taskPort;
    private final TaskMapper taskMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public TaskPageResponse findAll(Pageable pageable) {
        log.debug("Listing tasks: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return TaskPageResponse.from(taskPort.findAll(pageable).map(taskMapper::toResponse));
    }

    @Override
    public TaskResponse findById(Long id) {
        return taskMapper.toResponse(findTask(id));
    }

    @Override
    @Transactional
    public TaskResponse create(TaskRequest request) {
        validateBusinessRules(request);
        Task saved = taskPort.save(taskMapper.toEntity(request));
        log.info("Task created: id={}", saved.getId());
        publishCompletionIfNeeded(null, saved);
        return taskMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TaskResponse update(Long id, TaskRequest request) {
        validateBusinessRules(request);
        Task task = findTask(id);
        TaskStatus previousStatus = task.getEstado();
        taskMapper.updateEntity(request, task);
        Task saved = taskPort.save(task);
        log.info("Task updated: id={}", saved.getId());
        publishCompletionIfNeeded(previousStatus, saved);
        return taskMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        taskPort.delete(findTask(id));
        log.info("Task deleted: id={}", id);
    }

    private Task findTask(Long id) {
        return taskPort.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
    }

    private void validateBusinessRules(TaskRequest request) {
        if (request.prioridad() == TaskPriority.ALTA && !isAdmin()) {
            throw new TaskBusinessException("Solo un administrador puede crear o modificar tareas de prioridad ALTA");
        }
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    private void publishCompletionIfNeeded(TaskStatus previousStatus, Task task) {
        if (task.getEstado() == TaskStatus.COMPLETADA && previousStatus != TaskStatus.COMPLETADA) {
            eventPublisher.publishEvent(new TaskCompletedEvent(task.getId(), java.time.LocalDateTime.now()));
            log.info("Task completion event published: id={}", task.getId());
        }
    }
}

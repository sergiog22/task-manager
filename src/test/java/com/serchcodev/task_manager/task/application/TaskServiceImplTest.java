package com.serchcodev.task_manager.task.application;

import com.serchcodev.task_manager.task.api.TaskRequest;
import com.serchcodev.task_manager.task.api.TaskResponse;
import com.serchcodev.task_manager.task.domain.Task;
import com.serchcodev.task_manager.task.domain.TaskCompletedEvent;
import com.serchcodev.task_manager.task.domain.TaskPort;
import com.serchcodev.task_manager.task.domain.TaskPriority;
import com.serchcodev.task_manager.task.domain.TaskStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskPort taskPort;
    @Mock
    private TaskMapper taskMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private TaskServiceImpl service;

    @Test
    void createsTaskAndPublishesCompletionEvent() {
        authenticateAsAdmin();
        TaskRequest request = new TaskRequest("Cerrar reporte", "Pendiente del mes", TaskStatus.COMPLETADA,
                TaskPriority.ALTA, LocalDate.of(2026, 9, 30));
        Task task = new Task();
        task.setId(7L);
        task.setEstado(TaskStatus.COMPLETADA);
        TaskResponse response = new TaskResponse(7L, "Cerrar reporte", "Pendiente del mes", TaskStatus.COMPLETADA,
                TaskPriority.ALTA, request.fechaLimite(), null, null);
        when(taskMapper.toEntity(request)).thenReturn(task);
        when(taskPort.save(task)).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(response);

        assertThat(service.create(request)).isEqualTo(response);

        ArgumentCaptor<TaskCompletedEvent> event = ArgumentCaptor.forClass(TaskCompletedEvent.class);
        verify(eventPublisher).publishEvent(event.capture());
        assertThat(event.getValue().taskId()).isEqualTo(7L);
    }

    @Test
    void rejectsHighPriorityTaskForNonAdmin() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("user", "password", "ROLE_USER"));
        TaskRequest request = new TaskRequest("Tarea", null, TaskStatus.PENDIENTE, TaskPriority.ALTA, null);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(TaskBusinessException.class)
                .hasMessageContaining("administrador");
    }

    private void authenticateAsAdmin() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("admin", "password",
                java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }
}

package com.serchcodev.task_manager.task.api;

import com.serchcodev.task_manager.task.domain.TaskPriority;
import com.serchcodev.task_manager.task.domain.TaskStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String titulo,
        String descripcion,
        TaskStatus estado,
        TaskPriority prioridad,
        LocalDate fechaLimite,
        LocalDateTime creadaEn,
        LocalDateTime actualizadaEn
) {
}

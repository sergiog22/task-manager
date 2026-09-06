package com.serchcodev.task_manager.task.api;

import com.serchcodev.task_manager.task.domain.TaskPriority;
import com.serchcodev.task_manager.task.domain.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskRequest(
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 150, message = "El título no puede superar 150 caracteres")
        String titulo,

        @Size(max = 500, message = "La descripción no puede superar 500 caracteres")
        String descripcion,

        @NotNull(message = "El estado es obligatorio")
        TaskStatus estado,

        @NotNull(message = "La prioridad es obligatoria")
        TaskPriority prioridad,

        LocalDate fechaLimite
) {
}

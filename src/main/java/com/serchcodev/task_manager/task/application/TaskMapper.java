package com.serchcodev.task_manager.task.application;

import com.serchcodev.task_manager.task.api.TaskRequest;
import com.serchcodev.task_manager.task.api.TaskResponse;
import com.serchcodev.task_manager.task.domain.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creadaEn", ignore = true)
    @Mapping(target = "actualizadaEn", ignore = true)
    Task toEntity(TaskRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creadaEn", ignore = true)
    @Mapping(target = "actualizadaEn", ignore = true)
    void updateEntity(TaskRequest request, @MappingTarget Task task);

    TaskResponse toResponse(Task task);
}

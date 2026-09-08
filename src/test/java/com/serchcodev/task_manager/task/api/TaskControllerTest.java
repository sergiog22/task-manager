package com.serchcodev.task_manager.task.api;

import com.serchcodev.task_manager.task.application.TaskPageResponse;
import com.serchcodev.task_manager.task.application.TaskService;
import com.serchcodev.task_manager.task.domain.TaskPriority;
import com.serchcodev.task_manager.task.domain.TaskStatus;
import com.serchcodev.task_manager.task.domain.TaskNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@Import(ApiExceptionHandler.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Test
    void returnsPaginatedTasks() throws Exception {
        TaskResponse task = new TaskResponse(1L, "Tarea", null, TaskStatus.PENDIENTE, TaskPriority.MEDIA, null, null, null);
        when(taskService.findAll(any())).thenReturn(new TaskPageResponse(List.of(task), 0, 20, 1, 1, true, true));

        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void returnsProblemDetailForInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/tasks").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"\",\"estado\":\"PENDIENTE\",\"prioridad\":\"MEDIA\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.titulo").exists());
    }

    @Test
    void returnsNotFoundProblemDetail() throws Exception {
        when(taskService.findById(99L)).thenThrow(new TaskNotFoundException(99L));

        mockMvc.perform(get("/api/v1/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Task not found"));
    }
}

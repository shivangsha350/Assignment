package com.internal.tasktracker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Invalid status returns HTTP 400 Bad Request with error description")
    void searchTasks_invalidStatus_returns400() throws Exception {
        mockMvc.perform(get("/api/tasks").param("status", "INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid status: 'INVALID'. Allowed values: OPEN, IN_PROGRESS, DONE"));
    }

    @Test
    @DisplayName("Page less than 1 returns HTTP 400 Bad Request")
    void searchTasks_invalidPage_returns400() throws Exception {
        mockMvc.perform(get("/api/tasks").param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Page must be greater than or equal to 1"));

        mockMvc.perform(get("/api/tasks").param("page", "-5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Page must be greater than or equal to 1"));
    }

    @Test
    @DisplayName("Page size less than 1 or exceeding 100 returns HTTP 400 Bad Request")
    void searchTasks_invalidPageSize_returns400() throws Exception {
        mockMvc.perform(get("/api/tasks").param("pageSize", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Page size must be between 1 and 100"));

        mockMvc.perform(get("/api/tasks").param("pageSize", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Page size must be between 1 and 100"));
    }

    @Test
    @DisplayName("Valid query returns HTTP 200 with matching response structure")
    void searchTasks_validRequest_returns200() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .param("q", "api")
                        .param("status", "OPEN")
                        .param("page", "1")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.total").isNumber())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.pageSize").value(5));
    }
}

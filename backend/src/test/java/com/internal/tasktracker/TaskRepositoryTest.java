package com.internal.tasktracker;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Test
    @DisplayName("Archived tasks must never be returned when search term is empty")
    void searchTasks_excludesArchivedTasks_whenEmptySearchTerm() {
        // Confirm archived tasks exist in DB to ensure the test is meaningful
        long totalArchivedInDb = taskRepository.findAll().stream().filter(Task::isArchived).count();
        assertThat(totalArchivedInDb).isGreaterThan(0);

        List<Task> results = taskRepository.searchTasks("%%", null);

        assertThat(results).isNotEmpty();
        assertThat(results).noneMatch(Task::isArchived);
    }

    @Test
    @DisplayName("Archived tasks must never be returned even when title or description matches search term")
    void searchTasks_excludesArchivedTasks_whenSearchTermMatchesArchivedTask() {
        // Task 20 ("Decommission legacy search endpoint") and Task 21 ("Legacy API cleanup") have "api" and archived=true
        List<Task> results = taskRepository.searchTasks("%api%", null);

        assertThat(results).isNotEmpty();
        assertThat(results).noneMatch(Task::isArchived);
    }

    @Test
    @DisplayName("Status filter must strictly exclude tasks of other statuses when no search term is provided")
    void searchTasks_filtersByStatus_withoutSearchTerm() {
        List<Task> doneTasks = taskRepository.searchTasks("%%", "DONE");

        assertThat(doneTasks).isNotEmpty();
        assertThat(doneTasks).allMatch(task -> "DONE".equals(task.getStatus()));
        assertThat(doneTasks).noneMatch(Task::isArchived);

        List<Task> openTasks = taskRepository.searchTasks("%%", "OPEN");

        assertThat(openTasks).isNotEmpty();
        assertThat(openTasks).allMatch(task -> "OPEN".equals(task.getStatus()));
        assertThat(openTasks).noneMatch(Task::isArchived);
    }

    @Test
    @DisplayName("Status filter must strictly exclude tasks of other statuses when search term matches title or description")
    void searchTasks_filtersByStatus_withSearchTerm() {
        // Query for "fix" tasks with DONE status: prior bug returned OPEN and IN_PROGRESS tasks whose title started with "Fix"
        List<Task> results = taskRepository.searchTasks("%fix%", "DONE");

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(task -> "DONE".equals(task.getStatus()));
        assertThat(results).noneMatch(Task::isArchived);
        assertThat(results).allMatch(task ->
                task.getTitle().toLowerCase().contains("fix")
                        || (task.getDescription() != null && task.getDescription().toLowerCase().contains("fix")));
    }
}

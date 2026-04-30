package com.taskmanager.service;

import com.taskmanager.model.Task;
import com.taskmanager.model.Project;
import com.taskmanager.model.User;
import com.taskmanager.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    public Task createTask(String title, String description, Long projectId,
                           Long assignedToId, Task.Priority priority,
                           LocalDateTime dueDate, User createdBy) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setProject(projectService.getProjectById(projectId));
        task.setPriority(priority);
        task.setDueDate(dueDate);
        task.setCreatedBy(createdBy);
        task.setStatus(Task.Status.TODO);

        if (assignedToId != null) {
            task.setAssignedTo(userService.findById(assignedToId));
        }

        return taskRepository.save(task);
    }

    public Task updateStatus(Long taskId, Task.Status status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus(status);
        return taskRepository.save(task);
    }

    public Task updateTask(Long taskId, String title, String description,
                           Task.Priority priority, LocalDateTime dueDate,
                           Long assignedToId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        if (title != null) task.setTitle(title);
        if (description != null) task.setDescription(description);
        if (priority != null) task.setPriority(priority);
        if (dueDate != null) task.setDueDate(dueDate);
        if (assignedToId != null) {
            task.setAssignedTo(userService.findById(assignedToId));
        }
        return taskRepository.save(task);
    }

    public List<Task> getTasksByProject(Long projectId) {
        Project project = projectService.getProjectById(projectId);
        return taskRepository.findByProject(project);
    }

    public List<Task> getTasksForUser(User user) {
        return taskRepository.findByAssignedTo(user);
    }

    public List<Task> getOverdueTasks() {
        return taskRepository.findByDueDateBeforeAndStatusNot(
                LocalDateTime.now(), Task.Status.DONE);
    }

    public void deleteTask(Long taskId) {
        taskRepository.deleteById(taskId);
    }
}
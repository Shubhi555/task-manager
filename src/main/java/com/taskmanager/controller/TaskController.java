package com.taskmanager.controller;

import com.taskmanager.model.Task;
import com.taskmanager.model.User;
import com.taskmanager.service.TaskService;
import com.taskmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody Map<String, Object> request,
                                         Authentication auth) {
        try {
            User creator = userService.findByEmail(auth.getName());
            LocalDateTime dueDate = null;
            if (request.get("dueDate") != null) {
                dueDate = LocalDateTime.parse(request.get("dueDate").toString());
            }
            Long assignedToId = null;
            if (request.get("assignedToId") != null) {
                assignedToId = Long.valueOf(request.get("assignedToId").toString());
            }
            Task.Priority priority = Task.Priority.MEDIUM;
            if (request.get("priority") != null) {
                priority = Task.Priority.valueOf(request.get("priority").toString());
            }
            Task task = taskService.createTask(
                    request.get("title").toString(),
                    request.get("description") != null ? request.get("description").toString() : "",
                    Long.valueOf(request.get("projectId").toString()),
                    assignedToId,
                    priority,
                    dueDate,
                    creator
            );
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getTasksByProject(@PathVariable Long projectId,
                                                Authentication auth) {
        try {
            List<Task> tasks = taskService.getTasksByProject(projectId);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyTasks(Authentication auth) {
        try {
            User user = userService.findByEmail(auth.getName());
            List<Task> tasks = taskService.getTasksForUser(user);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/overdue")
    public ResponseEntity<?> getOverdueTasks(Authentication auth) {
        try {
            List<Task> tasks = taskService.getOverdueTasks();
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                           @RequestBody Map<String, String> request,
                                           Authentication auth) {
        try {
            Task.Status status = Task.Status.valueOf(request.get("status"));
            Task task = taskService.updateStatus(id, status);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id,
                                         @RequestBody Map<String, Object> request,
                                         Authentication auth) {
        try {
            LocalDateTime dueDate = null;
            if (request.get("dueDate") != null) {
                dueDate = LocalDateTime.parse(request.get("dueDate").toString());
            }
            Long assignedToId = null;
            if (request.get("assignedToId") != null) {
                assignedToId = Long.valueOf(request.get("assignedToId").toString());
            }
            Task.Priority priority = null;
            if (request.get("priority") != null) {
                priority = Task.Priority.valueOf(request.get("priority").toString());
            }
            Task task = taskService.updateTask(
                    id,
                    request.get("title") != null ? request.get("title").toString() : null,
                    request.get("description") != null ? request.get("description").toString() : null,
                    priority,
                    dueDate,
                    assignedToId
            );
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id, Authentication auth) {
        try {
            taskService.deleteTask(id);
            return ResponseEntity.ok(Map.of("message", "Task deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

package com.taskmanager.controller;

import com.taskmanager.model.Project;
import com.taskmanager.model.User;
import com.taskmanager.service.ProjectService;
import com.taskmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?> getProjects(Authentication auth) {
        try {
            User user = userService.findByEmail(auth.getName());
            List<Project> projects = projectService.getProjectsForUser(user);
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody Map<String, String> request,
                                            Authentication auth) {
        try {
            User owner = userService.findByEmail(auth.getName());
            Project project = projectService.createProject(
                    request.get("name"),
                    request.get("description"),
                    owner
            );
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProject(@PathVariable Long id, Authentication auth) {
        try {
            Project project = projectService.getProjectById(id);
            return ResponseEntity.ok(project);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<?> addMember(@PathVariable Long id,
                                        @RequestBody Map<String, Long> request,
                                        Authentication auth) {
        try {
            User requester = userService.findByEmail(auth.getName());
            Project project = projectService.getProjectById(id);
            if (!project.getOwner().getEmail().equals(requester.getEmail())
                    && requester.getRole() != com.taskmanager.model.User.Role.ADMIN) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "Only project owner or admin can add members"));
            }
            Project updated = projectService.addMember(id, request.get("userId"));
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProject(@PathVariable Long id, Authentication auth) {
        try {
            User requester = userService.findByEmail(auth.getName());
            Project project = projectService.getProjectById(id);
            if (!project.getOwner().getEmail().equals(requester.getEmail())
                    && requester.getRole() != com.taskmanager.model.User.Role.ADMIN) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "Only project owner or admin can delete"));
            }
            projectService.deleteProject(id);
            return ResponseEntity.ok(Map.of("message", "Project deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}

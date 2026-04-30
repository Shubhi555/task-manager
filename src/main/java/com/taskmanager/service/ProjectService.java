package com.taskmanager.service;

import com.taskmanager.model.Project;
import com.taskmanager.model.User;
import com.taskmanager.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserService userService;

    public Project createProject(String name, String description, User owner) {
        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setOwner(owner);
        project.setMembers(new ArrayList<>());
        return projectRepository.save(project);
    }

    public List<Project> getProjectsForUser(User user) {
        List<Project> owned = projectRepository.findByOwner(user);
        List<Project> member = projectRepository.findByMembersContaining(user);
        for (Project p : member) {
            if (!owned.contains(p)) owned.add(p);
        }
        return owned;
    }

    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
    }

    public Project addMember(Long projectId, Long userId) {
        Project project = getProjectById(projectId);
        User user = userService.findById(userId);
        if (!project.getMembers().contains(user)) {
            project.getMembers().add(user);
        }
        return projectRepository.save(project);
    }

    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }
}
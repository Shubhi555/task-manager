package com.taskmanager.repository;

import com.taskmanager.model.Task;
import com.taskmanager.model.User;
import com.taskmanager.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProject(Project project);
    List<Task> findByAssignedTo(User user);
    List<Task> findByAssignedToAndStatus(User user, Task.Status status);
    List<Task> findByDueDateBeforeAndStatusNot(LocalDateTime date, Task.Status status);
}
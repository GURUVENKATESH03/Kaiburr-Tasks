package com.kaiburr.rabin.Service;


import com.kaiburr.rabin.Repos.TaskRepository;
import com.kaiburr.rabin.model.Task;
import com.kaiburr.rabin.model.TaskExecution;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private final TaskRepository repo;

    public TaskService(TaskRepository repo) {
        this.repo = repo;
    }

    public Task saveTask(Task task) {
        validateCommand(task.getCommand());
        return repo.save(task);
    }

    public void validateCommand(String command) {
        if (command.contains("rm") || command.contains("shutdown") || command.contains("sudo")) {
            throw new IllegalArgumentException("Unsafe command!");
        }
    }

    public Task executeTask(String taskId) throws IOException {
        Task task = repo.findById(taskId).orElseThrow();
        TaskExecution exec = new TaskExecution();
        exec.setStartTime(new Date());

        Process process = Runtime.getRuntime().exec(task.getCommand());
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String output = reader.lines().collect(Collectors.joining("\n"));

        exec.setEndTime(new Date());
        exec.setOutput(output);

        task.getTaskExecutions().add(exec);
        return repo.save(task);
    } 

    public List<Task> getAllTasks() {
        return repo.findAll();
    }

    public Task getTaskById(String id) {
        return repo.findById(id).orElse(null);
    }

    public void deleteTaskById(String id) {
        repo.deleteById(id);
    }

    public List<Task> findByName(String name) {
        return repo.findByNameContainingIgnoreCase(name);
    }
}

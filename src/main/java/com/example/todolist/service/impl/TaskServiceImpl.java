package com.example.todolist.service.impl;

import com.example.todolist.exception.NullEntityReferenceException;
import com.example.todolist.model.Task;
import com.example.todolist.repository.TaskRepository;
import com.example.todolist.service.TaskService;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {
    private TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public Task create(Task role) {
        if (role != null) {
            return taskRepository.save(role);
        }
        throw new NullEntityReferenceException("Task cannot be 'null'");
    }

    @Override
    public Task readById(long id) {
        return taskRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Task with id " + id + " not found"));
    }

    @Override
    public Task update(Task role) {
        if (role != null) {
            readById(role.getId());
            return taskRepository.save(role);
        }
        throw new NullEntityReferenceException("Task cannot be 'null'");
    }

    @Override
    public void delete(long id) {
        taskRepository.delete(readById(id));
    }

    @Override
    public List<Task> getAll() {
        List<Task> tasks = taskRepository.findAll();
        return tasks.isEmpty() ? new ArrayList<>() : tasks;
    }

    @Override
    public List<Task> getByTodoId(long todoId) {
        List<Task> tasks = taskRepository.getByTodoId(todoId);
        return tasks.isEmpty() ? new ArrayList<>() : tasks;
    }
}

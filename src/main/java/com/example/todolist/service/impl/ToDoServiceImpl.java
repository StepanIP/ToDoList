package com.example.todolist.service.impl;

import com.example.todolist.exception.NullEntityReferenceException;
import com.example.todolist.model.ToDo;
import com.example.todolist.repository.ToDoRepository;
import com.example.todolist.service.ToDoService;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Service
public class ToDoServiceImpl implements ToDoService {

    private ToDoRepository todoRepository;

    public ToDoServiceImpl(ToDoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @Override
    public ToDo create(ToDo role) {
        if (role != null) {
            return todoRepository.save(role);
        }
        throw new NullEntityReferenceException("ToDo cannot be 'null'");
    }

    @Override
    public ToDo readById(long id) {
        return todoRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("ToDo with id " + id + " not found"));
    }

    @Override
    public ToDo update(ToDo role) {
        if (role != null) {
            readById(role.getId());
            return todoRepository.save(role);
        }
        throw new NullEntityReferenceException("ToDo cannot be 'null'");
    }

    @Override
    public void delete(long id) {
        todoRepository.delete(readById(id));
    }

    @Override
    public List<ToDo> getAll() {
        List<ToDo> todos = todoRepository.findAll();
        return todos.isEmpty() ? new ArrayList<>() : todos;
    }

    @Override
    public List<ToDo> getByUserId(long userId) {
        List<ToDo> todos = todoRepository.getByUserId(userId);
        return todos.isEmpty() ? new ArrayList<>() : todos;
    }

    @Override
    public List<Boolean> isCollaboratorToDo(List<ToDo> allToDo, List<ToDo> collaboratorToDo) {
        List<Boolean> result = new LinkedList<>();
        boolean temp = true;
        for (ToDo todo : allToDo) {
            for (ToDo todo1 : collaboratorToDo) {
                if (todo.equals(todo1)) {
                    temp = true;
                    break;
                } else {
                    temp = false;
                }
            }
            result.add(temp);
        }
        return result;
    }
}

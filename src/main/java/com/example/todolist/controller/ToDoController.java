package com.example.todolist.controller;

import com.example.todolist.model.Task;
import com.example.todolist.model.ToDo;
import com.example.todolist.model.User;
import com.example.todolist.service.TaskService;
import com.example.todolist.service.ToDoService;
import com.example.todolist.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/todos")
public class ToDoController {

    private final ToDoService todoService;
    private final TaskService taskService;
    private final UserService userService;

    List<Boolean> isCollaboratorTodo;

    public ToDoController(ToDoService todoService, TaskService taskService, UserService userService) {
        this.todoService = todoService;
        this.taskService = taskService;
        this.userService = userService;
    }

    @GetMapping("/create/users/{owner_id}")
    public String create(@PathVariable("owner_id") long ownerId, Model model) {
        model.addAttribute("todo", new ToDo());
        model.addAttribute("ownerId", ownerId);
        return "create-todo";
    }

    @PostMapping("/create/users/{owner_id}")
    public String create(@PathVariable("owner_id") long ownerId, @Validated @ModelAttribute("todo") ToDo todo, BindingResult result) {
        if (result.hasErrors()) {
            return "create-todo";
        }
        todo.setCreatedAt(LocalDateTime.now());
        todo.setOwner(userService.readById(ownerId));
        todoService.create(todo);
        return "redirect:/todos/all/users/" + ownerId;
    }

    @GetMapping("/{id}/tasks")
    public String read(@PathVariable long id, Model model, Principal principal) {
        User authorizedUser = userService.readByEmail(principal.getName());
        if (!authorizedUser.getMyTodos().contains(todoService.readById(id)) && authorizedUser.getRole().getName().equals("USER")){
            model.addAttribute("isCollaboratorsToDo", true);
        }
        ToDo todo = todoService.readById(id);
        List<Task> tasks = taskService.getByTodoId(id);
        List<User> users = userService.getAll().stream()
                .filter(user -> user.getId() != todo.getOwner().getId()).collect(Collectors.toList());
        model.addAttribute("todo", todo);
        model.addAttribute("tasks", tasks);
        model.addAttribute("users", users);
        return "todo-tasks";
    }

    @GetMapping("/{todo_id}/update/users/{owner_id}")
    public String update(@PathVariable("todo_id") long todoId, @PathVariable("owner_id") long ownerId, Model model) {
        ToDo todo = todoService.readById(todoId);
        model.addAttribute("todo", todo);
        return "update-todo";
    }

    @PostMapping("/{todo_id}/update/users/{owner_id}")
    public String update(@PathVariable("todo_id") long todoId, @PathVariable("owner_id") long ownerId,
                         @Validated @ModelAttribute("todo") ToDo todo, BindingResult result) {
        if (result.hasErrors()) {
            todo.setOwner(userService.readById(ownerId));
            return "update-todo";
        }
        ToDo oldTodo = todoService.readById(todoId);
        todo.setOwner(oldTodo.getOwner());
        todo.setCollaborators(oldTodo.getCollaborators());
        todoService.update(todo);
        return "redirect:/todos/all/users/" + ownerId;
    }

    @GetMapping("/{todo_id}/delete/users/{owner_id}")
    public String delete(@PathVariable("todo_id") long todoId, @PathVariable("owner_id") long ownerId) {
        todoService.delete(todoId);
        return "redirect:/todos/all/users/" + ownerId;
    }

    @GetMapping("/all/users/{user_id}")
    public String getAll(@PathVariable("user_id") long userId, Model model, Principal principal) {
        User authorizedUser = userService.readByEmail(principal.getName());
        List<ToDo> todos = todoService.getByUserId(userId);
        List<ToDo> collaboratorTodos = authorizedUser.getOtherTodos();
        if (authorizedUser.getRole().getName().equals("USER") && authorizedUser.getId()==userId) {
            isCollaboratorTodo=todoService.isCollaboratorToDo(todos, collaboratorTodos);
        }
        else if(authorizedUser.getId()!=userId && !authorizedUser.getRole().getName().equals("ADMIN")){
            isCollaboratorTodo = new LinkedList<>();
            List<ToDo> authorizedUserToDo = userService.readByEmail(principal.getName()).getMyTodos();
            authorizedUserToDo.removeAll(userService.readByEmail(principal.getName()).getOtherTodos());
            boolean temp = false;
            for (ToDo todo : todos) {
                for (ToDo todo1 : authorizedUserToDo) {
                    if (todo.equals(todo1)) {
                        temp = false;
                        break;
                    } else {
                        temp = true;
                    }
                }
                isCollaboratorTodo.add(temp);
            }
        }
        else{
            int pool = todos.size();
            isCollaboratorTodo = new LinkedList<>();
            while(pool!=0){
                isCollaboratorTodo.add(false);
                pool--;
            }
        }

        model.addAttribute("todos", todos);
        model.addAttribute("user", userService.readById(userId));
        model.addAttribute("isCollaboratorTodo", isCollaboratorTodo);
        return "todos-user";
    }

    @GetMapping("/{id}/add")
    public String addCollaborator(@PathVariable long id, @RequestParam("user_id") long userId) {
        ToDo todo = todoService.readById(id);
        List<User> collaborators = todo.getCollaborators();
        collaborators.add(userService.readById(userId));
        todo.setCollaborators(collaborators);
        todoService.update(todo);
        return "redirect:/todos/" + id + "/tasks";
    }

    @GetMapping("/{id}/remove")
    public String removeCollaborator(@PathVariable long id, @RequestParam("user_id") long userId) {
        ToDo todo = todoService.readById(id);
        List<User> collaborators = todo.getCollaborators();
        collaborators.remove(userService.readById(userId));
        todo.setCollaborators(collaborators);
        todoService.update(todo);
        return "redirect:/todos/" + id + "/tasks";
    }
}

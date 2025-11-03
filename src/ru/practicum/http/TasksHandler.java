package ru.practicum.http;

import ru.practicum.manager.TaskManager;
import ru.practicum.model.Task;
import ru.practicum.exception.NotFoundException;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TasksHandler extends BaseHttpHandler {

    public TasksHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("GET".equals(method)) {
                if (path.matches("/tasks/?")) {
                    List<Task> tasks = manager.getAllTasks();
                    sendOk(exchange, gson.toJson(tasks));
                } else if (path.matches("/tasks/\\d+")) {
                    int id = Integer.parseInt(path.split("/")[2]);
                    Task task = manager.getTask(id);
                    if (task == null) sendNotFound(exchange);
                    else sendOk(exchange, gson.toJson(task));
                } else {
                    sendNotFound(exchange);
                }

            } else if ("POST".equals(method)) {
                InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                Task task = gson.fromJson(body, Task.class);

                if (task.getId() == 0) {
                    try {
                        manager.addTask(task);
                        sendOk(exchange, gson.toJson(task));
                    } catch (IllegalArgumentException e) {
                        sendHasInteractions(exchange);
                    }
                } else {
                    try {
                        manager.updateTask(task);
                        sendOk(exchange, gson.toJson(task));
                    } catch (NotFoundException e) {
                        sendNotFound(exchange);
                    } catch (IllegalArgumentException e) {
                        sendHasInteractions(exchange);
                    }
                }

            } else if ("DELETE".equals(method) && path.matches("/tasks/\\d+")) {
                int id = Integer.parseInt(path.split("/")[2]);
                try {
                    manager.deleteTask(id);
                    sendOk(exchange, "{\"status\":\"deleted\"}");
                } catch (NotFoundException e) {
                    sendNotFound(exchange);
                }

            } else {
                sendMethodNotAllowed(exchange);
            }

        } catch (Exception e) {
            sendServerError(exchange);
        } finally {
            exchange.close();
        }
    }
}
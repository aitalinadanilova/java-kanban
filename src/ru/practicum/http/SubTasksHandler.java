package ru.practicum.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.manager.TaskManager;
import ru.practicum.model.SubTask;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SubTasksHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson = HttpTaskServer.getGson();

    public SubTasksHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if (method.equals("GET")) {
                if (path.matches("/subtasks/?")) {
                    List<SubTask> subtasks = manager.getAllSubTasks();
                    sendOk(exchange, gson.toJson(subtasks));
                } else if (path.matches("/subtasks/\\d+")) {
                    int id = Integer.parseInt(path.split("/")[2]);
                    SubTask subTask = manager.getSubtask(id);
                    if (subTask != null) {
                        sendOk(exchange, gson.toJson(subTask));
                    } else {
                        sendNotFound(exchange);
                    }
                }
            } else if (method.equals("POST")) {
                InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                SubTask subTask = gson.fromJson(body, SubTask.class);

                if (subTask.getId() == 0) {
                    manager.addSubTask(subTask);
                    sendCreated(exchange, gson.toJson(subTask));
                } else {
                    boolean updated = manager.updateSubtask(subTask);
                    if (updated) sendCreated(exchange, gson.toJson(subTask));
                    else sendNotFound(exchange);
                }
            } else if (method.equals("DELETE") && path.matches("/subtasks/\\d+")) {
                int id = Integer.parseInt(path.split("/")[2]);
                manager.deleteSubtask(id);
                sendOk(exchange, "{\"status\":\"deleted\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendServerError(exchange);
        }
    }
}


package ru.practicum.http;

import ru.practicum.manager.TaskManager;
import ru.practicum.model.Epic;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler {

    public EpicsHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("GET".equals(method)) {
                if (path.matches("/epics/?")) {
                    List<Epic> epics = manager.getAllEpics();
                    sendOk(exchange, gson.toJson(epics));
                } else if (path.matches("/epics/\\d+")) {
                    int id = Integer.parseInt(path.split("/")[2]);
                    Epic epic = manager.getEpic(id);
                    if (epic != null) sendOk(exchange, gson.toJson(epic));
                    else sendNotFound(exchange);
                }

            } else if ("POST".equals(method)) {
                InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                Epic epic = gson.fromJson(body, Epic.class);

                if (epic.getId() == 0) {
                    manager.addEpic(epic);
                    sendCreated(exchange, gson.toJson(epic));
                } else {
                    boolean updated = manager.updateEpic(epic);
                    if (updated) sendCreated(exchange, gson.toJson(epic));
                    else sendNotFound(exchange);
                }

            } else if ("DELETE".equals(method) && path.matches("/epics/\\d+")) {
                int id = Integer.parseInt(path.split("/")[2]);
                manager.deleteEpic(id);
                sendOk(exchange, "{\"status\":\"deleted\"}");
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
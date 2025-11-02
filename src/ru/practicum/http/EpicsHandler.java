package ru.practicum.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.manager.TaskManager;
import ru.practicum.model.Epic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson = HttpTaskServer.getGson();

    public EpicsHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if (method.equals("GET")) {
                if (path.matches("/epics/?")) {
                    List<Epic> epics = manager.getAllEpics();
                    sendOk(exchange, gson.toJson(epics));
                } else if (path.matches("/epics/\\d+")) {
                    int id = Integer.parseInt(path.split("/")[2]);
                    Epic epic = manager.getEpic(id);
                    if (epic != null) {
                        sendOk(exchange, gson.toJson(epic));
                    } else {
                        sendNotFound(exchange);
                    }
                }
            } else if (method.equals("POST")) {
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
            } else if (method.equals("DELETE") && path.matches("/epics/\\d+")) {
                int id = Integer.parseInt(path.split("/")[2]);
                manager.deleteEpic(id);
                sendOk(exchange, "{\"status\":\"deleted\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendServerError(exchange);
        }
    }
}
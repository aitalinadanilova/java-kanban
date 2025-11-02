package ru.practicum.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.manager.TaskManager;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson = new Gson();

    public PrioritizedHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("GET")) {
                sendMethodNotAllowed(exchange);
                return;
            }
            sendOk(exchange, gson.toJson(manager.getPrioritizedTasks()));
        } catch (Exception e) {
            sendServerError(exchange);
        }
    }
}


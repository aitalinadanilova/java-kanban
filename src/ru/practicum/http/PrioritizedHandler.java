package ru.practicum.http;

import ru.practicum.manager.TaskManager;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler {

    public PrioritizedHandler(TaskManager manager) {
        super(manager);
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
        } finally {
            exchange.close();
        }
    }
}

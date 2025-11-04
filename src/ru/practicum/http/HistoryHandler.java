package ru.practicum.http;

import ru.practicum.manager.TaskManager;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler {

    public HistoryHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("GET")) {
                sendMethodNotAllowed(exchange);
                return;
            }
            sendOk(exchange, gson.toJson(manager.getHistory()));
        } catch (Exception e) {
            sendServerError(exchange);
        } finally {
            exchange.close();
        }
    }
}


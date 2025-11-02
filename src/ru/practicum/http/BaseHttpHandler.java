package ru.practicum.http;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler {

    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    protected void sendOk(HttpExchange exchange, String text) throws IOException {
        sendText(exchange, text, 200);
    }

    protected void sendCreated(HttpExchange exchange, String text) throws IOException {
        sendText(exchange, text, 201);
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        sendText(exchange, "{\"error\":\"Not Found\"}", 404);
    }

    protected void sendServerError(HttpExchange exchange) throws IOException {
        sendText(exchange, "{\"error\":\"Internal Server Error\"}", 500);
    }

    protected void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        sendText(exchange, "{\"error\":\"Method Not Allowed\"}", 405);
    }

    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        sendText(exchange, "{\"error\":\"Task intersects with existing tasks\"}", 409);
    }
}


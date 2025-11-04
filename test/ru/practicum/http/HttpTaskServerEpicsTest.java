package ru.practicum.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import ru.practicum.manager.*;
import ru.practicum.model.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerEpicsTest {

    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;

    @BeforeEach
    public void setUp() throws IOException {
        HistoryManager historyManager = Managers.getDefaultHistory();
        manager = new InMemoryTaskManager(historyManager);
        taskServer = new HttpTaskServer(manager);
        gson = HttpTaskServer.getGson();
        taskServer.start();
    }

    @AfterEach
    public void tearDown() {
        taskServer.stop();
    }

    @Test
    public void shouldAddEpicViaPostRequest() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Test epic", Status.NEW);
        String json = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Неверный код ответа сервера");

        List<Epic> epics = manager.getAllEpics();
        assertEquals(1, epics.size(), "Эпик должен быть добавлен");
        assertEquals("Epic 1", epics.get(0).getTitle(), "Название эпика не совпадает");
    }

    @Test
    public void shouldReturnAllEpicsViaGetRequest() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic A", "Test epic list", Status.NEW);
        manager.addEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Неверный код ответа при GET /epics");
        assertTrue(response.body().contains("Epic A"), "Ответ должен содержать Epic A");
    }
}
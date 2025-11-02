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

public class HttpTaskServerSubtasksTest {

    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;
    private Epic epic;

    @BeforeEach
    public void setUp() throws IOException {
        HistoryManager historyManager = Managers.getDefaultHistory();
        manager = new InMemoryTaskManager(historyManager);
        taskServer = new HttpTaskServer(manager);
        gson = HttpTaskServer.getGson();
        taskServer.start();

        epic = new Epic("Epic 1", "Main epic", Status.NEW);
        manager.addEpic(epic);
    }

    @AfterEach
    public void tearDown() {
        taskServer.stop();
    }

    @Test
    public void shouldAddSubtaskViaPostRequest() throws IOException, InterruptedException {
        SubTask subtask = new SubTask(
                "Subtask 1",
                "Subtask for POST test",
                Status.NEW,
                epic.getId()
        );

        String json = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Неверный код ответа при добавлении сабтаска");

        List<SubTask> subtasks = manager.getAllSubTasks();
        assertEquals(1, subtasks.size(), "Должен быть создан один сабтаск");
        assertEquals("Subtask 1", subtasks.get(0).getTitle(), "Название сабтаска не совпадает");
        assertEquals(epic.getId(), subtasks.get(0).getEpicId(), "ID эпика не совпадает");
    }

    @Test
    public void shouldReturnAllSubtasksViaGetRequest() throws IOException, InterruptedException {
        SubTask subtask = new SubTask("Subtask 2", "Desc", Status.NEW, epic.getId());
        manager.addSubTask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "GET /subtasks должен вернуть код 200");
        assertTrue(response.body().contains("Subtask 2"), "Ответ должен содержать Subtask 2");
    }

    @Test
    public void shouldDeleteSubtaskViaDeleteRequest() throws IOException, InterruptedException {
        SubTask subtask = new SubTask("Subtask to delete", "Desc", Status.NEW, epic.getId());
        manager.addSubTask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + subtask.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "DELETE /subtasks/{id} должен вернуть код 200");
        assertTrue(manager.getAllSubTasks().isEmpty(), "Список сабтасков должен быть пуст после удаления");
    }
}
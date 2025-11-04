package ru.practicum.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import ru.practicum.manager.*;
import ru.practicum.model.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerTasksTest {

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
    public void shouldAddTaskViaPostRequest() throws IOException, InterruptedException {
        Task task = new Task(
                "Test Task",
                "Task for testing POST /tasks",
                Status.NEW,
                Duration.ofMinutes(15),
                LocalDateTime.now()
        );

        String json = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный код ответа сервера");

        List<Task> tasks = manager.getAllTasks();
        assertEquals(1, tasks.size(), "Должна быть создана одна задача");
        assertEquals("Test Task", tasks.get(0).getTitle(), "Название задачи не совпадает");
    }

    @Test
    public void shouldReturnAllTasksViaGetRequest() throws IOException, InterruptedException {
        Task task = new Task(
                "Task 1", "Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now()
        );
        manager.addTask(task);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Task 1"), "Ответ должен содержать задачу Task 1");
    }
}

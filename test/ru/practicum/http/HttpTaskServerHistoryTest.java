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

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerHistoryTest {

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
    public void shouldReturnHistoryViaGetRequest() throws IOException, InterruptedException {
        Task task = new Task(
                "Viewed Task",
                "Check history endpoint",
                Status.NEW,
                Duration.ofMinutes(10),
                LocalDateTime.now()
        );
        manager.addTask(task);
        manager.getTask(task.getId());

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Viewed Task"), "История должна содержать просмотренную задачу");
    }
}

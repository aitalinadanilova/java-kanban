package ru.practicum.manager;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import ru.practicum.model.Task;
import ru.practicum.model.Status;

public class HistoryManagerTest {

    HistoryManager historyManager = Managers.getDefaultHistory();

    @Test
    void addTaskToHistory() {
        Task task = new Task("Task", "Description", Status.NEW);
        task.setId(1);

        historyManager.add(task);
        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История не должна быть пустой.");
        assertEquals(1, history.size(), "История должна содержать 1 задачу.");
    }
}

package ru.practicum.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private InMemoryHistoryManager historyManager;
    private Task task1, task2, task3;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
        task1 = new Task("T1", "desc1", Status.NEW, Duration.ofMinutes(10), LocalDateTime.now());
        task2 = new Task("T2", "desc2", Status.NEW, Duration.ofMinutes(10), LocalDateTime.now());
        task3 = new Task("T3", "desc3", Status.NEW, Duration.ofMinutes(10), LocalDateTime.now());
        task1.setId(1);
        task2.setId(2);
        task3.setId(3);
    }

    @Test
    void shouldReturnEmptyHistoryInitially() {
        assertTrue(historyManager.getHistory().isEmpty(), "История изначально пуста");
    }

    @Test
    void shouldAddAndNotDuplicateTasks() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1);
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Дубликаты не должны сохраняться");
        assertEquals(task1, history.get(history.size() - 1), "Последним должен быть task1");
    }

    @Test
    void shouldRemoveTasksFromBeginningMiddleAndEnd() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(1); // начало
        assertEquals(List.of(task2, task3), historyManager.getHistory());

        historyManager.remove(2); // середина
        assertEquals(List.of(task3), historyManager.getHistory());

        historyManager.remove(3); // конец
        assertTrue(historyManager.getHistory().isEmpty());
    }
}

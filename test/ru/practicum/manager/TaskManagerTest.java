package ru.practicum.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.model.*;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    protected abstract T createManager();

    @BeforeEach
    void setUp() {
        manager = createManager();
    }

    // Проверка добавления задач
    @Test
    void shouldAddAndGetTask() {
        Task task = new Task("Task 1", "Description", Status.NEW);
        int id = manager.addTask(task);

        Task saved = manager.getTask(id);
        assertNotNull(saved, "Задача должна сохраняться");
        assertEquals("Task 1", saved.getTitle());
        assertEquals(Status.NEW, saved.getStatus());
    }

    // Проверка связей подзадач и эпика
    @Test
    void subtaskShouldBeLinkedToEpic() {
        Epic epic = new Epic("Epic", "Desc", Status.NEW);
        int epicId = manager.addEpic(epic);

        SubTask subtask = new SubTask("Sub", "desc", Status.NEW, epicId);
        int subId = manager.addSubTask(subtask);

        SubTask loaded = manager.getSubtask(subId);
        assertEquals(epicId, loaded.getEpicId(), "Подзадача должна быть связана с эпиком");
        Epic loadedEpic = manager.getEpic(epicId);
        assertTrue(loadedEpic.getSubtaskIds().contains(subId));
    }

    // Проверка расчёта статуса эпика
    @Test
    void epicStatusShouldBeCalculatedCorrectly() {
        Epic epic = new Epic("Epic", "desc", Status.NEW);
        int epicId = manager.addEpic(epic);

        // a. Все NEW
        manager.addSubTask(new SubTask("s1", "d", Status.NEW, epicId));
        manager.addSubTask(new SubTask("s2", "d", Status.NEW, epicId));
        assertEquals(Status.NEW, manager.getEpic(epicId).getStatus(), "Все NEW → NEW");

        // b. Все DONE
        manager.deleteAllSubtasks();
        manager.addSubTask(new SubTask("s1", "d", Status.DONE, epicId));
        manager.addSubTask(new SubTask("s2", "d", Status.DONE, epicId));
        assertEquals(Status.DONE, manager.getEpic(epicId).getStatus(), "Все DONE → DONE");

        // c. NEW + DONE
        manager.deleteAllSubtasks();
        manager.addSubTask(new SubTask("s1", "d", Status.NEW, epicId));
        manager.addSubTask(new SubTask("s2", "d", Status.DONE, epicId));
        assertEquals(Status.IN_PROGRESS, manager.getEpic(epicId).getStatus(), "NEW + DONE → IN_PROGRESS");

        // d. Все IN_PROGRESS
        manager.deleteAllSubtasks();
        manager.addSubTask(new SubTask("s1", "d", Status.IN_PROGRESS, epicId));
        manager.addSubTask(new SubTask("s2", "d", Status.IN_PROGRESS, epicId));
        assertEquals(Status.IN_PROGRESS, manager.getEpic(epicId).getStatus(), "Все IN_PROGRESS → IN_PROGRESS");
    }

    // Проверка пересечения временных интервалов
    @Test
    void shouldNotAllowOverlappingTasks() {
        Task t1 = new Task("T1", "desc", Status.NEW);
        t1.setStartTime(LocalDateTime.of(2025, 10, 22, 10, 0));
        t1.setDuration(Duration.ofMinutes(60));
        manager.addTask(t1);

        Task t2 = new Task("T2", "desc", Status.NEW);
        t2.setStartTime(LocalDateTime.of(2025, 10, 22, 10, 30));
        t2.setDuration(Duration.ofMinutes(60));

        // Проверяем, что добавление конфликтной задачи не допускается (ожидаем выброс исключения)
        assertThrows(IllegalArgumentException.class, () -> {
            manager.addTask(t2);
        }, "Конфликтующие задачи не должны пересекаться по времени");
    }

    // Проверка истории
    @Test
    void shouldTrackHistory() {
        Task task = new Task("History", "desc", Status.NEW);
        int id = manager.addTask(task);

        manager.getTask(id);
        List<Task> history = manager.getHistory();

        assertEquals(1, history.size(), "История должна содержать одну задачу");
        assertEquals(task, history.get(0));
    }
}

package ru.practicum.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.model.Epic;
import ru.practicum.model.Status;
import ru.practicum.model.SubTask;
import ru.practicum.model.Task;



import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private InMemoryTaskManager manager;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager(new InMemoryHistoryManager());
    }

    @Test
    void shouldAddAndFindDifferentTypesOfTasks() {
        Epic epic = new Epic("Epic", "Epic Desc", Status.NEW);
        int epicId = manager.addEpic(epic);

        SubTask sub = new SubTask("Sub", "Sub Desc", Status.NEW, epicId);
        int subId = manager.addSubTask(sub);

        Task task = new Task("Task", "Desc", Status.NEW);
        int taskId = manager.addTask(task);

        assertEquals(task, manager.getTask(taskId), "Обычная задача не найдена по id.");
        assertEquals(epic, manager.getEpic(epicId), "Эпик не найден по id.");
        assertEquals(sub, manager.getSubtask(subId), "Подзадача не найдена по id.");
    }

    @Test
    void shouldNotConflictBetweenPresetAndGeneratedIds() {
        Task manual = new Task("Manual", "Desc", Status.NEW);
        manual.setId(10);
        int assignedId = manager.addTask(manual);

        assertNotEquals(10, assignedId, "Сгенерированный id не должен совпасть с вручную выставленным.");

        assertNull(manager.getTask(10), "В менеджере не должно быть задачи под ручным id=10.");

        assertEquals(manual, manager.getTask(assignedId), "Задача должна быть доступна по присвоенному id.");
    }
}

package ru.practicum.manager;

import org.junit.jupiter.api.*;
import ru.practicum.model.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    private File file;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws IOException {
        file = File.createTempFile("tasks", ".csv");
        manager = new FileBackedTaskManager(file);
    }

    @AfterEach
    void tearDown() {
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    void shouldSaveAndLoadTasksCorrectly() {
        Task task = new Task("Test task", "Simple description", Status.NEW);
        manager.addTask(task);

        Epic epic = new Epic("Epic", "Epic description", Status.NEW);
        int epicId = manager.addEpic(epic);

        SubTask sub = new SubTask("Subtask", "Part of epic", Status.DONE, epicId);
        manager.addSubTask(sub);

        manager.save();

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertEquals(1, loaded.getAllTasks().size(), "Должна быть 1 задача");
        Task loadedTask = loaded.getAllTasks().getFirst();
        assertEquals(task.getId(), loadedTask.getId());
        assertEquals(task.getTitle(), loadedTask.getTitle());
        assertEquals(task.getDescription(), loadedTask.getDescription());
        assertEquals(task.getStatus(), loadedTask.getStatus());

        assertEquals(1, loaded.getAllEpics().size(), "Должен быть 1 эпик");
        Epic loadedEpic = loaded.getAllEpics().getFirst();
        assertEquals(epic.getId(), loadedEpic.getId());
        assertEquals(epic.getTitle(), loadedEpic.getTitle());
        assertEquals(epic.getDescription(), loadedEpic.getDescription());
        assertEquals(epic.getStatus(), loadedEpic.getStatus());
        assertEquals(epic.getSubtaskIds().size(), loadedEpic.getSubtaskIds().size(),
                "Количество подзадач у эпика должно совпадать");

        assertEquals(1, loaded.getAllSubTasks().size(), "Должна быть 1 подзадача");
        SubTask loadedSub = loaded.getAllSubTasks().getFirst();
        assertEquals(sub.getId(), loadedSub.getId());
        assertEquals(sub.getTitle(), loadedSub.getTitle());
        assertEquals(sub.getDescription(), loadedSub.getDescription());
        assertEquals(sub.getStatus(), loadedSub.getStatus());
        assertEquals(sub.getEpicId(), loadedSub.getEpicId(),
                "EpicId подзадачи должен совпадать");
    }

    @Test
    void shouldHandleEmptyFile() throws IOException {
        Files.write(file.toPath(), "id,type,name,status,description,epic\n".getBytes());

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        assertTrue(loaded.getAllTasks().isEmpty(), "Список задач должен быть пуст");
        assertTrue(loaded.getAllEpics().isEmpty(), "Список эпиков должен быть пуст");
        assertTrue(loaded.getAllSubTasks().isEmpty(), "Список подзадач должен быть пуст");
    }

    @Test
    void shouldIncrementNextIdProperly() {
        Task task1 = new Task("Task 1", "Desc", Status.NEW);
        manager.addTask(task1);
        Task task2 = new Task("Task 2", "Desc", Status.NEW);
        manager.addTask(task2);

        manager.save();
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        Task task3 = new Task("Task 3", "Desc", Status.NEW);
        loaded.addTask(task3);

        assertEquals(3, task3.getId(), "Следующий ID должен быть корректно увеличен");
    }
}
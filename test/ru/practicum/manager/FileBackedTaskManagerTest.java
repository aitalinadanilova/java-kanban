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
        manager.addEpic(epic);

        SubTask sub = new SubTask("Subtask", "Part of epic", Status.DONE, epic.getId());
        manager.addSubTask(sub);

        manager.save();

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertEquals(1, loaded.getAllTasks().size(), "Должна быть 1 задача");
        assertEquals(1, loaded.getAllEpics().size(), "Должен быть 1 эпик");
        assertEquals(1, loaded.getAllSubTasks().size(), "Должна быть 1 подзадача");

        Epic loadedEpic = loaded.getAllEpics().getFirst();
        assertTrue(loadedEpic.getSubtaskIds().contains(sub.getId()), "Эпик должен содержать ID подзадачи");
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
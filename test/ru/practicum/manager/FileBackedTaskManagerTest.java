package ru.practicum.manager;

import org.junit.jupiter.api.*;
import ru.practicum.exception.ManagerLoadException;
import ru.practicum.model.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;

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
        assertEquals(1, loaded.getAllEpics().size(), "Должен быть 1 эпик");
        assertEquals(1, loaded.getAllSubTasks().size(), "Должна быть 1 подзадача");
    }

    @Test
    void shouldHandleEmptyFile() throws IOException {
        Files.write(file.toPath(), "id,type,name,status,description,epic,duration,startTime\n".getBytes());
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);

        assertTrue(loaded.getAllTasks().isEmpty());
        assertTrue(loaded.getAllEpics().isEmpty());
        assertTrue(loaded.getAllSubTasks().isEmpty());
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

    @Test
    void shouldSaveAndLoadStartTimeAndDuration() {
        Task task = new Task("Timed", "With time", Status.NEW);
        task.setStartTime(LocalDateTime.of(2025, 10, 30, 12, 0));
        task.setDuration(Duration.ofMinutes(90));
        manager.addTask(task);
        manager.save();

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        Task loadedTask = loaded.getAllTasks().getFirst();

        assertEquals(task.getStartTime(), loadedTask.getStartTime(), "Время начала должно совпадать");
        assertEquals(task.getDuration(), loadedTask.getDuration(), "Продолжительность должна совпадать");
    }

    @Test
    void shouldRebuildEpicSubtasksRelations() {
        Epic epic = new Epic("Epic", "desc", Status.NEW);
        int epicId = manager.addEpic(epic);

        SubTask sub1 = new SubTask("S1", "d", Status.NEW, epicId);
        SubTask sub2 = new SubTask("S2", "d", Status.NEW, epicId);
        manager.addSubTask(sub1);
        manager.addSubTask(sub2);

        manager.save();
        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        Epic loadedEpic = loaded.getEpic(epicId);

        assertEquals(2, loadedEpic.getSubtaskIds().size(), "Эпик должен содержать 2 подзадачи после загрузки");
    }

    @Test
    void shouldUpdateEpicTimeAfterLoad() {
        Epic epic = new Epic("Epic", "desc", Status.NEW);
        int epicId = manager.addEpic(epic);

        SubTask sub1 = new SubTask("S1", "d", Status.NEW, epicId);
        sub1.setStartTime(LocalDateTime.of(2025, 10, 30, 10, 0));
        sub1.setDuration(Duration.ofMinutes(60));

        SubTask sub2 = new SubTask("S2", "d", Status.NEW, epicId);
        sub2.setStartTime(LocalDateTime.of(2025, 10, 30, 12, 0));
        sub2.setDuration(Duration.ofMinutes(30));

        manager.addSubTask(sub1);
        manager.addSubTask(sub2);
        manager.save();

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        Epic loadedEpic = loaded.getEpic(epicId);

        assertEquals(LocalDateTime.of(2025, 10, 30, 10, 0), loadedEpic.getStartTime(),
                "Старт эпика должен быть равен самому раннему старту подзадачи");
        assertEquals(LocalDateTime.of(2025, 10, 30, 12, 30), loadedEpic.getEndTime(),
                "Конец эпика должен быть равен самому позднему окончанию подзадачи");
    }

    @Test
    void shouldThrowExceptionWhenFileIsCorrupted() throws IOException {
        Files.writeString(file.toPath(), "id,type,name,status,description,epic\nbroken,line,here\n");
        assertThrows(NumberFormatException.class, () -> {
            FileBackedTaskManager.loadFromFile(file);
        });
    }

    @Test
    void shouldSaveAfterEachModification() {
        Task t = new Task("A", "desc", Status.NEW);
        manager.addTask(t);

        long before = file.length();
        t.setStatus(Status.DONE);
        manager.updateTask(t);
        long after = file.length();

        assertTrue(after >= before, "Файл должен обновляться после каждого изменения");
    }

    @Test
    void shouldDeleteAndResaveCorrectly() {
        Task t1 = new Task("A", "desc", Status.NEW);
        int id1 = manager.addTask(t1);

        Task t2 = new Task("B", "desc", Status.NEW);
        manager.addTask(t2);

        manager.deleteTask(id1);
        manager.save();

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        assertEquals(1, loaded.getAllTasks().size(), "После удаления должна остаться одна задача");
    }
}
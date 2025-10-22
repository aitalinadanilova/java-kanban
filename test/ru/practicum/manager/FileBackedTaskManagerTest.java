package ru.practicum.manager;

import org.junit.jupiter.api.*;
import ru.practicum.model.*;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    private File tempFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setup() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        manager = new FileBackedTaskManager(tempFile);
    }

    @Test
    void saveAndLoad_shouldRestoreTasksCorrectly() {
        Task t1 = new Task("Task1", "desc1", Status.NEW);
        Epic e1 = new Epic("Epic1", "descE", Status.NEW);
        manager.addTask(t1);
        int epicId = manager.addEpic(e1);
        manager.addSubTask(new SubTask("Sub1", "sdesc", Status.DONE, epicId));

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loaded.getAllTasks().size());
        assertEquals(1, loaded.getAllEpics().size());
        assertEquals(1, loaded.getAllSubTasks().size());
    }
}

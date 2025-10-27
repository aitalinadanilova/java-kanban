package ru.practicum.manager;

import org.junit.jupiter.api.*;
import ru.practicum.model.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    private File file;

    @Override
    protected FileBackedTaskManager createManager() {
        try {
            file = File.createTempFile("tasks", ".csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new FileBackedTaskManager(file);
    }

    @AfterEach
    void cleanup() {
        if (file != null && file.exists()) {
            file.delete();
        }
    }

    @Test
    void shouldSaveAndLoadTasks() {
        Task task = new Task("Save", "desc", Status.NEW);
        manager.addTask(task);
        manager.save();

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(file);
        assertEquals(1, loaded.getAllTasks().size());
        assertEquals(task.getTitle(), loaded.getAllTasks().getFirst().getTitle());
    }

    @Test
    void shouldHandleFileErrorsGracefully() {
        File badFile = new File("/no_access_dir/tasks.csv");
        assertThrows(RuntimeException.class, () -> new FileBackedTaskManager(badFile).save(),
                "Ошибки при работе с файлом должны перехватываться корректно");
    }

    @Test
    void shouldNotThrowWhenLoadingEmptyFile() throws IOException {
        Files.write(file.toPath(), "id,type,name,status,description,epic\n".getBytes());
        assertDoesNotThrow(() -> FileBackedTaskManager.loadFromFile(file),
                "Пустой файл не должен вызывать исключений");
    }
}
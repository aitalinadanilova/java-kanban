package ru.practicum.manager;

import org.junit.jupiter.api.Test;
import ru.practicum.model.*;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerIntegrityTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createManager() {
        return new InMemoryTaskManager(new InMemoryHistoryManager());
    }

    @Test
    void shouldDeleteEpicAndItsSubtasks() {
        Epic epic = new Epic("Epic", "Desc", Status.NEW);
        int epicId = manager.addEpic(epic);
        int subId = manager.addSubTask(new SubTask("Sub", "d", Status.NEW, epicId));

        manager.deleteEpic(epicId);

        assertNull(manager.getEpic(epicId));
        assertNull(manager.getSubtask(subId));
    }
}

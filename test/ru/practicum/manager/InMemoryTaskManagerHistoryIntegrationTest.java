package ru.practicum.manager;
import org.junit.jupiter.api.Test;
import ru.practicum.model.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerHistoryIntegrationTest {

    private InMemoryTaskManager manager;
    private InMemoryHistoryManager history;


    @Test
    void getTask_shouldAddToHistory_andEnsureUniquenessAndOrder() {
        Task t1 = new Task("T1", "d1", Status.NEW);
        Task t2 = new Task("T2", "d2", Status.NEW);

        int id1 = manager.addTask(t1);
        int id2 = manager.addTask(t2);

        manager.getTask(id1);
        manager.getTask(id2);
        manager.getTask(id1);

        List<Task> hist = manager.getHistory();
        assertEquals(2, hist.size());
        assertEquals(id2, hist.get(0).getId());
        assertEquals(id1, hist.get(1).getId());
    }

    @Test
    void viewingDifferentKinds_shouldAllAppearInHistoryAsTasks() {
        Epic epic = new Epic("Epic", "ed", Status.NEW);
        int epicId = manager.addEpic(epic);

        SubTask sub = new SubTask("Sub", "sd", Status.NEW, epicId);
        int subId = manager.addSubTask(sub);

        Task t = new Task("T", "td", Status.NEW);
        int tId = manager.addTask(t);

        manager.getSubtask(subId);
        manager.getTask(tId);

        List<Task> h = manager.getHistory();
        assertEquals(3, h.size());
        assertTrue(h.stream().anyMatch(x -> x.getId() == epicId));
        assertTrue(h.stream().anyMatch(x -> x.getId() == subId));
        assertTrue(h.stream().anyMatch(x -> x.getId() == tId));
    }
}


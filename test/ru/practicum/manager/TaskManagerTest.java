package ru.practicum.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    protected abstract T createManager();

    @BeforeEach
    void setUp() {
        manager = createManager();
    }

    //  ADD / GET

    @Test
    void shouldAddAndGetTask() {
        Task task = new Task("Task 1", "Description", Status.NEW);
        int id = manager.addTask(task);

        Task saved = manager.getTask(id);
        assertNotNull(saved, "Задача должна сохраняться");
        assertEquals("Task 1", saved.getTitle());
    }

    @Test
    void shouldAddAndGetEpicAndSubtask() {
        Epic epic = new Epic("Epic", "Desc", Status.NEW);
        int epicId = manager.addEpic(epic);

        SubTask subtask = new SubTask("Sub", "desc", Status.NEW, epicId);
        int subId = manager.addSubTask(subtask);

        assertNotNull(manager.getEpic(epicId));
        assertNotNull(manager.getSubtask(subId));
        assertEquals(epicId, manager.getSubtask(subId).getEpicId());
    }

    //  UPDATE

    @Test
    void shouldUpdateTask() {
        Task task = new Task("Task", "Desc", Status.NEW);
        int id = manager.addTask(task);

        Task updated = new Task("Updated", "Desc2", Status.DONE);
        updated.setId(id);
        boolean result = manager.updateTask(updated);

        assertTrue(result);
        assertEquals(Status.DONE, manager.getTask(id).getStatus());
        assertEquals("Updated", manager.getTask(id).getTitle());
    }

    @Test
    void shouldUpdateEpicAndSubtask() {
        Epic epic = new Epic("Epic", "Desc", Status.NEW);
        int epicId = manager.addEpic(epic);

        Epic updatedEpic = new Epic("UpdatedEpic", "NewDesc", Status.NEW);
        updatedEpic.setId(epicId);
        assertTrue(manager.updateEpic(updatedEpic));
        assertEquals("UpdatedEpic", manager.getEpic(epicId).getTitle());

        SubTask sub = new SubTask("Sub", "d", Status.NEW, epicId);
        int subId = manager.addSubTask(sub);

        SubTask updatedSub = new SubTask("UpdatedSub", "d2", Status.DONE, epicId);
        updatedSub.setId(subId);
        assertTrue(manager.updateSubtask(updatedSub));
        assertEquals(Status.DONE, manager.getSubtask(subId).getStatus());
    }

    //  DELETE

    @Test
    void shouldDeleteSingleTaskEpicAndSubtask() {
        Task t = new Task("T", "D", Status.NEW);
        int tId = manager.addTask(t);
        manager.deleteTask(tId);
        assertNull(manager.getTask(tId));

        Epic e = new Epic("E", "D", Status.NEW);
        int eId = manager.addEpic(e);
        manager.deleteEpic(eId);
        assertNull(manager.getEpic(eId));

        Epic epic = new Epic("Epic", "Desc", Status.NEW);
        int epicId = manager.addEpic(epic);
        SubTask sub = new SubTask("S", "D", Status.NEW, epicId);
        int sId = manager.addSubTask(sub);

        manager.deleteSubtask(sId);
        assertNull(manager.getSubtask(sId));
        assertTrue(manager.getEpic(epicId).getSubtaskIds().isEmpty());
    }

    @Test
    void shouldDeleteAllTasksEpicsAndSubtasks() {
        Task t = new Task("T", "D", Status.NEW);
        manager.addTask(t);

        Epic e = new Epic("E", "D", Status.NEW);
        int eId = manager.addEpic(e);
        manager.addSubTask(new SubTask("S", "D", Status.NEW, eId));

        manager.deleteAllTasks();
        manager.deleteAllSubtasks();
        manager.deleteAllEpics();

        assertTrue(manager.getAllTasks().isEmpty());
        assertTrue(manager.getAllEpics().isEmpty());
        assertTrue(manager.getAllSubTasks().isEmpty());
    }

    //  GET LISTS

    @Test
    void shouldReturnAllTasksEpicsAndSubtasks() {
        manager.addTask(new Task("T", "D", Status.NEW));
        int eId = manager.addEpic(new Epic("E", "D", Status.NEW));
        manager.addSubTask(new SubTask("S", "D", Status.NEW, eId));

        assertEquals(1, manager.getAllTasks().size());
        assertEquals(1, manager.getAllEpics().size());
        assertEquals(1, manager.getAllSubTasks().size());
    }

    @Test
    void shouldReturnEpicSubtasks() {
        Epic epic = new Epic("Epic", "D", Status.NEW);
        int epicId = manager.addEpic(epic);
        SubTask s1 = new SubTask("S1", "D", Status.NEW, epicId);
        SubTask s2 = new SubTask("S2", "D", Status.NEW, epicId);
        manager.addSubTask(s1);
        manager.addSubTask(s2);

        List<SubTask> subs = manager.getEpicSubtasks(epicId);
        assertEquals(2, subs.size());
    }

    //  HISTORY

    @Test
    void shouldTrackHistory() {
        Task task = new Task("History", "desc", Status.NEW);
        int id = manager.addTask(task);
        manager.getTask(id);

        List<Task> history = manager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task, history.getFirst());
    }

    //  PRIORITIZED TASKS

    @Test
    void shouldReturnTasksSortedByStartTime() {
        Task t1 = new Task("T1", "D", Status.NEW);
        t1.setStartTime(LocalDateTime.of(2025, 10, 22, 10, 0));
        t1.setDuration(Duration.ofMinutes(60));

        Task t2 = new Task("T2", "D", Status.NEW);
        t2.setStartTime(LocalDateTime.of(2025, 10, 22, 8, 0));
        t2.setDuration(Duration.ofMinutes(60));

        manager.addTask(t1);
        manager.addTask(t2);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals("T2", prioritized.getFirst().getTitle(), "Задачи должны быть отсортированы по времени начала");
    }

    //STATUS / VALIDATION

    @Test
    void epicStatusShouldBeCalculatedCorrectly() {
        Epic epic = new Epic("Epic", "desc", Status.NEW);
        int epicId = manager.addEpic(epic);

        // Все NEW
        manager.addSubTask(new SubTask("s1", "d", Status.NEW, epicId));
        manager.addSubTask(new SubTask("s2", "d", Status.NEW, epicId));
        assertEquals(Status.NEW, manager.getEpic(epicId).getStatus());

        // Все DONE
        manager.deleteAllSubtasks();
        manager.addSubTask(new SubTask("s1", "d", Status.DONE, epicId));
        manager.addSubTask(new SubTask("s2", "d", Status.DONE, epicId));
        assertEquals(Status.DONE, manager.getEpic(epicId).getStatus());

        // NEW + DONE
        manager.deleteAllSubtasks();
        manager.addSubTask(new SubTask("s1", "d", Status.NEW, epicId));
        manager.addSubTask(new SubTask("s2", "d", Status.DONE, epicId));
        assertEquals(Status.IN_PROGRESS, manager.getEpic(epicId).getStatus());
    }

    @Test
    void shouldNotAllowOverlappingTasks() {
        Task t1 = new Task("T1", "desc", Status.NEW);
        t1.setStartTime(LocalDateTime.of(2025, 10, 22, 10, 0));
        t1.setDuration(Duration.ofMinutes(60));
        manager.addTask(t1);

        Task t2 = new Task("T2", "desc", Status.NEW);
        t2.setStartTime(LocalDateTime.of(2025, 10, 22, 10, 30));
        t2.setDuration(Duration.ofMinutes(60));

        assertThrows(IllegalArgumentException.class, () -> manager.addTask(t2));
    }
}
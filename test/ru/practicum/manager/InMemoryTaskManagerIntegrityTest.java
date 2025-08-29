package ru.practicum.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.model.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerIntegrityTest {

    private InMemoryTaskManager manager;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager(new InMemoryHistoryManager());
    }

    @Test
    void deleteSubtask_shouldRemoveSubtaskAndClearIdFromEpic() {
        Epic epic = new Epic("Epic", "EpicDesc", Status.NEW);
        int epicId = manager.addEpic(epic);

        SubTask s1 = new SubTask("S1", "d1", Status.NEW, epicId);
        SubTask s2 = new SubTask("S2", "d2", Status.NEW, epicId);
        int s1Id = manager.addSubTask(s1);
        int s2Id = manager.addSubTask(s2);

        Epic storedEpic = manager.getEpic(epicId);
        List<Integer> idsBefore = storedEpic.getSubtaskIds();
        assertTrue(idsBefore.contains(s1Id));
        assertTrue(idsBefore.contains(s2Id));

        manager.deleteSubtask(s1Id);

        assertNull(manager.getSubtask(s1Id), "Подзадача должна быть удалена из менеджера");
        Epic updatedEpic = manager.getEpic(epicId);
        assertFalse(updatedEpic.getSubtaskIds().contains(s1Id), "В эпике не должно остаться id удалённой подзадачи");
        assertTrue(updatedEpic.getSubtaskIds().contains(s2Id), "Оставшиеся id подзадач должны сохраниться");
    }

    @Test
    void deleteEpic_shouldRemoveEpicAndItsSubtasks() {
        Epic epic = new Epic("Epic", "EpicDesc", Status.NEW);
        int epicId = manager.addEpic(epic);

        SubTask s1 = new SubTask("S1", "d1", Status.NEW, epicId);
        int s1Id = manager.addSubTask(s1);

        manager.deleteEpic(epicId);

        assertNull(manager.getEpic(epicId), "Эпик должен быть удалён");
        assertNull(manager.getSubtask(s1Id), "Подзадачи удалённого эпика тоже должны быть удалены");
    }

    @Test
    void deleteAllSubtasks_shouldClearSubtasksAndEpicsReference() {
        Epic epic1 = new Epic("E1", "d", Status.NEW);
        int e1 = manager.addEpic(epic1);

        SubTask s1 = new SubTask("S1", "d1", Status.NEW, e1);
        SubTask s2 = new SubTask("S2", "d2", Status.NEW, e1);
        manager.addSubTask(s1);
        manager.addSubTask(s2);

        manager.deleteAllSubtasks();

        List<SubTask> allSubs = manager.getAllSubTasks();
        assertTrue(allSubs.isEmpty(), "Все подзадачи должны быть удалены");

        Epic updatedEpic = manager.getEpic(e1);
        assertTrue(updatedEpic.getSubtaskIds().isEmpty(), "В эпике не должно оставаться старых id подзадач");
    }

    @Test
    void manualIdShouldNotConflictWithGeneratedId() {
        Task manual = new Task("Manual", "desc", Status.NEW);
        manual.setId(500);
        int added = manager.addTask(manual);

        assertNotEquals(500, added, "Сгенерированный id не должен совпадать с ранее вручную выставленным");
        assertNull(manager.getTask(500), "По ручному id не должно быть задачи в менеджере");
        assertNotNull(manager.getTask(added), "Задача доступна по присвоенному id");
    }

    @Test
    void mutationOfReturnedTaskAffectsStoredTask() {
        Task task = new Task("Orig", "orig desc", Status.NEW);
        int id = manager.addTask(task);

        Task retrieved = manager.getTask(id);
        retrieved.setTitle("Changed");
        retrieved.setDescription("changed desc");
        retrieved.setStatus(Status.DONE);

        Task stored = manager.getTask(id);
        assertEquals("Changed", stored.getTitle(), "Изменение через полученный объект должно отражаться в менеджере");
        assertEquals("changed desc", stored.getDescription());
        assertEquals(Status.DONE, stored.getStatus());
    }
}


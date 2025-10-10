package ru.practicum.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.model.*;


import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerIntegrityTest {

    private InMemoryTaskManager manager;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager(new InMemoryHistoryManager());
    }

    //Проверка удаления подзадачи
    @Test
    void deleteSubtask_shouldRemoveSubtaskAndClearIdFromEpic() {
        Epic epic = new Epic("Epic", "EpicDesc", Status.NEW);
        int epicId = manager.addEpic(epic);

        SubTask s1 = new SubTask("S1", "Desc1", Status.NEW, epicId);
        SubTask s2 = new SubTask("S2", "Desc2", Status.NEW, epicId);
        int s1Id = manager.addSubTask(s1);
        int s2Id = manager.addSubTask(s2);

        Epic before = manager.getEpic(epicId);
        assertTrue(before.getSubtaskIds().contains(s1Id));
        assertTrue(before.getSubtaskIds().contains(s2Id));

        manager.deleteSubtask(s1Id);

        assertNull(manager.getSubtask(s1Id), "Удалённая подзадача должна отсутствовать в менеджере");

        Epic updatedEpic = manager.getEpic(epicId);
        assertFalse(updatedEpic.getSubtaskIds().contains(s1Id),
                "Эпик не должен содержать id удалённой подзадачи");
        assertTrue(updatedEpic.getSubtaskIds().contains(s2Id),
                "Оставшиеся id подзадач должны сохраниться");
    }

    //Проверка удаления эпика
    @Test
    void deleteEpic_shouldRemoveEpicAndItsSubtasks() {
        Epic epic = new Epic("Epic", "EpicDesc", Status.NEW);
        int epicId = manager.addEpic(epic);

        SubTask sub = new SubTask("S1", "Desc", Status.NEW, epicId);
        int subId = manager.addSubTask(sub);

        manager.deleteEpic(epicId);

        assertNull(manager.getEpic(epicId), "Эпик должен быть удалён");
        assertNull(manager.getSubtask(subId), "Подзадачи удалённого эпика тоже должны быть удалены");
    }

    //Проверка полного удаления подзадач-
    @Test
    void deleteAllSubtasks_shouldClearSubtasksAndEpicsReference() {
        Epic epic = new Epic("E1", "desc", Status.NEW);
        int epicId = manager.addEpic(epic);

        manager.addSubTask(new SubTask("S1", "d1", Status.NEW, epicId));
        manager.addSubTask(new SubTask("S2", "d2", Status.NEW, epicId));


        manager.deleteAllSubtasks();

        assertTrue(manager.getAllSubTasks().isEmpty(), "Все подзадачи должны быть удалены");

        Epic updatedEpic = manager.getEpic(epicId);
        assertTrue(updatedEpic.getSubtaskIds().isEmpty(),
                "В эпике не должно оставаться старых id подзадач");
    }

    //Проверка генерации id
    @Test
    void manualIdShouldNotConflictWithGeneratedId() {
        Task manual = new Task("Manual", "desc", Status.NEW);
        manual.setId(500);

        int generatedId = manager.addTask(manual);

        assertNotEquals(500, generatedId,
                "Сгенерированный id не должен совпадать с вручную выставленным");
        assertNull(manager.getTask(500),
                "В менеджере не должно быть задачи с вручную заданным id");
        assertNotNull(manager.getTask(generatedId),
                "Задача должна быть доступна по сгенерированному id");
    }

    //Проверка, что изменение полученного объекта отражается в хранилище
    @Test
    void mutationOfReturnedTaskAffectsStoredTask() {
        Task task = new Task("Orig", "orig desc", Status.NEW);
        int id = manager.addTask(task);

        // Получаем задачу и изменяем
        Task retrieved = manager.getTask(id);
        retrieved.setTitle("Changed");
        retrieved.setDescription("changed desc");
        retrieved.setStatus(Status.DONE);

        // Проверяем, что в хранилище те же изменения
        Task stored = manager.getTask(id);
        assertEquals("Changed", stored.getTitle(), "Изменения должны отражаться в менеджере");
        assertEquals("changed desc", stored.getDescription());
        assertEquals(Status.DONE, stored.getStatus());
    }

    //Проверка удаления всех эпиков
    @Test
    void deleteAllEpics_shouldAlsoRemoveTheirSubtasks() {
        Epic e1 = new Epic("Epic1", "d", Status.NEW);
        int e1Id = manager.addEpic(e1);
        int s1Id = manager.addSubTask(new SubTask("S1", "d1", Status.NEW, e1Id));

        Epic e2 = new Epic("Epic2", "d", Status.NEW);
        int e2Id = manager.addEpic(e2);
        int s2Id = manager.addSubTask(new SubTask("S2", "d2", Status.NEW, e2Id));

        manager.deleteAllEpics();

        assertTrue(manager.getAllEpics().isEmpty(), "Все эпики должны быть удалены");
        assertTrue(manager.getAllSubTasks().isEmpty(), "Все подзадачи должны быть удалены");
        assertNull(manager.getSubtask(s1Id), "Подзадача первого эпика должна быть удалена");
        assertNull(manager.getSubtask(s2Id), "Подзадача второго эпика должна быть удалена");
    }
}
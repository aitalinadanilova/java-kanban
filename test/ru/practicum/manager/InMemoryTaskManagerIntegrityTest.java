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
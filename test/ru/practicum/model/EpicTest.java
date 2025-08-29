package ru.practicum.model;
import org.junit.jupiter.api.Test;
import ru.practicum.model.Epic;
import ru.practicum.model.Status;
import ru.practicum.model.SubTask;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    @Test
    void epicsWithSameIdShouldBeEqual() {
        Epic epic1 = new Epic("Epic1", "Epic description1", Status.NEW);
        epic1.setId(10);

        Epic epic2 = new Epic("Epic2", "Epic description2", Status.NEW);
        epic2.setId(10);

        assertEquals(epic1, epic2, "Эпики с одинаковым id должны быть равны.");
    }

    @Test
    void epicCannotBeItsOwnSubtask() {
        Epic epic = new Epic("Epic1", "Epic description1", Status.NEW);
        epic.setId(1);

        SubTask subtask = new SubTask("Sub", "Sub description", Status.NEW, 2);

        assertNotEquals(epic.getId(), subtask.getEpicId(),
                "Эпик не может быть своим же сабтаском.");
    }
}

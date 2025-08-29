package ru.practicum.model;
import org.junit.jupiter.api.Test;
import ru.practicum.model.Status;
import ru.practicum.model.SubTask;

import static org.junit.jupiter.api.Assertions.*;


class SubTaskTest {

    @Test
    void subtasksWithSameIdAreEqual() {
        SubTask subtask1 = new SubTask("Subtask1", "Description1", Status.NEW, 1);
        SubTask subtask2 = new SubTask("Subtask2", "Description2", Status.DONE, 1);

        subtask1.setId(1);
        subtask2.setId(1);

        assertEquals(subtask1, subtask2, "Подзадачи с одинаковым id должны быть равны.");
    }
}
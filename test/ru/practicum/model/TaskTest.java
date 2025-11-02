package ru.practicum.model;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import ru.practicum.model.Task;
import ru.practicum.model.Status;

import java.time.Duration;
import java.time.LocalDateTime;

public class TaskTest {

    @Test
    void tasksWithSameIdAreEqual() {
        Task task1 = new Task(
                "Task1",
                "Description1",
                Status.NEW,
                Duration.ofMinutes(10),
                LocalDateTime.now()
        );

        Task task2 = new Task(
                "Task2",
                "Description2",
                Status.DONE,
                Duration.ofMinutes(5),
                LocalDateTime.now()
        );

        task1.setId(1);
        task2.setId(1);

        assertEquals(task1, task2, "Задачи с одинаковым id должны быть равны.");
    }

}


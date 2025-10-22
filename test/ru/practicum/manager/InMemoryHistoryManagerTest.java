package ru.practicum.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.model.Task;
import ru.practicum.model.Status;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private InMemoryHistoryManager history;

    @BeforeEach
    void setUp() {
        history = new InMemoryHistoryManager();
    }

    @Test
    void add_shouldIgnoreNull() {
        history.add(null);
        List<Task> h = history.getHistory();
        assertNotNull(h);
        assertTrue(h.isEmpty(), "После добавления null история должна оставаться пустой");
    }

    @Test
    void add_duplicateShouldMoveToTailAndKeepOnlyOneCopy() {
        Task t1 = new Task("T1", "D1", Status.NEW);
        t1.setId(1);
        Task t2 = new Task("T2", "D2", Status.NEW);
        t2.setId(2);

        history.add(t1);
        history.add(t2);
        history.add(t1);

        List<Task> h = history.getHistory();
        assertEquals(2, h.size(), "Должно остаться 2 уникальные задачи");
        assertEquals(2, h.get(0).getId());
        assertEquals(1, h.get(1).getId());
    }

    @Test
    void getHistory_shouldReturnCopyNotReference() {
        Task t1 = new Task("T1", "D1", Status.NEW);
        t1.setId(1);
        history.add(t1);

        List<Task> first = history.getHistory();
        assertEquals(1, first.size());

        first.clear();

        List<Task> after = history.getHistory();
        assertEquals(1, after.size(), "Изменение возвращённого списка не должно влиять на внутренний список");
    }

    @Test
    void remove_shouldRemoveById() {
        Task t1 = new Task("T1", "D1", Status.NEW);
        t1.setId(1);
        Task t2 = new Task("T2", "D2", Status.NEW);
        t2.setId(2);

        history.add(t1);
        history.add(t2);

        history.remove(2); // удаляем вторую
        List<Task> h = history.getHistory();
        assertEquals(1, h.size());
        assertEquals(1, h.get(0).getId());
    }
}

package ru.practicum.manager;

import ru.practicum.model.Epic;
import ru.practicum.model.SubTask;
import ru.practicum.model.Task;

import java.util.List;

public interface TaskManager {
    // методы ADD
    int addTask(Task task);

    int addEpic(Epic epic);

    int addSubTask(SubTask subtask);

    // методы UPDATE
    boolean updateTask(Task updatedTask);

    boolean updateEpic(Epic updatedEpic);

    boolean updateSubtask(SubTask updatedSubtask);

    // методы DELETE
    void deleteTask(int id);

    void deleteEpic(int id);

    void deleteSubtask(int id);

    // методы DELETE
    void remove(int id);

    void deleteAllSubtasks();

    void deleteAllEpics();

    void deleteAllTasks();

    // методы GET
    Task getTask(int id);

    Epic getEpic(int id);

    Epic getEpicById(int id);

    SubTask getSubtask(int id);

    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<SubTask> getAllSubTasks();

    List<Task> getHistory();
}

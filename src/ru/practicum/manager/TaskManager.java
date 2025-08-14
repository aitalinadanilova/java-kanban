package ru.practicum.manager;
import ru.practicum.model.Task;
import ru.practicum.model.Epic;
import ru.practicum.model.SubTask;
import ru.practicum.model.Status;

import java.util.*;

public class TaskManager {

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, SubTask> subtasks = new HashMap<>();
    private int nextId = 1;

    private int generateId() {
        return nextId++;
    }

    // методы ADD
    public int addTask(Task task) {
        if (task == null) {
            return -1;
        }
        task.setId(generateId());
        tasks.put(task.getId(), task);
        return task.getId();
    }

    public int addEpic(Epic epic) {
        if (epic == null) {
            return -1;
        }
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
        return epic.getId();
    }

    public int addSubTask(SubTask subtask) {
        if (subtask == null) {
            return -1;
        }
        // Проверяем, существует ли эпик для этой подзадачи
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) {
            return -1; // Эпика нет — ничего не делаем
        }
        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);
        epic.addSubtaskId(subtask.getId());
        updateEpicStatus(epic);
        return subtask.getId();
    }

    // методы UPDATE
    public boolean updateTask(Task updatedTask) {
        if (updatedTask == null || !tasks.containsKey(updatedTask.getId())) {
            return false; // если такой задачи нет
        }
        tasks.put(updatedTask.getId(), updatedTask);
        return true;
    }

    public boolean updateEpic(Epic updatedEpic) {
        Epic existingEpic = epics.get(updatedEpic.getId());
        if (existingEpic == null) {
            return false; // если такого эпика нет
        }
        // обновляем только имя и описание
        existingEpic.setTitle(updatedEpic.getTitle());
        existingEpic.setDescription(updatedEpic.getDescription());
        return true;
    }

    public boolean updateSubtask(SubTask updatedSubtask) {
        if (updatedSubtask == null || !subtasks.containsKey(updatedSubtask.getId())) {
            return false; // подзадачи нет
        }
        // проверка на наличие такого эпика
        Epic epic = epics.get(updatedSubtask.getEpicId());
        if (epic == null) {
            return false;
        }
        subtasks.put(updatedSubtask.getId(), updatedSubtask);
        updateEpicStatus(epic);
        return true;
    }

    // метод DELETE
    public void deleteTask(int id) {
        tasks.remove(id);
    }

    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
        }
    }

    public void deleteSubtask(int id) {
        SubTask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubtaskIds().remove((Integer) id);
                updateEpicStatus(epic);
            }
        }
    }

    public void deleteAllSubtasks() {
        subtasks.clear();
        // очистка
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic);
        }
    }

    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }
    public void deleteAllTasks() {
        tasks.clear();
    }

    // метод GET
    public Task getTask(int id) {
        return tasks.get(id);
    }

    public Epic getEpic(int id) {
        return epics.get(id);
    }

    public Epic getEpicById(int id) {
        return epics.get(id); // если epics — это Map<Integer, Epic>
    }


    public SubTask getSubtask(int id) {
        return subtasks.get(id);
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public List<SubTask> getAllSubTasks() {
        return new ArrayList<>(subtasks.values());
    }


    private void updateEpicStatus(Epic epic) {
        if (epic.getSubtaskIds().isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Integer subtaskId : epic.getSubtaskIds()) {
            Status status = subtasks.get(subtaskId).getStatus();
            if (status != Status.NEW) {
                allNew = false;
            }
            if (status != Status.DONE) {
                allDone = false;
            }
        }

        if (allNew) {
            epic.setStatus(Status.NEW);
        } else if (allDone) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }
}
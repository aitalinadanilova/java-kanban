package ru.practicum.manager;

import ru.practicum.model.*;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final Map<Integer, SubTask> subtasks = new HashMap<>();
    private final HistoryManager historyManager; // связь с менеджером истории
    private int nextId = 1;

    public int generateId() {
        return nextId++;
    }

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    // методы ADD
    @Override
    public int addTask(Task task) {
        if (task == null) return -1;
        task.setId(generateId());
        tasks.put(task.getId(), task);
        return task.getId();
    }

    @Override
    public int addEpic(Epic epic) {
        if (epic == null) return -1;
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
        return epic.getId();
    }

    @Override
    public int addSubTask(SubTask subtask) {
        if (subtask == null) return -1;
        Epic epic = epics.get(subtask.getEpicId());
        if (epic == null) return -1;
        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);
        epic.addSubtaskId(subtask.getId());
        updateEpicStatus(epic);
        return subtask.getId();
    }

    // методы UPDATE
    @Override
    public boolean updateTask(Task updatedTask) {
        if (updatedTask == null || !tasks.containsKey(updatedTask.getId())) return false;
        tasks.put(updatedTask.getId(), updatedTask);
        return true;
    }

    @Override
    public boolean updateEpic(Epic updatedEpic) {
        Epic existingEpic = epics.get(updatedEpic.getId());
        if (existingEpic == null) return false;
        existingEpic.setTitle(updatedEpic.getTitle());
        existingEpic.setDescription(updatedEpic.getDescription());
        return true;
    }

    @Override
    public boolean updateSubtask(SubTask updatedSubtask) {
        if (updatedSubtask == null || !subtasks.containsKey(updatedSubtask.getId())) return false;
        Epic epic = epics.get(updatedSubtask.getEpicId());
        if (epic == null) return false;
        subtasks.put(updatedSubtask.getId(), updatedSubtask);
        updateEpicStatus(epic);
        return true;
    }

    @Override
    public void deleteTask(int id) {

    }

    @Override
    public void deleteEpic(int id) {

    }

    @Override
    public void deleteSubtask(int id) {

    }

    // методы DELETE
    @Override
    public void remove(int id) {
        // если это обычная задача
        if (tasks.containsKey(id)) {
            tasks.remove(id);
            historyManager.remove(id);
            return;
        }

        // если это подзадача
        if (subtasks.containsKey(id)) {
            SubTask subtask = subtasks.remove(id);
            historyManager.remove(id);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubtaskIds().remove(Integer.valueOf(id));
                updateEpicStatus(epic);
            }
            return;
        }

        // если это эпик
        if (epics.containsKey(id)) {
            Epic epic = epics.remove(id);
            if (epic != null) {
                for (Integer subId : epic.getSubtaskIds()) {
                    subtasks.remove(subId);
                    historyManager.remove(subId);
                }
                historyManager.remove(id);
            }
        }
    }

    @Override
    public void deleteAllSubtasks() {
        for (Integer id : subtasks.keySet()) {
            historyManager.remove(id);
        }
        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            updateEpicStatus(epic);
        }
    }

    @Override
    public void deleteAllEpics() {
        for (Epic epic : epics.values()) {
            historyManager.remove(epic.getId());
            for (Integer subId : epic.getSubtaskIds()) {
                historyManager.remove(subId);
            }
        }
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteAllTasks() {

    }

    // методы GET
    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public SubTask getSubtask(int id) {
        SubTask subtask = subtasks.get(id);
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<SubTask> getAllSubTasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override

    public List<Task> getHistory() {

        return historyManager.getHistory();

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
            if (status != Status.NEW) allNew = false;
            if (status != Status.DONE) allDone = false;
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

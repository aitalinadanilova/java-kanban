package ru.practicum.manager;

import ru.practicum.model.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, SubTask> subtasks = new HashMap<>();
    protected final HistoryManager historyManager;
    protected int nextId = 1;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    private int generateId() {
        return nextId++;
    }

    protected final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparingInt(Task::getId)
    );

    //ADD
    @Override
    public int addTask(Task task) {
        if (task == null) return -1;

        if (task.getStartTime() != null && task.getDuration() != null && hasOverlap(task)) {
            throw new IllegalArgumentException("Задачи пересекаются по времени: " + task.getTitle());
        }

        task.setId(generateId());
        tasks.put(task.getId(), task);
        prioritizedTasks.add(task);
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

            if (subtask.getStartTime() != null && subtask.getDuration() != null && hasOverlap(subtask)) {
                throw new IllegalArgumentException("Подзадача пересекается по времени: " + subtask.getTitle());
            }

            subtask.setId(generateId());
            subtasks.put(subtask.getId(), subtask);
            prioritizedTasks.add(subtask);

            epic.addSubtaskId(subtask.getId());
            updateEpicStatus(epic);
            updateEpicTime(epic);
            return subtask.getId();
        }

    // UPDATE
        @Override
        public boolean updateTask(Task updatedTask) {
            if (updatedTask == null || !tasks.containsKey(updatedTask.getId())) return false;


            Task old = tasks.get(updatedTask.getId());
            prioritizedTasks.remove(old);

            if (updatedTask.getStartTime() != null && updatedTask.getDuration() != null && hasOverlap(updatedTask)) {
                prioritizedTasks.add(old);
                throw new IllegalArgumentException("Обновление приводит к пересечению: " + updatedTask.getTitle());
            }

            tasks.put(updatedTask.getId(), updatedTask);
            prioritizedTasks.add(updatedTask);
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

        SubTask old = subtasks.get(updatedSubtask.getId());
        prioritizedTasks.remove(old);

        if (updatedSubtask.getStartTime() != null && updatedSubtask.getDuration() != null && hasOverlap(updatedSubtask)) {
            prioritizedTasks.add(old);
            throw new IllegalArgumentException("Обновление подзадачи приводит к пересечению: " + updatedSubtask.getTitle());
        }

        subtasks.put(updatedSubtask.getId(), updatedSubtask);
        prioritizedTasks.add(updatedSubtask);

        updateEpicStatus(epic);
        updateEpicTime(epic);
        return true;
    }

    // DELETE
    @Override
    public void deleteTask(int id) {
        Task removed = tasks.remove(id);
        if (removed != null) {
            historyManager.remove(id);
            prioritizedTasks.remove(removed);
        }
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            historyManager.remove(epic.getId());
            // удалить все подзадачи эпика
            for (Integer subId : new ArrayList<>(epic.getSubtaskIds())) {
                SubTask removedSub = subtasks.remove(subId);
                if (removedSub != null) {
                    historyManager.remove(subId);
                    prioritizedTasks.remove(removedSub);
                }
            }
        }
    }

    @Override
    public void deleteSubtask(int id) {
        SubTask removedSubtask = subtasks.remove(id);
        if (removedSubtask != null) {
            historyManager.remove(id);
            prioritizedTasks.remove(removedSubtask);

            Epic epic = epics.get(removedSubtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic);
                updateEpicTime(epic);
            }
        }
    }


    @Override
    public void deleteAllSubtasks() {
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.values().forEach(prioritizedTasks::remove);
        subtasks.clear();

        epics.values().forEach(epic -> {
            epic.clearSubtasks();
            updateEpicStatus(epic);
            updateEpicTime(epic);
        });
    }

    @Override
    public void deleteAllEpics() {
        epics.values().forEach(epic -> {
            historyManager.remove(epic.getId());
            epic.getSubtaskIds().forEach(historyManager::remove);
        });
        epics.clear();

        subtasks.values().forEach(prioritizedTasks::remove);
        subtasks.clear();
    }

    @Override
    public void deleteAllTasks() {
        tasks.keySet().forEach(historyManager::remove);
        tasks.values().forEach(prioritizedTasks::remove);
        tasks.clear();
    }

    //GET
    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) historyManager.add(task);
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) historyManager.add(epic);
        return epic;
    }

    @Override
    public SubTask getSubtask(int id) {
        SubTask subtask = subtasks.get(id);
        if (subtask != null) historyManager.add(subtask);
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
    public List<SubTask> getEpicSubtasks(int epicId) {
        return subtasks.values().stream()
                .filter(sub -> sub.getEpicId() == epicId)
                .toList();
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

        List<Status> statuses = epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .map(SubTask::getStatus)
                .toList();

        if (statuses.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        if (statuses.stream().allMatch(s -> s == Status.NEW)) {
            epic.setStatus(Status.NEW);
        } else if (statuses.stream().allMatch(s -> s == Status.DONE)) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private boolean hasOverlap(Task newTask) {
        LocalDateTime start = newTask.getStartTime();
        LocalDateTime end = newTask.getEndTime();
        if (start == null || end == null) return false;

        return prioritizedTasks.stream()
                .filter(t -> t.getId() != newTask.getId())
                .anyMatch(existing -> isOverlapping(existing, start, end));
    }

    private boolean isOverlapping(Task existing, LocalDateTime newStart, LocalDateTime newEnd) {
        if (existing.getStartTime() == null || existing.getDuration() == null) {
            return false;
        }

        LocalDateTime start = existing.getStartTime();
        LocalDateTime end = existing.getEndTime();

        return !(end.isBefore(newStart) || start.isAfter(newEnd));
    }

    protected void validateNoOverlap(Task newTask) {
        if (newTask == null) return;
        if (newTask.getStartTime() == null || newTask.getDuration() == null) return;

        if (hasOverlap(newTask)) {
            throw new IllegalArgumentException("Задачи пересекаются по времени: " + newTask.getTitle());
        }
    }

    protected void updateEpicTime(Epic epic) {
        List<SubTask> subs = getEpicSubtasks(epic.getId());
        if (subs.isEmpty()) {
            epic.setStartTime(null);
            epic.setDuration(Duration.ZERO);
            // установим endTime в null, если есть такой сеттер
            try {
                epic.setEndTime(null);
            } catch (Exception ignored) {}
            return;
        }

        LocalDateTime start = subs.stream()
                .map(SubTask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime end = subs.stream()
                .map(SubTask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        long totalMinutes = subs.stream()
                .map(SubTask::getDuration)
                .filter(Objects::nonNull)
                .mapToLong(Duration::toMinutes)
                .sum();

        epic.setStartTime(start);
        epic.setDuration(Duration.ofMinutes(totalMinutes));
        try {
            epic.setEndTime(end);
        } catch (Exception ignored) {}
    }
}
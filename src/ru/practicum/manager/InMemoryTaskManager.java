package ru.practicum.manager;

import ru.practicum.model.*;

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

    //ADD
    @Override
    public int addTask(Task task) {
        if (task == null) return -1;
        validateNoOverlap(task);

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
        validateNoOverlap(subtask);

        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);
        prioritizedTasks.add(subtask);

        epic.addSubtaskId(subtask.getId());
        updateEpicStatus(epic);
        return subtask.getId();
    }

    // UPDATE
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

    // DELETE
    @Override
    public void deleteTask(int id) {
        if (tasks.remove(id) != null) {
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            historyManager.remove(id);
            for (Integer subId : new ArrayList<>(epic.getSubtaskIds())) {
                subtasks.remove(subId);
                historyManager.remove(subId);
            }
        }
    }

    @Override
    public void deleteSubtask(int id) {
        SubTask removedSubtask = subtasks.remove(id);
        if (removedSubtask != null) {
            historyManager.remove(id);
            Epic epic = epics.get(removedSubtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic);
            }
        }
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.clear();

        epics.values().forEach(epic -> {
            epic.clearSubtasks();
            updateEpicStatus(epic);
        });
    }

    @Override
    public void deleteAllEpics() {
        epics.values().forEach(epic -> {
            historyManager.remove(epic.getId());
            epic.getSubtaskIds().forEach(historyManager::remove);
        });
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteAllTasks() {
        tasks.keySet().forEach(historyManager::remove);
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

        if (statuses.stream().allMatch(s -> s == Status.NEW)) {
            epic.setStatus(Status.NEW);
        } else if (statuses.stream().allMatch(s -> s == Status.DONE)) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }
    protected final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
    );
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
                .anyMatch(t -> {
                    LocalDateTime tStart = t.getStartTime();
                    LocalDateTime tEnd = t.getEndTime();
                    return tStart != null && tEnd != null &&
                            !(end.isBefore(tStart) || start.isAfter(tEnd));
                });
    }
    protected void validateNoOverlap(Task newTask) {
        if (newTask.getStartTime() == null || newTask.getDuration() == null) {
            return;
        }

        LocalDateTime newStart = newTask.getStartTime();
        LocalDateTime newEnd = newTask.getEndTime();


        for (Task existing : tasks.values()) {
            if (isOverlapping(existing, newStart, newEnd)) {
                throw new IllegalArgumentException("Задачи пересекаются по времени: "
                        + existing.getTitle() + " и " + newTask.getTitle());
            }
        }


        for (SubTask existing : subtasks.values()) {
            if (isOverlapping(existing, newStart, newEnd)) {
                throw new IllegalArgumentException("Подзадача пересекается по времени: "
                        + existing.getTitle() + " и " + newTask.getTitle());
            }
        }
    }

    private boolean isOverlapping(Task existing, LocalDateTime newStart, LocalDateTime newEnd) {
        if (existing.getStartTime() == null || existing.getDuration() == null) {
            return false;
        }

        LocalDateTime start = existing.getStartTime();
        LocalDateTime end = existing.getEndTime();

        return !(end.isBefore(newStart) || end.equals(newStart)
                || start.isAfter(newEnd) || start.equals(newEnd));
    }
}

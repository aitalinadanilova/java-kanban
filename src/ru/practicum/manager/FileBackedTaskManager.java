package ru.practicum.manager;

import ru.practicum.exception.ManagerLoadException;
import ru.practicum.exception.ManagerSaveException;
import ru.practicum.model.*;

import java.io.*;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;
    private static final String HEADER = "id,type,name,status,description,epic,duration,startTime";

    public FileBackedTaskManager(File file) {
        super(new InMemoryHistoryManager());
        this.file = file;
    }

    @Override
    public int addTask(Task task) {
        int id = super.addTask(task);
        save();
        return id;
    }

    @Override
    public int addEpic(Epic epic) {
        int id = super.addEpic(epic);
        save();
        return id;
    }

    @Override
    public int addSubTask(SubTask subtask) {
        int id = super.addSubTask(subtask);
        save();
        return id;
    }

    @Override
    public boolean updateTask(Task updatedTask) {
        boolean result = super.updateTask(updatedTask);
        save();
        return result;
    }

    @Override
    public boolean updateEpic(Epic updatedEpic) {
        boolean result = super.updateEpic(updatedEpic);
        save();
        return result;
    }

    @Override
    public boolean updateSubtask(SubTask updatedSubtask) {
        boolean result = super.updateSubtask(updatedSubtask);
        save();
        return result;
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    protected void save() {
        try (Writer writer = new FileWriter(file)) {
            writer.write(HEADER + "\n");

            for (Task task : getAllTasks()) {
                writer.write(toString(task) + "\n");
            }
            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic) + "\n");
            }
            for (SubTask sub : getAllSubTasks()) {
                writer.write(toString(sub) + "\n");
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении данных в файл: " + file.getName(), e);
        }
    }

    private String toString(Task task) {
        String epicId = task instanceof SubTask sub ? String.valueOf(sub.getEpicId()) : "";
        long duration = task.getDuration() == null ? 0 : task.getDuration().toMinutes();
        String startTime = task.getStartTime() == null ? "" : task.getStartTime().toString();

        return String.format("%d,%s,%s,%s,%s,%s,%d,%s",
                task.getId(),
                task.getType(),
                task.getTitle(),
                task.getStatus(),
                task.getDescription(),
                epicId,
                duration,
                startTime
        );
    }

    private Task fromString(String line) {
        String[] parts = line.split(",", -1);

        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];
        String epicIdStr = parts[5];
        long durationMinutes = parts[6].isEmpty() ? 0 : Long.parseLong(parts[6]);
        String startTimeStr = parts[7];
        LocalDateTime startTime = startTimeStr.isEmpty() ? null : LocalDateTime.parse(startTimeStr);

        return switch (type) {
            case TASK -> {
                Task task = new Task(name, description, status);
                task.setId(id);
                task.setDuration(Duration.ofMinutes(durationMinutes));
                task.setStartTime(startTime);
                yield task;
            }
            case EPIC -> {
                Epic epic = new Epic(name, description, status);
                epic.setId(id);
                yield epic;
            }
            case SUBTASK -> {
                int epicId = Integer.parseInt(epicIdStr);
                SubTask sub = new SubTask(name, description, status, epicId);
                sub.setId(id);
                sub.setDuration(Duration.ofMinutes(durationMinutes));
                sub.setStartTime(startTime);
                yield sub;
            }
        };
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.isEmpty() || lines.size() == 1) return manager;
            lines.removeFirst();

            int maxId = 0;

            for (String line : lines) {
                if (line.isBlank()) continue;
                Task task = manager.fromString(line);

                if (task instanceof Epic epic) {
                    manager.epics.put(epic.getId(), epic);
                } else if (task instanceof SubTask sub) {
                    manager.subtasks.put(sub.getId(), sub);
                } else {
                    manager.tasks.put(task.getId(), task);
                }

                if (task.getId() > maxId) maxId = task.getId();
            }

            for (SubTask sub : manager.subtasks.values()) {
                Epic epic = manager.epics.get(sub.getEpicId());
                if (epic != null) epic.addSubtaskId(sub.getId());
            }

            for (Epic epic : manager.epics.values()) {
                manager.updateEpicTime(epic);
            }

            manager.prioritizedTasks.addAll(manager.getAllTasks());
            manager.prioritizedTasks.addAll(manager.getAllSubTasks());

            manager.nextId = maxId + 1;

        } catch (IOException e) {
            throw new ManagerLoadException("Ошибка при загрузке данных из файла: " + file.getName(), e);
        }

        return manager;
    }
}
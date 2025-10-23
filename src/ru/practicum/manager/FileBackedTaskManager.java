package ru.practicum.manager;
import ru.practicum.exception.ManagerLoadException;
import ru.practicum.exception.ManagerSaveException;
import ru.practicum.model.*;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;
    private static final String HEADER = "id,type,name,status,description,epic";

    public FileBackedTaskManager(File file) {
        super(new InMemoryHistoryManager());
        this.file = file;
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
        String base = String.format("%d,%s,%s,%s,%s",
                task.getId(),
                task.getType(),
                task.getTitle(),
                task.getStatus(),
                task.getDescription());

        if (task instanceof SubTask) {
            return base + "," + ((SubTask) task).getEpicId();
        }
        return base + ",";
    }

    private Task fromString(String line) {
        String[] parts = line.split(",", -1);
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];

        return switch (type) {
            case TASK -> {
                Task task = new Task(name, description, status);
                task.setId(id);
                yield task;
            }
            case EPIC -> {
                Epic epic = new Epic(name, description, status);
                epic.setId(id);
                yield epic;
            }
            case SUBTASK -> {
                int epicId = Integer.parseInt(parts[5]);
                SubTask sub = new SubTask(name, description, status, epicId);
                sub.setId(id);
                yield sub;
            }
        };
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.isEmpty() || lines.size() == 1) return manager; // пустой файл
            lines.removeFirst();

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
            }

            for (SubTask sub : manager.subtasks.values()) {
                Epic epic = manager.epics.get(sub.getEpicId());
                if (epic != null) {
                    epic.addSubtaskId(sub.getId());
                }
            }

            int maxId = 0;
            for (int id : manager.getAllIds()) {
                if (id > maxId) maxId = id;
            }
            manager.nextId = maxId + 1;

        } catch (IOException e) {
            throw new ManagerLoadException("Ошибка при загрузке данных из файла: " + file.getName(), e);
        }
        return manager;
    }
}
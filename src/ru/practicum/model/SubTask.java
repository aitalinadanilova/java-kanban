package ru.practicum.model;

import java.time.Duration;
import java.time.LocalDateTime;

public class SubTask extends Task {
    private final int epicId;

    public SubTask(String title, String description, Status status, int epicId) {
        super(title, description, status, Duration.ofMinutes(10), LocalDateTime.now());
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return super.toString() + " (эпик " + epicId + ")";
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

}
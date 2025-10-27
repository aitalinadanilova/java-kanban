package ru.practicum.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private LocalDateTime endTime;
    private final List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String title, String description, Status status) {
        super(title, description, status);
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(int id) {
        subtaskIds.add(id);
    }

    public void removeSubtaskId(int id) {
        subtaskIds.remove((Integer) id);
    }

    public void clearSubtasks() {
        subtaskIds.clear();
    }

    @Override
    public String toString() {
        return super.toString() + " | Подзадачи: " + subtaskIds;
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }


    @Override
    public Duration getDuration() {
        return duration == null ? Duration.ZERO : duration;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        super.setStartTime(startTime);
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}


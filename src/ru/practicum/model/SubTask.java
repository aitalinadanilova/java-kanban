package ru.practicum.model;

public class SubTask extends Task {
    private final int epicId;

    public SubTask(String title, String description, Status status, int epicId) {
        super(title, description, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return super.toString() + " (эпик " + epicId + ")";
    }
}
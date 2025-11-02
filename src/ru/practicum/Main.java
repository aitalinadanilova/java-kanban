package ru.practicum;

import ru.practicum.model.Task;
import ru.practicum.model.Epic;
import ru.practicum.model.SubTask;
import ru.practicum.model.Status;
import ru.practicum.manager.*;

import java.time.Duration;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        Task task1 = new Task("Переезд", "Собрать вещи", Status.NEW, Duration.ofMinutes(10), LocalDateTime.now());
        Task task2 = new Task("Учёба", "Закончить проект", Status.IN_PROGRESS, Duration.ofMinutes(10), LocalDateTime.now());
        manager.addTask(task1);
        manager.addTask(task2);

        Epic epic1 = new Epic("Праздник", "Организовать вечеринку", Status.NEW);
        manager.addEpic(epic1);

        SubTask sub1 = new SubTask("Купить еду", "Закупка продуктов", Status.NEW, epic1.getId());
        SubTask sub2 = new SubTask("Украшения", "Оформить зал", Status.NEW, epic1.getId());
        manager.addSubTask(sub1);
        manager.addSubTask(sub2);

        Epic epic2 = new Epic("Ремонт", "Обновить кухню", Status.NEW);
        manager.addEpic(epic2);
        SubTask sub3 = new SubTask("Покрасить стены", "Выбрать цвет", Status.DONE, epic2.getId());
        manager.addSubTask(sub3);

        System.out.println("\nВсе задачи:");
        System.out.println(manager.getAllTasks());

        System.out.println("\nВсе эпики:");
        System.out.println(manager.getAllEpics());

        System.out.println("\nВсе подзадачи:");
        System.out.println(manager.getAllSubTasks());

        task2.setStatus(Status.DONE);
        manager.updateTask(task2);

        sub1.setStatus(Status.DONE);
        manager.updateSubtask(sub1);
        System.out.println("\nПосле перевода sub1 в DONE:");

        sub2.setStatus(Status.DONE);
        manager.updateSubtask(sub2);
        System.out.println("\nПосле перевода sub2 в DONE:");


        System.out.println("\nПроверка эпика2 (должен быть DONE):");

        System.out.println("\n Списки после обновления статусов ");
        System.out.println("Задачи: " + manager.getAllTasks());
        System.out.println("Эпики: " + manager.getAllEpics());
        System.out.println("Подзадачи: " + manager.getAllSubTasks());

        manager.deleteTask(task1.getId());
        manager.deleteEpic(epic1.getId());

        System.out.println("\nПосле удаления:");
        System.out.println("Задачи: " + manager.getAllTasks());
        System.out.println("Эпики: " + manager.getAllEpics());
        System.out.println("Подзадачи: " + manager.getAllSubTasks());


    }
}

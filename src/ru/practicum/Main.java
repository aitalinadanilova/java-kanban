package ru.practicum;

import ru.practicum.model.Task;
import ru.practicum.model.Epic;
import ru.practicum.model.SubTask;
import ru.practicum.model.Status;
import ru.practicum.manager.*;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        // Создаём обычные задачи
        Task task1 = new Task("Переезд", "Собрать вещи", Status.NEW);
        Task task2 = new Task("Учёба", "Закончить проект", Status.IN_PROGRESS);
        manager.addTask(task1);
        manager.addTask(task2);

        // Эпик с подзадачами
        Epic epic1 = new Epic("Праздник", "Организовать вечеринку", Status.NEW);
        manager.addEpic(epic1);

        SubTask sub1 = new SubTask("Купить еду", "Закупка продуктов", Status.NEW, epic1.getId());
        SubTask sub2 = new SubTask("Украшения", "Оформить зал", Status.NEW, epic1.getId());
        manager.addSubTask(sub1);
        manager.addSubTask(sub2);

        // Эпик с одной подзадачей
        Epic epic2 = new Epic("Ремонт", "Обновить кухню", Status.NEW);
        manager.addEpic(epic2);
        SubTask sub3 = new SubTask("Покрасить стены", "Выбрать цвет", Status.DONE, epic2.getId());
        manager.addSubTask(sub3);

        // Вывод всех
        System.out.println("\nВсе задачи:");
        System.out.println(manager.getAllTasks());

        System.out.println("\nВсе эпики:");
        System.out.println(manager.getAllEpics());

        System.out.println("\nВсе подзадачи:");
        System.out.println(manager.getAllSubTasks());

        // Меняем статусы и проверяем перерасчёт эпиков
        task2.setStatus(Status.DONE);
        manager.updateTask(task2);

        sub1.setStatus(Status.DONE);
        manager.updateSubtask(sub1);
        System.out.println("\nПосле перевода sub1 в DONE:");
        System.out.println("Эпик1: " + manager.getEpicById(epic1.getId())); // ожидаем IN_PROGRESS

        sub2.setStatus(Status.DONE);
        manager.updateSubtask(sub2);
        System.out.println("\nПосле перевода sub2 в DONE:");
        System.out.println("Эпик1: " + manager.getEpicById(epic1.getId())); // ожидаем DONE

        System.out.println("\nПроверка эпика2 (должен быть DONE):");
        System.out.println("Эпик2: " + manager.getEpicById(epic2.getId()));

        // Печатаем обновлённые списки
        System.out.println("\n Списки после обновления статусов ");
        System.out.println("Задачи: " + manager.getAllTasks());
        System.out.println("Эпики: " + manager.getAllEpics());
        System.out.println("Подзадачи: " + manager.getAllSubTasks());

        // Удаление
        manager.deleteTask(task1.getId());   // удаляем task1
        manager.deleteEpic(epic1.getId());   // удаляем epic

        System.out.println("\nПосле удаления:");
        System.out.println("Задачи: " + manager.getAllTasks());
        System.out.println("Эпики: " + manager.getAllEpics());
        System.out.println("Подзадачи: " + manager.getAllSubTasks());


    }
}

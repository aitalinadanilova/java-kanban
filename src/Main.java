public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        // Создаём обычные задачи
        Task task1 = new Task("Переезд", "Собрать вещи", Status.NEW);
        Task task2 = new Task("Учёба", "Закончить проект", Status.IN_PROGRESS);
        manager.addTask(task1);
        manager.addTask(task2);

        // Эпик с подзадачами
        Epic epic1 = new Epic("Праздник", "Организовать вечеринку");
        manager.addEpic(epic1);

        SubTask sub1 = new SubTask("Купить еду", "Закупка продуктов", Status.NEW, epic1.getId());
        SubTask sub2 = new SubTask("Украшения", "Оформить зал", Status.NEW, epic1.getId());
        manager.addSubTask(sub1);
        manager.addSubTask(sub2);

        // Эпик с одной подзадачей
        Epic epic2 = new Epic("Ремонт", "Обновить кухню");
        manager.addEpic(epic2);
        SubTask sub3 = new SubTask("Покрасить стены", "Выбрать цвет", Status.DONE, epic2.getId());
        manager.addSubTask(sub3);

        // Вывод
        System.out.println("\n Все задачи ");
        System.out.println(manager.getAllTasks());

        System.out.println("\n Все эпики ");
        System.out.println(manager.getAllEpics());

        System.out.println("\n Все подзадачи ");
        System.out.println(manager.getAllSubTasks());

        // Меняем статус подзадачи и проверяем эпик
        sub1.setStatus(Status.DONE);
        manager.updateSubtask(sub1);

        System.out.println("\nПосле изменения статуса подзадачи:");
        System.out.println(manager.getEpicById(epic1.getId()));
    }
}

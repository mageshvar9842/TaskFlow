package controller;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import model.Priority;
import model.Task;
import model.TaskStatus;
import util.StorageManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * TaskManager handles all CRUD operations and reminder functionality
 */
public class TaskManager {
    private List<Task> tasks;
    private final StorageManager storageManager;
    private final ScheduledExecutorService scheduler;
    private final Stack<TaskAction> undoStack;

    public TaskManager() {
        this.storageManager = new StorageManager();
        this.tasks = storageManager.loadTasks();
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.undoStack = new Stack<>();
        startReminderService();
    }

    /**
     * Add a new task
     */
    public void addTask(Task task) {
        tasks.add(task);
        undoStack.push(new TaskAction(ActionType.ADD, task));
        autoSave();
    }

    /**
     * Update an existing task
     */
    public void updateTask(Task task) {
        int index = findTaskIndex(task.getId());
        if (index != -1) {
            Task oldTask = tasks.get(index).clone();
            tasks.set(index, task);
            undoStack.push(new TaskAction(ActionType.UPDATE, task, oldTask));
            autoSave();
        }
    }

    /**
     * Delete a task
     */
    public void deleteTask(Task task) {
        tasks.remove(task);
        undoStack.push(new TaskAction(ActionType.DELETE, task));
        autoSave();
    }

    /**
     * Undo last action
     */
    public void undoLastAction() {
        if (undoStack.isEmpty()) {
            return;
        }

        TaskAction action = undoStack.pop();
        switch (action.type) {
            case ADD:
                tasks.remove(action.task);
                break;
            case DELETE:
                tasks.add(action.task);
                break;
            case UPDATE:
                int index = findTaskIndex(action.task.getId());
                if (index != -1 && action.oldTask != null) {
                    tasks.set(index, action.oldTask);
                }
                break;
        }
        autoSave();
    }

    /**
     * Get all tasks
     */
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks);
    }

    /**
     * Get tasks by status
     */
    public List<Task> getTasksByStatus(TaskStatus status) {
        return tasks.stream()
                .filter(task -> task.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * Get tasks by priority
     */
    public List<Task> getTasksByPriority(Priority priority) {
        return tasks.stream()
                .filter(task -> task.getPriority() == priority)
                .collect(Collectors.toList());
    }

    /**
     * Get today's tasks
     */
    public List<Task> getTodaysTasks() {
        return tasks.stream()
                .filter(Task::isDueToday)
                .collect(Collectors.toList());
    }

    /**
     * Get overdue tasks
     */
    public List<Task> getOverdueTasks() {
        return tasks.stream()
                .filter(Task::isOverdue)
                .collect(Collectors.toList());
    }

    /**
     * Search tasks by title
     */
    public List<Task> searchTasks(String query) {
        String lowerQuery = query.toLowerCase();
        return tasks.stream()
                .filter(task -> task.getTitle().toLowerCase().contains(lowerQuery) ||
                        (task.getDescription() != null && task.getDescription().toLowerCase().contains(lowerQuery)))
                .collect(Collectors.toList());
    }

    /**
     * Filter tasks by date
     */
    public List<Task> filterByDate(LocalDate date) {
        return tasks.stream()
                .filter(task -> task.getDueDate() != null && task.getDueDate().equals(date))
                .collect(Collectors.toList());
    }

    /**
     * Get task completion percentage
     */
    public int getCompletionPercentage() {
        if (tasks.isEmpty()) {
            return 0;
        }
        long completed = tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        return (int) ((completed * 100) / tasks.size());
    }

    /**
     * Auto-save tasks
     */
    private void autoSave() {
        storageManager.saveTasks(tasks);
    }

    /**
     * Start reminder service to check for upcoming tasks
     */
    private void startReminderService() {
        scheduler.scheduleAtFixedRate(() -> {
            LocalDateTime now = LocalDateTime.now();
            for (Task task : tasks) {
                if (task.getDueDate() != null && task.getDueTime() != null && 
                    !task.isReminderSent() && task.getStatus() != TaskStatus.COMPLETED) {
                    
                    LocalDateTime taskDateTime = LocalDateTime.of(task.getDueDate(), task.getDueTime());
                    long minutesUntil = ChronoUnit.MINUTES.between(now, taskDateTime);
                    
                    // Remind 15 minutes before
                    if (minutesUntil <= 15 && minutesUntil >= 0) {
                        sendReminder(task);
                        task.setReminderSent(true);
                        autoSave();
                    }
                }
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    /**
     * Send reminder notification
     */
    private void sendReminder(Task task) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Task Reminder");
            alert.setHeaderText("Upcoming Task!");
            alert.setContentText("Task: " + task.getTitle() + "\nDue at: " + task.getDueTime());
            alert.show();
        });
    }

    /**
     * Find task index by ID
     */
    private int findTaskIndex(String id) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Shutdown scheduler
     */
    public void shutdown() {
        scheduler.shutdown();
    }

    /**
     * Inner class for undo functionality
     */
    private static class TaskAction {
        ActionType type;
        Task task;
        Task oldTask;

        TaskAction(ActionType type, Task task) {
            this.type = type;
            this.task = task;
        }

        TaskAction(ActionType type, Task task, Task oldTask) {
            this.type = type;
            this.task = task;
            this.oldTask = oldTask;
        }
    }

    private enum ActionType {
        ADD, UPDATE, DELETE
    }
}

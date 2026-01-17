package model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Task model representing a complete task with all properties
 */
public class Task {
    private String id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private LocalTime dueTime;
    private Priority priority;
    private TaskStatus status;
    private List<SubTask> subTasks;
    private LocalDate createdDate;
    private boolean reminderSent;

    public Task() {
        this.id = UUID.randomUUID().toString();
        this.status = TaskStatus.PENDING;
        this.priority = Priority.MEDIUM;
        this.subTasks = new ArrayList<>();
        this.createdDate = LocalDate.now();
        this.reminderSent = false;
    }

    public Task(String title, String description) {
        this();
        this.title = title;
        this.description = description;
    }

    /**
     * Check if the task is overdue
     */
    public boolean isOverdue() {
        if (dueDate == null || status == TaskStatus.COMPLETED) {
            return false;
        }
        LocalDate today = LocalDate.now();
        return dueDate.isBefore(today);
    }

    /**
     * Check if the task is due today
     */
    public boolean isDueToday() {
        if (dueDate == null) {
            return false;
        }
        return dueDate.isEqual(LocalDate.now());
    }

    /**
     * Check if all sub-tasks are completed
     */
    public boolean areAllSubTasksCompleted() {
        if (subTasks.isEmpty()) {
            return true;
        }
        return subTasks.stream().allMatch(SubTask::isCompleted);
    }

    /**
     * Get completion percentage of sub-tasks
     */
    public int getSubTaskCompletionPercentage() {
        if (subTasks.isEmpty()) {
            return 0;
        }
        long completedCount = subTasks.stream().filter(SubTask::isCompleted).count();
        return (int) ((completedCount * 100) / subTasks.size());
    }

    /**
     * Add a sub-task
     */
    public void addSubTask(SubTask subTask) {
        subTasks.add(subTask);
    }

    /**
     * Remove a sub-task
     */
    public void removeSubTask(SubTask subTask) {
        subTasks.remove(subTask);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalTime getDueTime() {
        return dueTime;
    }

    public void setDueTime(LocalTime dueTime) {
        this.dueTime = dueTime;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public List<SubTask> getSubTasks() {
        return subTasks;
    }

    public void setSubTasks(List<SubTask> subTasks) {
        this.subTasks = subTasks;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public boolean isReminderSent() {
        return reminderSent;
    }

    public void setReminderSent(boolean reminderSent) {
        this.reminderSent = reminderSent;
    }

    @Override
    public String toString() {
        return title + " [" + status.getDisplayName() + "]";
    }

    /**
     * Create a deep copy of the task
     */
    public Task clone() {
        Task cloned = new Task();
        cloned.setId(this.id);
        cloned.setTitle(this.title);
        cloned.setDescription(this.description);
        cloned.setDueDate(this.dueDate);
        cloned.setDueTime(this.dueTime);
        cloned.setPriority(this.priority);
        cloned.setStatus(this.status);
        cloned.setCreatedDate(this.createdDate);
        cloned.setReminderSent(this.reminderSent);
        
        // Deep copy subtasks
        List<SubTask> clonedSubTasks = new ArrayList<>();
        for (SubTask st : this.subTasks) {
            SubTask clonedSt = new SubTask();
            clonedSt.setId(st.getId());
            clonedSt.setTitle(st.getTitle());
            clonedSt.setCompleted(st.isCompleted());
            clonedSubTasks.add(clonedSt);
        }
        cloned.setSubTasks(clonedSubTasks);
        
        return cloned;
    }
}

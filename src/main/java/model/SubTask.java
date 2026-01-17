package model;

import java.util.UUID;

/**
 * SubTask model representing a sub-task within a main task
 */
public class SubTask {
    private String id;
    private String title;
    private boolean completed;

    public SubTask() {
        this.id = UUID.randomUUID().toString();
        this.completed = false;
    }

    public SubTask(String title) {
        this();
        this.title = title;
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

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public String toString() {
        return (completed ? "✓ " : "○ ") + title;
    }
}

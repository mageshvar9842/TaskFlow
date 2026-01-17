package ui;

import javafx.animation.RotateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import model.SubTask;
import model.Task;
import model.TaskStatus;

/**
 * TaskCard component for displaying individual tasks
 */
public class TaskCard extends VBox {
    private final Task task;
    private final MainApplication app;
    private VBox subTasksContainer;
    private Button expandButton;
    private boolean expanded = false;

    public TaskCard(Task task, MainApplication app) {
        this.task = task;
        this.app = app;
        
        setupCard();
    }

    private void setupCard() {
        this.getStyleClass().add("task-card");
        this.setPadding(new Insets(20));
        this.setSpacing(12);
        
        // Apply special styles for overdue/today
        if (task.isOverdue()) {
            this.getStyleClass().add("overdue-card");
        } else if (task.isDueToday()) {
            this.getStyleClass().add("today-card");
        }

        // Header row with title and priority
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(task.getTitle());
        titleLabel.getStyleClass().add("task-title");
        
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        // Priority badge
        Label priorityBadge = new Label(task.getPriority().getDisplayName());
        priorityBadge.getStyleClass().addAll("priority-badge", 
                                             "priority-" + task.getPriority().name().toLowerCase());

        // Status badge
        Label statusBadge = new Label(task.getStatus().getDisplayName());
        statusBadge.getStyleClass().addAll("status-badge", 
                                           "status-" + task.getStatus().name().toLowerCase().replace("_", "-"));

        headerRow.getChildren().addAll(titleLabel, spacer1, priorityBadge, statusBadge);

        // Description
        VBox contentBox = new VBox(8);
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            Label descLabel = new Label(task.getDescription());
            descLabel.getStyleClass().add("task-description");
            descLabel.setWrapText(true);
            descLabel.setMaxWidth(Double.MAX_VALUE);
            contentBox.getChildren().add(descLabel);
        }

        // Meta information
        HBox metaBox = new HBox(15);
        metaBox.setAlignment(Pos.CENTER_LEFT);

        if (task.getDueDate() != null) {
            Label dateLabel = new Label("📅 " + task.getDueDate().toString());
            dateLabel.getStyleClass().add("task-meta");
            metaBox.getChildren().add(dateLabel);
        }

        if (task.getDueTime() != null) {
            Label timeLabel = new Label("⏰ " + task.getDueTime().toString());
            timeLabel.getStyleClass().add("task-meta");
            metaBox.getChildren().add(timeLabel);
        }

        if (!task.getSubTasks().isEmpty()) {
            int completed = (int) task.getSubTasks().stream().filter(SubTask::isCompleted).count();
            Label subTaskLabel = new Label("✓ " + completed + "/" + task.getSubTasks().size() + " subtasks");
            subTaskLabel.getStyleClass().add("task-meta");
            metaBox.getChildren().add(subTaskLabel);
        }

        contentBox.getChildren().add(metaBox);

        // Sub-tasks section (collapsible)
        if (!task.getSubTasks().isEmpty()) {
            HBox expandBox = new HBox(5);
            expandBox.setAlignment(Pos.CENTER_LEFT);
            
            expandButton = new Button("▶");
            expandButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
            expandButton.setOnAction(e -> toggleSubTasks());
            
            Label expandLabel = new Label("Show subtasks");
            expandLabel.setStyle("-fx-cursor: hand;");
            expandLabel.setOnMouseClicked(e -> toggleSubTasks());
            
            expandBox.getChildren().addAll(expandButton, expandLabel);
            contentBox.getChildren().add(expandBox);

            subTasksContainer = new VBox(8);
            subTasksContainer.setPadding(new Insets(10, 0, 0, 20));
            subTasksContainer.setManaged(false);
            subTasksContainer.setVisible(false);
            
            for (SubTask subTask : task.getSubTasks()) {
                CheckBox subTaskCheck = new CheckBox(subTask.getTitle());
                subTaskCheck.setSelected(subTask.isCompleted());
                subTaskCheck.setOnAction(e -> {
                    subTask.setCompleted(subTaskCheck.isSelected());
                    
                    // Auto-complete main task if all subtasks done
                    if (task.areAllSubTasksCompleted() && task.getStatus() != TaskStatus.COMPLETED) {
                        task.setStatus(TaskStatus.COMPLETED);
                    }
                    
                    app.getTaskManager().updateTask(task);
                    app.refreshTaskList();
                });
                subTasksContainer.getChildren().add(subTaskCheck);
            }
            
            contentBox.getChildren().add(subTasksContainer);
        }

        // Action buttons
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_LEFT);

        Button editBtn = new Button("✏ Edit");
        editBtn.getStyleClass().addAll("btn", "btn-primary");
        editBtn.setOnAction(e -> editTask());

        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.getStyleClass().addAll("btn", "btn-danger");
        deleteBtn.setOnAction(e -> deleteTask());

        // Status change buttons
        if (task.getStatus() != TaskStatus.COMPLETED) {
            Button completeBtn = new Button("✓ Complete");
            completeBtn.getStyleClass().addAll("btn", "btn-success");
            completeBtn.setOnAction(e -> {
                task.setStatus(TaskStatus.COMPLETED);
                app.getTaskManager().updateTask(task);
                app.refreshTaskList();
            });
            actionBox.getChildren().add(completeBtn);
        }

        if (task.getStatus() == TaskStatus.PENDING) {
            Button startBtn = new Button("▶ Start");
            startBtn.getStyleClass().addAll("btn", "btn-secondary");
            startBtn.setOnAction(e -> {
                task.setStatus(TaskStatus.IN_PROGRESS);
                app.getTaskManager().updateTask(task);
                app.refreshTaskList();
            });
            actionBox.getChildren().add(startBtn);
        }

        actionBox.getChildren().addAll(editBtn, deleteBtn);

        // Add all to card
        this.getChildren().addAll(headerRow, contentBox, actionBox);
    }

    private void toggleSubTasks() {
        expanded = !expanded;
        subTasksContainer.setManaged(expanded);
        subTasksContainer.setVisible(expanded);
        
        RotateTransition rotate = new RotateTransition(Duration.millis(200), expandButton);
        rotate.setToAngle(expanded ? 90 : 0);
        rotate.play();
    }

    private void editTask() {
        TaskDialog dialog = new TaskDialog(app.getScene().getWindow(), task, app.getTaskManager());
        dialog.showAndWait().ifPresent(updatedTask -> {
            app.getTaskManager().updateTask(updatedTask);
            app.refreshTaskList();
        });
    }

    private void deleteTask() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Task");
        alert.setHeaderText("Are you sure you want to delete this task?");
        alert.setContentText(task.getTitle());
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                app.getTaskManager().deleteTask(task);
                app.refreshTaskList();
            }
        });
    }
}

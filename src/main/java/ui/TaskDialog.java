package ui;

import controller.TaskManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Callback;
import model.Priority;
import model.SubTask;
import model.Task;
import model.TaskStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Dialog for creating and editing tasks
 */
public class TaskDialog extends Dialog<Task> {
    private final Task task;
    private final boolean isEdit;
    private final TaskManager taskManager;
    
    private TextField titleField;
    private TextArea descriptionArea;
    private DatePicker datePicker;
    private TextField timeField;
    private ComboBox<Priority> priorityCombo;
    private ComboBox<TaskStatus> statusCombo;
    private VBox subTasksBox;
    private List<SubTaskRow> subTaskRows;

    public TaskDialog(Window owner, Task task, TaskManager taskManager) {
        this.task = task;
        this.isEdit = (task != null);
        this.taskManager = taskManager;
        this.subTaskRows = new ArrayList<>();
        
        initDialog(owner);
    }

    private void initDialog(Window owner) {
        setTitle(isEdit ? "Edit Task" : "Create New Task");
        setHeaderText(isEdit ? "Update task details" : "Enter task details");
        
        initOwner(owner);

        ButtonType saveButtonType = new ButtonType(isEdit ? "Update" : "Create", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        // Title
        titleField = new TextField();
        titleField.setPromptText("Task title");
        titleField.setPrefWidth(400);
        if (isEdit) titleField.setText(task.getTitle());

        // Description
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Task description (optional)");
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setWrapText(true);
        if (isEdit && task.getDescription() != null) {
            descriptionArea.setText(task.getDescription());
        }

        // Due Date
        datePicker = new DatePicker();
        datePicker.setPromptText("Select due date");
        if (isEdit && task.getDueDate() != null) {
            datePicker.setValue(task.getDueDate());
        }

        // Due Time
        timeField = new TextField();
        timeField.setPromptText("HH:MM (e.g., 14:30)");
        if (isEdit && task.getDueTime() != null) {
            timeField.setText(task.getDueTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        }

        // Priority
        priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll(Priority.values());
        priorityCombo.setValue(isEdit ? task.getPriority() : Priority.MEDIUM);
        priorityCombo.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Priority> call(ListView<Priority> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Priority item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getDisplayName());
                        }
                    }
                };
            }
        });
        priorityCombo.setButtonCell(priorityCombo.getCellFactory().call(null));

        // Status
        statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll(TaskStatus.values());
        statusCombo.setValue(isEdit ? task.getStatus() : TaskStatus.PENDING);
        statusCombo.setCellFactory(new Callback<>() {
            @Override
            public ListCell<TaskStatus> call(ListView<TaskStatus> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(TaskStatus item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getDisplayName());
                        }
                    }
                };
            }
        });
        statusCombo.setButtonCell(statusCombo.getCellFactory().call(null));

        // Sub-tasks section
        Label subTaskLabel = new Label("Sub-tasks:");
        subTaskLabel.setStyle("-fx-font-weight: bold;");
        
        subTasksBox = new VBox(10);
        subTasksBox.setPadding(new Insets(10));
        subTasksBox.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        if (isEdit) {
            for (SubTask st : task.getSubTasks()) {
                addSubTaskRow(st);
            }
        }

        Button addSubTaskBtn = new Button("+ Add Sub-task");
        addSubTaskBtn.getStyleClass().addAll("btn", "btn-secondary");
        addSubTaskBtn.setOnAction(e -> addSubTaskRow(null));

        // Add all to grid
        int row = 0;
        grid.add(new Label("Title:"), 0, row);
        grid.add(titleField, 1, row++);
        
        grid.add(new Label("Description:"), 0, row);
        grid.add(descriptionArea, 1, row++);
        
        grid.add(new Label("Due Date:"), 0, row);
        grid.add(datePicker, 1, row++);
        
        grid.add(new Label("Due Time:"), 0, row);
        grid.add(timeField, 1, row++);
        
        grid.add(new Label("Priority:"), 0, row);
        grid.add(priorityCombo, 1, row++);
        
        grid.add(new Label("Status:"), 0, row);
        grid.add(statusCombo, 1, row++);
        
        grid.add(subTaskLabel, 0, row);
        grid.add(subTasksBox, 1, row++);
        grid.add(addSubTaskBtn, 1, row);

        getDialogPane().setContent(grid);

        // Enable/disable save button based on title
        Button saveButton = (Button) getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);
        titleField.textProperty().addListener((obs, old, newVal) -> {
            saveButton.setDisable(newVal.trim().isEmpty());
        });

        // Result converter
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return createTaskFromInput();
            }
            return null;
        });
    }

    private void addSubTaskRow(SubTask existingSubTask) {
        SubTaskRow row = new SubTaskRow(existingSubTask);
        subTaskRows.add(row);
        subTasksBox.getChildren().add(row);
    }

    private Task createTaskFromInput() {
        Task result = isEdit ? task : new Task();
        
        result.setTitle(titleField.getText().trim());
        result.setDescription(descriptionArea.getText().trim());
        result.setDueDate(datePicker.getValue());
        
        // Parse time
        if (!timeField.getText().trim().isEmpty()) {
            try {
                result.setDueTime(LocalTime.parse(timeField.getText().trim()));
            } catch (Exception e) {
                // Invalid time format, skip
            }
        }
        
        result.setPriority(priorityCombo.getValue());
        result.setStatus(statusCombo.getValue());
        
        // Collect sub-tasks
        List<SubTask> subTasks = new ArrayList<>();
        for (SubTaskRow row : subTaskRows) {
            String subTaskTitle = row.getSubTaskTitle();
            if (!subTaskTitle.isEmpty()) {
                SubTask st = row.getSubTask() != null ? row.getSubTask() : new SubTask();
                st.setTitle(subTaskTitle);
                st.setCompleted(row.isCompleted());
                subTasks.add(st);
            }
        }
        result.setSubTasks(subTasks);
        
        return result;
    }

    /**
     * Inner class for sub-task rows
     */
    private class SubTaskRow extends HBox {
        private final SubTask subTask;
        private final TextField subTaskField;
        private final CheckBox completedCheck;

        public SubTaskRow(SubTask subTask) {
            this.subTask = subTask;
            this.setSpacing(10);
            this.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            subTaskField = new TextField();
            subTaskField.setPromptText("Sub-task title");
            subTaskField.setPrefWidth(300);
            if (subTask != null) {
                subTaskField.setText(subTask.getTitle());
            }

            completedCheck = new CheckBox("Completed");
            if (subTask != null) {
                completedCheck.setSelected(subTask.isCompleted());
            }

            Button removeBtn = new Button("✖");
            removeBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white;");
            removeBtn.setOnAction(e -> {
                subTaskRows.remove(this);
                subTasksBox.getChildren().remove(this);
            });

            this.getChildren().addAll(subTaskField, completedCheck, removeBtn);
        }

        public SubTask getSubTask() {
            return subTask;
        }

        public String getSubTaskTitle() {
            return subTaskField.getText().trim();
        }

        public boolean isCompleted() {
            return completedCheck.isSelected();
        }
    }
}

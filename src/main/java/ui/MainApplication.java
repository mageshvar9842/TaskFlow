package ui;

import controller.TaskManager;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Task;
import model.TaskStatus;
import util.StorageManager;
import util.UserPreferences;

import java.time.LocalTime;
import java.util.List;

/**
 * Main JavaFX Application for Task Flow
 */
public class MainApplication extends Application {
    private TaskManager taskManager;
    private StorageManager storageManager;
    private UserPreferences userPreferences;
    private VBox taskListContainer;
    private Label greetingLabel;
    private Label progressLabel;
    private ProgressBar progressBar;
    private Scene scene;
    private boolean isDarkMode = false;
    private TextField searchField;
    private ComboBox<String> filterCombo;
    private CheckBox focusModeCheckBox;

    @Override
    public void start(Stage primaryStage) {
        taskManager = new TaskManager();
        storageManager = new StorageManager();
        userPreferences = storageManager.loadUserPrefs();
        isDarkMode = userPreferences.isDarkMode();

        primaryStage.setTitle("Task Flow - Modern To-Do List");
        
        BorderPane root = new BorderPane();
        root.getStyleClass().add("main-container");

        // Top: Header with greeting and controls
        VBox header = createHeader();
        root.setTop(header);

        // Center: Task list with scroll
        ScrollPane scrollPane = createTaskListPane();
        root.setCenter(scrollPane);

        // Bottom: Statistics
        VBox stats = createStatsPane();
        root.setBottom(stats);

        // Floating Action Button
        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(root);
        Button fab = createFloatingActionButton(primaryStage);
        stackPane.getChildren().add(fab);
        StackPane.setAlignment(fab, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(fab, new Insets(0, 32, 32, 0));

        scene = new Scene(stackPane, 1200, 800);
        applyTheme();
        
        primaryStage.setScene(scene);
        primaryStage.show();

        // Load and display tasks
        refreshTaskList();
    }

    /**
     * Create header with greeting and controls
     */
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.getStyleClass().add("header");
        header.setPadding(new Insets(20));

        // Greeting section
        HBox greetingBox = new HBox(20);
        greetingBox.setAlignment(Pos.CENTER_LEFT);

        VBox greetingTextBox = new VBox(5);
        greetingLabel = new Label(getGreeting());
        greetingLabel.getStyleClass().add("greeting-label");
        
        Label subGreeting = new Label("Let's organize your day");
        subGreeting.getStyleClass().add("subgreeting-label");
        
        greetingTextBox.getChildren().addAll(greetingLabel, subGreeting);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Controls
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_RIGHT);

        // Theme toggle
        Button themeToggle = new Button(isDarkMode ? "☀" : "🌙");
        themeToggle.getStyleClass().add("btn");
        themeToggle.getStyleClass().add("btn-secondary");
        themeToggle.setOnAction(e -> toggleTheme());

        // Settings button
        Button settingsBtn = new Button("⚙");
        settingsBtn.getStyleClass().add("btn");
        settingsBtn.getStyleClass().add("btn-secondary");
        settingsBtn.setOnAction(e -> showSettingsDialog());

        // Undo button
        Button undoBtn = new Button("↶");
        undoBtn.getStyleClass().add("btn");
        undoBtn.getStyleClass().add("btn-secondary");
        undoBtn.setOnAction(e -> {
            taskManager.undoLastAction();
            refreshTaskList();
        });

        controls.getChildren().addAll(undoBtn, themeToggle, settingsBtn);
        greetingBox.getChildren().addAll(greetingTextBox, spacer, controls);

        // Search and filter section
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(10, 0, 0, 0));

        searchField = new TextField();
        searchField.setPromptText("🔍 Search tasks...");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, old, newVal) -> filterTasks());

        filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("All Tasks", "Today", "Overdue", "High Priority", 
                                      "Medium Priority", "Low Priority", "Pending", 
                                      "In Progress", "Completed");
        filterCombo.setValue("All Tasks");
        filterCombo.setOnAction(e -> filterTasks());

        focusModeCheckBox = new CheckBox("Focus Mode");
        focusModeCheckBox.setSelected(userPreferences.isFocusMode());
        focusModeCheckBox.setOnAction(e -> {
            userPreferences.setFocusMode(focusModeCheckBox.isSelected());
            storageManager.saveUserPrefs(userPreferences);
            filterTasks();
        });

        searchBox.getChildren().addAll(searchField, filterCombo, focusModeCheckBox);

        header.getChildren().addAll(greetingBox, searchBox);
        return header;
    }

    /**
     * Create scrollable task list pane
     */
    private ScrollPane createTaskListPane() {
        taskListContainer = new VBox(15);
        taskListContainer.setPadding(new Insets(20));

        ScrollPane scrollPane = new ScrollPane(taskListContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        return scrollPane;
    }

    /**
     * Create statistics pane
     */
    private VBox createStatsPane() {
        VBox statsBox = new VBox(10);
        statsBox.setPadding(new Insets(20));
        statsBox.getStyleClass().add("header");

        progressLabel = new Label("Overall Progress: 0%");
        progressLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(12);

        statsBox.getChildren().addAll(progressLabel, progressBar);
        return statsBox;
    }

    /**
     * Create floating action button
     */
    private Button createFloatingActionButton(Stage primaryStage) {
        Button fab = new Button("+");
        fab.getStyleClass().add("fab");
        fab.setOnAction(e -> showAddTaskDialog(primaryStage));
        return fab;
    }

    /**
     * Get time-based greeting
     */
    private String getGreeting() {
        int hour = LocalTime.now().getHour();
        String timeGreeting;
        
        if (hour < 12) {
            timeGreeting = "Good Morning";
        } else if (hour < 18) {
            timeGreeting = "Good Afternoon";
        } else {
            timeGreeting = "Good Evening";
        }
        
        return timeGreeting + ", " + userPreferences.getUserName() + "!";
    }

    /**
     * Refresh task list display
     */
    public void refreshTaskList() {
        filterTasks();
        updateProgress();
    }

    /**
     * Filter and display tasks
     */
    private void filterTasks() {
        taskListContainer.getChildren().clear();
        
        List<Task> tasks = taskManager.getAllTasks();
        String searchText = searchField.getText().toLowerCase();
        String filter = filterCombo.getValue();
        boolean focusMode = focusModeCheckBox.isSelected();

        // Apply filters
        for (Task task : tasks) {
            // Focus mode: hide completed tasks
            if (focusMode && task.getStatus() == TaskStatus.COMPLETED) {
                continue;
            }

            // Search filter
            if (!searchText.isEmpty()) {
                boolean matches = task.getTitle().toLowerCase().contains(searchText) ||
                                (task.getDescription() != null && 
                                 task.getDescription().toLowerCase().contains(searchText));
                if (!matches) continue;
            }

            // Category filter
            boolean shouldShow = false;
            switch (filter) {
                case "All Tasks":
                    shouldShow = true;
                    break;
                case "Today":
                    shouldShow = task.isDueToday();
                    break;
                case "Overdue":
                    shouldShow = task.isOverdue();
                    break;
                case "High Priority":
                    shouldShow = task.getPriority() == model.Priority.HIGH;
                    break;
                case "Medium Priority":
                    shouldShow = task.getPriority() == model.Priority.MEDIUM;
                    break;
                case "Low Priority":
                    shouldShow = task.getPriority() == model.Priority.LOW;
                    break;
                case "Pending":
                    shouldShow = task.getStatus() == TaskStatus.PENDING;
                    break;
                case "In Progress":
                    shouldShow = task.getStatus() == TaskStatus.IN_PROGRESS;
                    break;
                case "Completed":
                    shouldShow = task.getStatus() == TaskStatus.COMPLETED;
                    break;
            }

            if (shouldShow) {
                taskListContainer.getChildren().add(new TaskCard(task, this));
            }
        }

        if (taskListContainer.getChildren().isEmpty()) {
            Label emptyLabel = new Label("No tasks found");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: gray;");
            taskListContainer.getChildren().add(emptyLabel);
        }
    }

    /**
     * Update progress display
     */
    private void updateProgress() {
        int percentage = taskManager.getCompletionPercentage();
        progressLabel.setText("Overall Progress: " + percentage + "%");
        progressBar.setProgress(percentage / 100.0);
    }

    /**
     * Show add task dialog
     */
    private void showAddTaskDialog(Stage owner) {
        TaskDialog dialog = new TaskDialog(owner, null, taskManager);
        dialog.showAndWait().ifPresent(task -> {
            taskManager.addTask(task);
            refreshTaskList();
        });
    }

    /**
     * Show settings dialog
     */
    private void showSettingsDialog() {
        Dialog<UserPreferences> dialog = new Dialog<>();
        dialog.setTitle("Settings");
        dialog.setHeaderText("Personalize Task Flow");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField(userPreferences.getUserName());
        nameField.setPromptText("Your Name");

        grid.add(new Label("Your Name:"), 0, 0);
        grid.add(nameField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                userPreferences.setUserName(nameField.getText());
                storageManager.saveUserPrefs(userPreferences);
                greetingLabel.setText(getGreeting());
                return userPreferences;
            }
            return null;
        });

        dialog.showAndWait();
    }

    /**
     * Toggle theme
     */
    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        userPreferences.setDarkMode(isDarkMode);
        storageManager.saveUserPrefs(userPreferences);
        applyTheme();
    }

    /**
     * Apply current theme
     */
    private void applyTheme() {
        scene.getStylesheets().clear();
        String theme = isDarkMode ? "dark-theme.css" : "light-theme.css";
        scene.getStylesheets().add(getClass().getResource("/styles/" + theme).toExternalForm());
    }

    /**
     * Get task manager
     */
    public TaskManager getTaskManager() {
        return taskManager;
    }

    /**
     * Get scene for theme updates
     */
    public Scene getScene() {
        return scene;
    }

    @Override
    public void stop() {
        taskManager.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

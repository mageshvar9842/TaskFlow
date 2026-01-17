package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.Task;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * StorageManager handles saving and loading tasks to/from JSON file
 */
public class StorageManager {
    private static final String DATA_FILE = "data/tasks.json";
    private static final String USER_PREFS_FILE = "data/user_prefs.json";
    private final Gson gson;

    public StorageManager() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
                .setPrettyPrinting()
                .create();

        // Ensure data directory exists
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
    }

    /**
     * Save tasks to JSON file
     */
    public void saveTasks(List<Task> tasks) {
        try (Writer writer = new FileWriter(DATA_FILE)) {
            gson.toJson(tasks, writer);
        } catch (IOException e) {
            System.err.println("Error saving tasks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load tasks from JSON file
     */
    public List<Task> loadTasks() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<Task>>() {}.getType();
            List<Task> tasks = gson.fromJson(reader, listType);
            return tasks != null ? tasks : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Error loading tasks: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Save user preferences
     */
    public void saveUserPrefs(UserPreferences prefs) {
        try (Writer writer = new FileWriter(USER_PREFS_FILE)) {
            gson.toJson(prefs, writer);
        } catch (IOException e) {
            System.err.println("Error saving user preferences: " + e.getMessage());
        }
    }

    /**
     * Load user preferences
     */
    public UserPreferences loadUserPrefs() {
        File file = new File(USER_PREFS_FILE);
        if (!file.exists()) {
            return new UserPreferences();
        }

        try (Reader reader = new FileReader(file)) {
            UserPreferences prefs = gson.fromJson(reader, UserPreferences.class);
            return prefs != null ? prefs : new UserPreferences();
        } catch (IOException e) {
            System.err.println("Error loading user preferences: " + e.getMessage());
            return new UserPreferences();
        }
    }
}

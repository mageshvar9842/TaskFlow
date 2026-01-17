package model;

/**
 * Enum representing task priority levels
 */
public enum Priority {
    HIGH("High", "#FF6B6B"),
    MEDIUM("Medium", "#FFD93D"),
    LOW("Low", "#6BCB77");

    private final String displayName;
    private final String color;

    Priority(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }
}

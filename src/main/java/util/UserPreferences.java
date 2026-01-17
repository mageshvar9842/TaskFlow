package util;

/**
 * UserPreferences stores user settings and personalization
 */
public class UserPreferences {
    private String userName;
    private boolean darkMode;
    private boolean focusMode;

    public UserPreferences() {
        this.userName = "User";
        this.darkMode = false;
        this.focusMode = false;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }

    public boolean isFocusMode() {
        return focusMode;
    }

    public void setFocusMode(boolean focusMode) {
        this.focusMode = focusMode;
    }
}

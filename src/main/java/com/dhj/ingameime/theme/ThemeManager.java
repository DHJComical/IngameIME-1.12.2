package com.dhj.ingameime.theme;

import com.dhj.ingameime.IngameIME_Forge;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Theme manager is responsible for loading, managing, and applying themes.
 */
public class ThemeManager {

    private static ThemeManager instance;
    private Theme currentTheme;
    private final Map<String, Theme> themes = new HashMap<>();
    private final File themesDir;
    private final File lastThemeFile;
    private final Gson gson;
    private final List<ThemeChangeListener> listeners = new ArrayList<>();
    
    // 默认主题
    private static final Theme DEFAULT_THEME_BACKUP = new Theme(
        "default",
        "Default Theme",
        0xFF000000,  // text color
        0xEBEBEBEB,  // background color
        0xFF555555,  // index color
        0xEBEBEBEB,  // selected item background
        0xFF000000,  // cursor color
        3,           // padding
        3,           // candidate box padding
        1,           // border width
        0x80000000   // border color
    );
    
    private ThemeManager() {
        this.themesDir = new File("config/ingameime/themes");
        this.lastThemeFile = new File(themesDir, "last_theme.txt");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        ensureThemesDirectory();
        loadDefaultThemes();
        loadCustomThemes();
        loadCurrentTheme();
    }
    
    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }
    
    private void ensureThemesDirectory() {
        if (!themesDir.exists()) {
            themesDir.mkdirs();
        }
    }

    /**
     * Load standard themes (Default, Dark, Light)
     * Logical modification: First check if there is JSON on the disk. If so, read it. Otherwise, use hard-coded JSON and generate a file.
     */
    private void loadDefaultThemes() {
        // --- Default Theme ---
        loadOrInitStandardTheme("default", DEFAULT_THEME_BACKUP);

        // --- Dark Theme ---
        Theme hardcodedDark = new Theme(
                "dark",
                "Dark Theme",
                0xFFFFFFFF,
                0x80333333,
                0xFFAAAAAA,
                0x80666666,
                0xFFFFFFFF,
                3,
                3,
                1,
                0x80FFFFFF
        );
        loadOrInitStandardTheme("dark", hardcodedDark);

        // --- Light Theme ---
        Theme hardcodedLight = new Theme(
                "light",
                "Light Theme",
                0xFF000000,
                0xF0FFFFFF,
                0xFF666666,
                0xF0DDDDDD,
                0xFF000000,
                3,
                3,
                1,
                0x80000000
        );
        loadOrInitStandardTheme("light", hardcodedLight);
    }

    private void loadOrInitStandardTheme(String id, Theme hardcodedTheme) {
        File file = new File(themesDir, id + ".json");
        boolean loadedFromDisk = false;

        // Try reading from disk
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Theme diskTheme = gson.fromJson(reader, Theme.class);
                if (diskTheme != null) {
                    themes.put(id, diskTheme);
                    loadedFromDisk = true;
                }
            } catch (Exception e) {
                IngameIME_Forge.logDebugInfo("[IME-ThemeSystem] Failed to read standard theme:" + id + ", will be reset to default value." + e.getMessage());
            }
        }

        // If the file is not present on disk, or the read operation fails, use the hard-coded default value and save to disk.
        if (!loadedFromDisk) {
            themes.put(id, hardcodedTheme);
            saveThemeToFile(hardcodedTheme);
        }
    }
    private void saveThemeToFile(Theme theme) {
        // Use the ThemeID as the filename
        File themeFile = new File(themesDir, theme.getId() + ".json");
        try (FileWriter writer = new FileWriter(themeFile)) {
            gson.toJson(theme, writer);
        } catch (IOException e) {
            IngameIME_Forge.logDebugInfo("[IME-ThemeSystem] Unable to save theme file:" + themeFile.getName());
        }
    }

    private void loadCurrentTheme() {
        String themeId = "default";

        String lastThemeId = loadLastThemeId();
        if (lastThemeId != null && !lastThemeId.isEmpty()) {
            themeId = lastThemeId;
        }
        if (themes.containsKey(themeId)) {
            currentTheme = themes.get(themeId);
        } else {
            // Default
            currentTheme = themes.get("default");
            if (currentTheme == null) {
                currentTheme = DEFAULT_THEME_BACKUP;
            }
        }
    }
    
    /**
     * Load the last used ThemeId
     */
    private String loadLastThemeId() {
        if (lastThemeFile.exists()) {
            try (FileReader reader = new FileReader(lastThemeFile)) {
                char[] buffer = new char[1024];
                int length = reader.read(buffer);
                if (length > 0) {
                    return new String(buffer, 0, length).trim();
                }
            } catch (IOException ignored) {}
        }
        return null;
    }
    
    /**
     * Save the current ThemeId as the last used theme.
     */
    private void saveLastThemeId(String themeId) {
        try (FileWriter writer = new FileWriter(lastThemeFile)) {
            writer.write(themeId);
        } catch (IOException e) {
            IngameIME_Forge.logDebugInfo("[IME-ThemeSystem] Unable to save the last used theme ID:" + e.getMessage());
        }
    }
    
    public Theme getCurrentTheme() {
        return currentTheme;
    }
    
    /**
     * Get theme based on ID
     */
    public Theme getTheme(String themeId) {
        return themes.get(themeId);
    }

    public void setTheme(String themeId) {
        if (themes.containsKey(themeId)) {
            currentTheme = themes.get(themeId);
            saveLastThemeId(themeId);
        } else {
            File customThemeFile = new File(themesDir, themeId + ".json");
            if (customThemeFile.exists()) {
                try (FileReader reader = new FileReader(customThemeFile)) {
                    Theme customTheme = gson.fromJson(reader, Theme.class);
                    if (customTheme != null) {
                        themes.put(themeId, customTheme);
                        currentTheme = customTheme;
                        saveLastThemeId(themeId);
                    }
                } catch (Exception e) {
                    IngameIME_Forge.logDebugInfo("[IME-ThemeSystem] Unable to load custom theme:" + themeId);
                }
            }
        }
    }
    
    public Map<String, Theme> getAvailableThemes() {
        return new HashMap<>(themes);
    }
    
    public void reloadThemes() {
        themes.clear();
        loadDefaultThemes();
        loadCustomThemes();
        loadCurrentTheme();
    }

    private void loadCustomThemes() {
        File[] themeFiles = themesDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (themeFiles != null) {
            for (File themeFile : themeFiles) {
                String themeId = themeFile.getName().replace(".json", "");
                if (!themes.containsKey(themeId)) {
                    try (FileReader reader = new FileReader(themeFile)) {
                        Theme theme = gson.fromJson(reader, Theme.class);
                        if (theme != null) {
                            themes.put(themeId, theme);
                        }
                    } catch (Exception e) {
                        IngameIME_Forge.logDebugInfo("[IME-ThemeSystem] Unable to load theme file:" + themeFile.getName());
                    }
                }
            }
        }
    }
    
    public void saveCustomTheme(Theme theme) {
        themes.put(theme.getId(), theme);
        saveThemeToFile(theme);
    }
    
    public void deleteCustomTheme(String themeId) {
        if (!themeId.equals("default") && !themeId.equals("dark") && !themeId.equals("light")) {
            themes.remove(themeId);
            File themeFile = new File(themesDir, themeId + ".json");
            if (themeFile.exists()) {
                themeFile.delete();
            }
        }
    }
    
    /**
     * Theme Change Listener Interface
     */
    public interface ThemeChangeListener {
        void onThemeChanged(Theme newTheme);
    }
    
    /**
     * Add theme change listener
     */
    public void addThemeChangeListener(ThemeChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove theme change listener
     */
    public void removeThemeChangeListener(ThemeChangeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notify all listeners that the theme has changed.
     */
    private void notifyThemeChanged() {
        for (ThemeChangeListener listener : listeners) {
            listener.onThemeChanged(currentTheme);
        }
    }
    
    /**
     * Set a theme and notify the listener.
     */
    public void setThemeAndNotify(String themeId) {
        setTheme(themeId);
        notifyThemeChanged();
    }
    
}
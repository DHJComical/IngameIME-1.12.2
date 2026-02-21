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
 * 主题管理器，负责加载、管理和应用主题
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
     * 加载标准主题 (Default, Dark, Light)
     * 逻辑修改：优先检查磁盘是否有 JSON，如果有则读取，没有才使用硬编码并生成文件。
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

        // 尝试从磁盘读取
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Theme diskTheme = gson.fromJson(reader, Theme.class);
                if (diskTheme != null) {
                    themes.put(id, diskTheme);
                    loadedFromDisk = true;
                }
            } catch (Exception e) {
                IngameIME_Forge.logDebugInfo("读取标准主题失败: " + id + ", 将重置为默认值。" + e.getMessage());
            }
        }

        // 如果磁盘没有文件，或者读取失败，则使用硬编码默认值，并保存到磁盘
        if (!loadedFromDisk) {
            themes.put(id, hardcodedTheme);
            saveThemeToFile(hardcodedTheme);
        }
    }
    private void saveThemeToFile(Theme theme) {
        // 使用主题ID作为文件名
        File themeFile = new File(themesDir, theme.getId() + ".json");
        try (FileWriter writer = new FileWriter(themeFile)) {
            gson.toJson(theme, writer);
        } catch (IOException e) {
            IngameIME_Forge.logDebugInfo("无法保存主题文件: " + themeFile.getName());
        }
    }

    private void loadCurrentTheme() {
        String themeId = "default";

        String lastThemeId = loadLastThemeId();
        if (lastThemeId != null && !lastThemeId.isEmpty()) {
            themeId = lastThemeId;
        }

        // 如果 themes map 里已经有了 (在 loadDefaultThemes 或 loadCustomThemes 里加载了)，直接用
        if (themes.containsKey(themeId)) {
            currentTheme = themes.get(themeId);
        } else {
            // 兜底逻辑
            currentTheme = themes.get("default");
            if (currentTheme == null) {
                currentTheme = DEFAULT_THEME_BACKUP;
            }
        }
    }
    
    /**
     * 加载上次使用的主题ID
     */
    private String loadLastThemeId() {
        if (lastThemeFile.exists()) {
            try (FileReader reader = new FileReader(lastThemeFile)) {
                char[] buffer = new char[1024];
                int length = reader.read(buffer);
                if (length > 0) {
                    return new String(buffer, 0, length).trim();
                }
            } catch (IOException e) {
                // 忽略错误，返回null
            }
        }
        return null;
    }
    
    /**
     * 保存当前主题ID为上次使用的主题
     */
    private void saveLastThemeId(String themeId) {
        try (FileWriter writer = new FileWriter(lastThemeFile)) {
            writer.write(themeId);
        } catch (IOException e) {
            IngameIME_Forge.logDebugInfo("无法保存上次使用的主题ID: " + e.getMessage());
        }
    }
    
    public Theme getCurrentTheme() {
        return currentTheme;
    }
    
    /**
     * 根据ID获取主题
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
                    IngameIME_Forge.logDebugInfo("无法加载自定义主题: " + themeId);
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
                        IngameIME_Forge.logDebugInfo("无法加载主题文件: " + themeFile.getName());
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
     * 主题变更监听器接口
     */
    public interface ThemeChangeListener {
        void onThemeChanged(Theme newTheme);
    }
    
    /**
     * 添加主题变更监听器
     */
    public void addThemeChangeListener(ThemeChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * 移除主题变更监听器
     */
    public void removeThemeChangeListener(ThemeChangeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * 通知所有监听器主题已变更
     */
    private void notifyThemeChanged() {
        for (ThemeChangeListener listener : listeners) {
            listener.onThemeChanged(currentTheme);
        }
    }
    
    /**
     * 设置主题并通知监听器
     */
    public void setThemeAndNotify(String themeId) {
        Theme oldTheme = currentTheme;
        setTheme(themeId);
        if (currentTheme != oldTheme) {
            notifyThemeChanged();
        }
    }
    
}
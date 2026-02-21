package com.dhj.ingameime.theme;

import com.dhj.ingameime.IngameIME_Forge;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

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
    private final Gson gson;
    private final List<ThemeChangeListener> listeners = new ArrayList<>();
    
    // 默认主题
    private static final Theme DEFAULT_THEME = new Theme(
        "default",
        "Default Theme",
        0xFF000000,  // text color
        0xEBEBEBEB,  // background color
        0xFF555555,  // index color
        0xEBEBEBEB,  // selected item background
        0xFF000000,  // cursor color
        3,           // padding
        5,           // candidate box padding
        1,           // border width
        0x80000000   // border color
    );
    
    private ThemeManager() {
        this.themesDir = new File("config/ingameime/themes");
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
    
    private void loadDefaultThemes() {
        // 添加默认主题
        themes.put("default", DEFAULT_THEME);
        
        // Add dark theme
        Theme darkTheme = new Theme(
            "dark",
            "Dark Theme",
            0xFFFFFFFF,  // text color
            0x80333333,  // background color
            0xFFAAAAAA,  // index color
            0x80666666,  // selected item background
            0xFFFFFFFF,  // cursor color
            3,           // padding
            5,           // candidate box padding
            1,           // border width
            0x80FFFFFF   // border color
        );
        themes.put("dark", darkTheme);
        
        // Add light theme
        Theme lightTheme = new Theme(
            "light",
            "Light Theme",
            0xFF000000,  // text color
            0xF0FFFFFF,  // background color
            0xFF666666,  // index color
            0xF0DDDDDD,  // selected item background
            0xFF000000,  // cursor color
            3,           // padding
            5,           // candidate box padding
            1,           // border width
            0x80000000   // border color
        );
        themes.put("light", lightTheme);
        
        // 保存默认主题到文件
        saveThemeToFile(DEFAULT_THEME);
        saveThemeToFile(darkTheme);
        saveThemeToFile(lightTheme);
    }
    
    private void saveThemeToFile(Theme theme) {
        File themeFile = new File(themesDir, theme.getId() + ".json");
        if (!themeFile.exists()) {
            try (FileWriter writer = new FileWriter(themeFile)) {
                gson.toJson(theme, writer);
            } catch (IOException e) {
                System.err.println("无法保存主题文件: " + themeFile.getName());
            }
        }
    }
    
    private void loadCurrentTheme() {
        // 默认使用default主题
        String themeId = "default";
        
        // 检查是否有上次使用的主题
        // 这里可以添加逻辑来记住上次使用的主题，比如从临时文件读取
        // 暂时先使用default主题
        
        // 尝试加载主题
        if (!themes.containsKey(themeId)) {
            File customThemeFile = new File(themesDir, themeId + ".json");
            if (customThemeFile.exists()) {
                try (FileReader reader = new FileReader(customThemeFile)) {
                    Theme customTheme = gson.fromJson(reader, Theme.class);
                    if (customTheme != null) {
                        themes.put(themeId, customTheme);
                    } else {
                        themeId = "default";
                    }
                } catch (IOException | JsonSyntaxException e) {
                    IngameIME_Forge.logDebugInfo("无法加载自定义主题: " + themeId);
                    themeId = "default";
                }
            } else {
                themeId = "default";
            }
        }
        
        currentTheme = themes.get(themeId);
        if (currentTheme == null) {
            currentTheme = DEFAULT_THEME;
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
        } else {
            // 尝试加载自定义主题
            File customThemeFile = new File(themesDir, themeId + ".json");
            if (customThemeFile.exists()) {
                try (FileReader reader = new FileReader(customThemeFile)) {
                    Theme customTheme = gson.fromJson(reader, Theme.class);
                    if (customTheme != null) {
                        themes.put(themeId, customTheme);
                        currentTheme = customTheme;
                    }
                } catch (IOException | JsonSyntaxException e) {
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
                    } catch (IOException | JsonSyntaxException e) {
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
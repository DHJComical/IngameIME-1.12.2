package com.dhj.ingameime.theme.api;

import com.dhj.ingameime.IngameIME_Forge;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class ThemeManager {
    private static ThemeManager instance;
    private Theme currentTheme;
    private final Map<String, Theme> themes = new HashMap<>();
    private final Map<String, ResourceLocation> textureCache = new HashMap<>();
    private final Map<String, Integer[]> textureSizeCache = new HashMap<>();
    private final File themesDir;
    private final File lastThemeFile;
    private final Gson gson;
    private final List<ThemeChangeListener> listeners = new ArrayList<>();

    private ThemeManager() {
        File mcDir = Minecraft.getMinecraft().gameDir;
        this.themesDir = new File(mcDir, "config/ingameime/themes");
        this.lastThemeFile = new File(themesDir, "last_theme.txt");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        ensureThemesDirectory();
        reloadThemes();
    }

    public static ThemeManager getInstance() {
        if (instance == null) instance = new ThemeManager();
        return instance;
    }

    private void ensureThemesDirectory() {
        if (!themesDir.exists()) themesDir.mkdirs();
    }

    public void reloadThemes() {
        IngameIME_Forge.logDebugInfo("[ThemeManager] Reloading themes...");
        themes.clear();
        clearCache();
        loadAllFromFolders();
        ensureStandardThemes();
        loadCurrentTheme();
        notifyThemeChanged();
        IngameIME_Forge.logDebugInfo("[ThemeManager] Reloaded {} themes, current: {}", themes.size(), 
                currentTheme != null ? currentTheme.getId() : "null");
    }

    private void clearCache() {
        textureCache.values().forEach(loc -> Minecraft.getMinecraft().getTextureManager().deleteTexture(loc));
        textureCache.clear();
        textureSizeCache.clear();
    }

    public void scanForNewThemes() {
        // 扫描新的文件夹格式主题
        File[] folders = themesDir.listFiles(File::isDirectory);
        if (folders != null) {
            for (File f : folders) {
                if (!f.getName().startsWith(".") && !themes.containsKey(f.getName())) {
                    loadThemeFromFolder(f);
                }
            }
        }
        
        // 扫描新的 JSON 文件格式主题
        File[] jsonFiles = themesDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (jsonFiles != null) {
            for (File f : jsonFiles) {
                String themeId = f.getName().replace(".json", "");
                if (!themes.containsKey(themeId)) {
                    loadThemeFromJsonFile(f);
                }
            }
        }
    }

    private void loadAllFromFolders() {
        // 加载文件夹格式的主题
        File[] folders = themesDir.listFiles(File::isDirectory);
        if (folders != null) {
            for (File f : folders) {
                if (!f.getName().startsWith(".")) { // 忽略隐藏文件夹
                    loadThemeFromFolder(f);
                }
            }
        }
        
        // 同时加载直接放置在 themes 目录下的 JSON 文件（兼容旧格式）
        File[] jsonFiles = themesDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (jsonFiles != null) {
            for (File f : jsonFiles) {
                loadThemeFromJsonFile(f);
            }
        }
    }

    private void loadThemeFromFolder(File folder) {
        if (!folder.isDirectory()) {
            IngameIME_Forge.logDebugInfo("[ThemeManager] Skipping '{}' - not a directory", folder.getName());
            return;
        }
        
        File configFile = new File(folder, "theme.json");
        if (!configFile.exists()) {
            IngameIME_Forge.logDebugInfo("[ThemeManager] Skipping folder '{}' - no theme.json found", folder.getName());
            return;
        }
        
        try (FileReader reader = new FileReader(configFile)) {
            Theme theme = gson.fromJson(reader, Theme.class);
            if (theme != null) {
                if (theme.getId() == null || theme.getId().isEmpty()) {
                    theme.setId(folder.getName());
                }
                if (theme.getName() == null || theme.getName().isEmpty()) {
                    theme.setName(folder.getName());
                }
                themes.put(theme.getId(), theme);
                IngameIME_Forge.logDebugInfo("[ThemeManager] Loaded theme: {} ({})", theme.getId(), theme.getName());
            } else {
                IngameIME_Forge.logDebugInfo("[ThemeManager] Failed to parse theme from '{}': gson returned null", folder.getName());
            }
        } catch (Exception e) {
            IngameIME_Forge.logDebugInfo("[ThemeManager] Error loading theme from folder '{}': {} - {}", 
                folder.getName(), e.getClass().getSimpleName(), e.getMessage());
        }
    }

    private void loadThemeFromJsonFile(File jsonFile) {
        try (FileReader reader = new FileReader(jsonFile)) {
            Theme theme = gson.fromJson(reader, Theme.class);
            if (theme != null) {
                // 使用文件名（不含.json）作为主题 ID
                String themeId = jsonFile.getName().replace(".json", "");
                if (theme.getId() == null || theme.getId().isEmpty()) {
                    theme.setId(themeId);
                }
                if (theme.getName() == null || theme.getName().isEmpty()) {
                    theme.setName(themeId);
                }
                themes.put(themeId, theme);
                IngameIME_Forge.logDebugInfo("[ThemeManager] Loaded theme from JSON: {} ({})", theme.getId(), theme.getName());
            } else {
                IngameIME_Forge.logDebugInfo("[ThemeManager] Failed to parse theme from JSON file '{}': gson returned null", jsonFile.getName());
            }
        } catch (Exception e) {
            IngameIME_Forge.logDebugInfo("[ThemeManager] Error loading theme from JSON file '{}': {} - {}", 
                jsonFile.getName(), e.getClass().getSimpleName(), e.getMessage());
        }
    }

    public ResourceLocation getThemeTexture(String key) {
        if (textureCache.containsKey(key)) return textureCache.get(key);

        String themeId = key.contains(":") ? key.split(":")[0] : key;
        String fileName = key.contains(":") ? key.split(":")[1] : themes.get(themeId).getTextureFile();

        if (fileName == null || fileName.isEmpty()) return null;
        File texFile = new File(new File(themesDir, themeId), fileName);
        if (texFile.exists()) return loadExternalTexture(key, texFile);
        return null;
    }

    public ResourceLocation loadExternalTexture(String cacheKey, File file) {
        try {
            BufferedImage img = ImageIO.read(file);
            if (img == null) return null;
            textureSizeCache.put(cacheKey, new Integer[]{img.getWidth(), img.getHeight()});
            DynamicTexture dyn = new DynamicTexture(img);
            ResourceLocation loc = Minecraft.getMinecraft().getTextureManager()
                    .getDynamicTextureLocation("ingameime_" + cacheKey.replace(":", "_"), dyn);
            textureCache.put(cacheKey, loc);
            return loc;
        } catch (Exception e) { return null; }
    }

    public Integer[] getTextureSize(String key) { return textureSizeCache.getOrDefault(key, new Integer[]{16, 16}); }

    public void saveCustomTheme(Theme theme) {
        File folder = new File(themesDir, theme.getId());
        if (!folder.exists()) folder.mkdirs();
        themes.put(theme.getId(), theme);
        try (FileWriter writer = new FileWriter(new File(folder, "theme.json"))) {
            gson.toJson(theme, writer);
            IngameIME_Forge.logDebugInfo("[ThemeManager] Saved theme: {} ({})", theme.getId(), theme.getName());
        } catch (IOException e) { 
            IngameIME_Forge.logDebugInfo("[ThemeManager] Failed to save theme '{}': {}", theme.getId(), e.getMessage());
        }
        textureCache.remove(theme.getId());
    }

    private void loadCurrentTheme() {
        String lastId = loadLastThemeId();
        currentTheme = themes.getOrDefault(lastId, themes.getOrDefault("default", null));
    }

    private String loadLastThemeId() {
        if (lastThemeFile.exists()) {
            try {
                List<String> lines = Files.readAllLines(lastThemeFile.toPath());
                if (!lines.isEmpty()) return lines.get(0).trim();
            } catch (IOException ignored) {}
        }
        return null;
    }

    private void ensureStandardThemes() {
        // 创建默认主题
        if (!themes.containsKey("default")) {
            Theme defaultTheme = Theme.createCustomTheme("default", "Default Theme");
            defaultTheme.setTextColor(0xFF000000);
            defaultTheme.setBackgroundColor(0xEBEBEBEB);
            defaultTheme.setIndexColor(0xFF555555);
            defaultTheme.setSelectedBackgroundColor(0xEBEBEBEB);
            defaultTheme.setCursorColor(0xFF000000);
            defaultTheme.setBorderColor(0x80000000);
            saveCustomTheme(defaultTheme);
        }
        
        // 创建深色主题
        if (!themes.containsKey("dark")) {
            Theme darkTheme = Theme.createCustomTheme("dark", "Dark Theme");
            darkTheme.setTextColor(0xFFFFFFFF);
            darkTheme.setBackgroundColor(0x80333333);
            darkTheme.setIndexColor(0xFFAAAAAA);
            darkTheme.setSelectedBackgroundColor(0x80555555);
            darkTheme.setCursorColor(0xFFFFFFFF);
            darkTheme.setBorderColor(0x80FFFFFF);
            saveCustomTheme(darkTheme);
        }
        
        // 创建浅色主题
        if (!themes.containsKey("light")) {
            Theme lightTheme = Theme.createCustomTheme("light", "Light Theme");
            lightTheme.setTextColor(0xFF000000);
            lightTheme.setBackgroundColor(0xF0FFFFFF);
            lightTheme.setIndexColor(0xFF555555);
            lightTheme.setSelectedBackgroundColor(0xE0EEEEEE);
            lightTheme.setCursorColor(0xFF000000);
            lightTheme.setBorderColor(0x80000000);
            saveCustomTheme(lightTheme);
        }
    }

    public void setThemeAndNotify(String id) {
        if (themes.containsKey(id)) {
            currentTheme = themes.get(id);
            try (FileWriter w = new FileWriter(lastThemeFile)) {
                w.write(id);
            } catch (IOException ignored) {}
            IngameIME_Forge.logDebugInfo("[ThemeManager] Theme switched to: {} ({})", id, currentTheme.getName());
            notifyThemeChanged();
        } else {
            IngameIME_Forge.logDebugInfo("[ThemeManager] Failed to switch theme: '{}' not found", id);
        }
    }

    public Theme getCurrentTheme() {
        return currentTheme;

    }
    public Theme getTheme(String id) {
        return themes.get(id);
    }

    public Map<String, Theme> getAvailableThemes() {
        return new HashMap<>(themes);
    }

    public void addThemeChangeListener(ThemeChangeListener l) {
        if (!listeners.contains(l)) listeners.add(l);
    }
    private void notifyThemeChanged() {
        for (ThemeChangeListener l : listeners)
            l.onThemeChanged(currentTheme);
    }
    public interface ThemeChangeListener {
        void onThemeChanged(Theme newTheme);
    }

    public void deleteCustomTheme(String id) {
        themes.remove(id);
        File f = new File(themesDir, id);
        if (f.exists()) {
            deleteRecursive(f);
            IngameIME_Forge.logDebugInfo("[ThemeManager] Deleted theme: {}", id);
        }
    }
    private void deleteRecursive(File f) {
        File[] c = f.listFiles();
        if (c != null) for (File s : c) deleteRecursive(s);
        f.delete();
    }
}
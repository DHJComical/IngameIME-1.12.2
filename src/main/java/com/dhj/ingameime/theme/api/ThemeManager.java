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
        File[] folders = themesDir.listFiles(File::isDirectory);
        if (folders != null) {
            for (File f : folders) {
                if (!themes.containsKey(f.getName())) loadThemeFromFolder(f);
            }
        }
    }

    private void loadAllFromFolders() {
        File[] folders = themesDir.listFiles(File::isDirectory);
        if (folders != null) for (File f : folders) loadThemeFromFolder(f);
    }

    private void loadThemeFromFolder(File folder) {
        File configFile = new File(folder, "theme.json");
        if (!configFile.exists()) return;
        try (FileReader reader = new FileReader(configFile)) {
            Theme theme = gson.fromJson(reader, Theme.class);
            if (theme != null) {
                theme.setId(folder.getName());
                themes.put(theme.getId(), theme);
                IngameIME_Forge.logDebugInfo("[ThemeManager] Loaded theme: {} ({})", theme.getId(), theme.getName());
            }
        } catch (Exception e) {
            IngameIME_Forge.logDebugInfo("[ThemeManager] Error loading theme from folder '{}': {}", folder.getName(), e.getMessage());
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
        if (!themes.containsKey("default")) saveCustomTheme(Theme.createCustomTheme("default", "Default Theme"));
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
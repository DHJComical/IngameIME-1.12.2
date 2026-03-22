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
    private File themesDir;
    private File lastThemeFile;
    private final Gson gson;
    private final List<ThemeChangeListener> listeners = new ArrayList<>();
    private boolean initialized = false;

    private ThemeManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    private void ensureInitialized() {
        if (initialized) return;
        
        try {
            File mcDir = Minecraft.getMinecraft().mcDataDir;
            this.themesDir = new File(mcDir, "config/ingameime/themes");
            this.lastThemeFile = new File(themesDir, "last_theme.txt");
            ensureThemesDirectory();
            reloadThemes();
            initialized = true;
            IngameIME_Forge.logDebugInfo("[ThemeManager] Initialized with themes directory: {}", themesDir.getAbsolutePath());
        } catch (Exception e) {
            IngameIME_Forge.logDebugInfo("[ThemeManager] Failed to initialize: {}", e.getMessage());
        }
    }

    public static ThemeManager getInstance() {
        if (instance == null) instance = new ThemeManager();
        instance.ensureInitialized();
        return instance;
    }

    private void ensureThemesDirectory() {
        if (themesDir == null) return;
        if (!themesDir.exists()) themesDir.mkdirs();
        IngameIME_Forge.logDebugInfo("[ThemeManager] Themes directory exists: {}", themesDir.exists());
        if (themesDir.exists()) {
            IngameIME_Forge.logDebugInfo("[ThemeManager] Themes directory contents: {}", Arrays.toString(themesDir.list()));
        }
    }

    public void reloadThemes() {
        if (themesDir == null) {
            IngameIME_Forge.logDebugInfo("[ThemeManager] Themes directory is null, skipping reload");
            return;
        }
        
        IngameIME_Forge.logDebugInfo("[ThemeManager] Reloading themes from: {}", themesDir.getAbsolutePath());
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
        if (themesDir == null) return;
        File[] folders = themesDir.listFiles(File::isDirectory);
        if (folders != null) {
            IngameIME_Forge.logDebugInfo("[ThemeManager] Scanning {} folders for themes", folders.length);
            for (File f : folders) {
                if (!themes.containsKey(f.getName())) {
                    IngameIME_Forge.logDebugInfo("[ThemeManager] Found new theme folder: {}", f.getName());
                    loadThemeFromFolder(f);
                }
            }
        }
    }

    private void loadAllFromFolders() {
        if (themesDir == null) return;
        File[] folders = themesDir.listFiles(File::isDirectory);
        if (folders != null) {
            IngameIME_Forge.logDebugInfo("[ThemeManager] Loading {} theme folders", folders.length);
            for (File f : folders) {
                IngameIME_Forge.logDebugInfo("[ThemeManager] Loading theme from: {}", f.getName());
                loadThemeFromFolder(f);
            }
        } else {
            IngameIME_Forge.logDebugInfo("[ThemeManager] No theme folders found");
        }
    }

    private void loadThemeFromFolder(File folder) {
        File configFile = new File(folder, "theme.json");
        if (!configFile.exists()) {
            IngameIME_Forge.logDebugInfo("[ThemeManager] No theme.json in {}", folder.getName());
            return;
        }
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
        if (themesDir == null) {
            IngameIME_Forge.logDebugInfo("[ThemeManager] Cannot save theme, themesDir is null");
            return;
        }
        File folder = new File(themesDir, theme.getId());
        if (!folder.exists()) folder.mkdirs();
        
        // Update in-memory theme map
        themes.put(theme.getId(), theme);
        
        // Save to disk
        File themeFile = new File(folder, "theme.json");
        IngameIME_Forge.logDebugInfo("[ThemeManager] Saving theme to: {}", themeFile.getAbsolutePath());
        try (FileWriter writer = new FileWriter(themeFile)) {
            gson.toJson(theme, writer);
            IngameIME_Forge.logDebugInfo("[ThemeManager] Saved theme: {} ({})", theme.getId(), theme.getName());
        } catch (IOException e) {
            IngameIME_Forge.logDebugInfo("[ThemeManager] Failed to save theme '{}': {}", theme.getId(), e.getMessage());
            e.printStackTrace();
        }
        textureCache.remove(theme.getId());
    }

    private void loadCurrentTheme() {
        String lastId = loadLastThemeId();
        IngameIME_Forge.logDebugInfo("[ThemeManager] Last theme ID: {}", lastId);
        currentTheme = themes.getOrDefault(lastId, themes.getOrDefault("default", null));
        if (currentTheme == null && !themes.isEmpty()) {
            currentTheme = themes.values().iterator().next();
        }
    }

    private String loadLastThemeId() {
        if (lastThemeFile == null || !lastThemeFile.exists()) {
            return null;
        }
        try {
            List<String> lines = Files.readAllLines(lastThemeFile.toPath());
            if (!lines.isEmpty()) return lines.get(0).trim();
        } catch (IOException ignored) {}
        return null;
    }

    private void ensureStandardThemes() {
        if (!themes.containsKey("default")) {
            Theme defaultTheme = Theme.createCustomTheme("default", "Default Theme");
            saveCustomTheme(defaultTheme);
            IngameIME_Forge.logDebugInfo("[ThemeManager] Created default theme");
        }
    }

    public void setThemeAndNotify(String id) {
        if (themes.containsKey(id)) {
            currentTheme = themes.get(id);
            if (lastThemeFile != null) {
                try (FileWriter w = new FileWriter(lastThemeFile)) {
                    w.write(id);
                } catch (IOException ignored) {}
            }
            IngameIME_Forge.logDebugInfo("[ThemeManager] Theme switched to: {} ({})", id, currentTheme.getName());
            notifyThemeChanged();
        } else {
            IngameIME_Forge.logDebugInfo("[ThemeManager] Failed to switch theme: '{}' not found. Available: {}", id, themes.keySet());
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
        if (themesDir == null) return;
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

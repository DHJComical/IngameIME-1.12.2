package com.dhj.ingameime.theme.api;

import com.dhj.ingameime.IngameIME_Forge;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThemeManager {
    private static final ResourceLocation RESOURCE_THEME_INDEX = new ResourceLocation("ingameime", "themes/index.json");
    private static final String USER_THEME_PACK_NAME = "IngameIME_UserThemes";
    private static final String THIRD_PARTY_THEME_PACK_NAME = "IngameIME_ThirdPartyThemes";

    private static ThemeManager instance;

    private Theme currentTheme;
    private final Map<String, Theme> themes = new HashMap<>();
    private final Map<String, ResourceLocation> textureCache = new HashMap<>();
    private final Map<String, Integer[]> textureSizeCache = new HashMap<>();
    private final Map<String, ResourceThemeSource> resourceThemeSources = new HashMap<>();
    private final Map<String, File> userThemeBaseDirs = new HashMap<>();

    private final File stateDir;
    private final File lastThemeFile;
    private final File userThemePackDir;
    private final File userThemePackThemesDir;
    private final File userThemePackIndexFile;
    private final File thirdPartyThemePackDir;
    private final File thirdPartyThemePackThemesDir;

    private final Gson gson;
    private final List<ThemeChangeListener> listeners = new ArrayList<>();

    private static class ResourceThemeIndex {
        List<String> themes = new ArrayList<>();
    }

    private static class ResourceThemeSource {
        final String namespace;
        final String basePath;

        ResourceThemeSource(String namespace, String basePath) {
            this.namespace = namespace;
            this.basePath = basePath;
        }
    }

    private ThemeManager() {
        File mcDir = Minecraft.getMinecraft().gameDir;
        this.stateDir = new File(mcDir, "config/ingameime");
        this.lastThemeFile = new File(stateDir, "last_theme.txt");
        this.userThemePackDir = new File(mcDir, "resourcepacks/" + USER_THEME_PACK_NAME);
        this.userThemePackThemesDir = new File(userThemePackDir, "assets/ingameime/themes");
        this.userThemePackIndexFile = new File(userThemePackThemesDir, "index.json");
        this.thirdPartyThemePackDir = new File(mcDir, "resourcepacks/" + THIRD_PARTY_THEME_PACK_NAME);
        this.thirdPartyThemePackThemesDir = new File(thirdPartyThemePackDir, "assets/ingameime/themes");
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        ensureStateDirectory();
        ensureUserThemePackDirectory();
        ensureThirdPartyThemePackDirectory();
        reloadThemes();
    }

    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    private void ensureStateDirectory() {
        if (!stateDir.exists()) {
            stateDir.mkdirs();
        }
    }

    private void ensureUserThemePackDirectory() {
        if (!userThemePackThemesDir.exists()) {
            userThemePackThemesDir.mkdirs();
        }

        File packMcmeta = new File(userThemePackDir, "pack.mcmeta");
        if (!packMcmeta.exists()) {
            writeThemePackMcmeta(packMcmeta, "IngameIME user themes");
        }

        if (!userThemePackIndexFile.exists()) {
            writeThemeIndex(new ResourceThemeIndex());
        }
    }

    private void ensureThirdPartyThemePackDirectory() {
        if (!thirdPartyThemePackThemesDir.exists()) {
            thirdPartyThemePackThemesDir.mkdirs();
        }

        File packMcmeta = new File(thirdPartyThemePackDir, "pack.mcmeta");
        if (!packMcmeta.exists()) {
            writeThemePackMcmeta(packMcmeta, "IngameIME third-party themes");
        }
    }

    private void writeThemePackMcmeta(File packMcmeta, String description) {
        String content = "{\n"
            + "  \"pack\": {\n"
            + "    \"pack_format\": 3,\n"
            + "    \"description\": \"" + description + "\"\n"
            + "  }\n"
            + "}\n";
        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(packMcmeta.toPath()), StandardCharsets.UTF_8)) {
            writer.write(content);
        } catch (IOException e) {
            IngameIME_Forge.logDebugInfo("[ThemeManager] Failed to create pack.mcmeta: {}", e.getMessage());
        }
    }

    private ResourceThemeIndex readThemeIndex() {
        ensureUserThemePackDirectory();
        if (!userThemePackIndexFile.exists()) {
            return new ResourceThemeIndex();
        }

        try (Reader reader = new InputStreamReader(Files.newInputStream(userThemePackIndexFile.toPath()), StandardCharsets.UTF_8)) {
            ResourceThemeIndex index = gson.fromJson(reader, ResourceThemeIndex.class);
            if (index == null || index.themes == null) {
                return new ResourceThemeIndex();
            }
            return index;
        } catch (Exception e) {
            IngameIME_Forge.logDebugInfo("[ThemeManager] Failed to read user theme index: {}", e.getMessage());
            return new ResourceThemeIndex();
        }
    }

    private void writeThemeIndex(ResourceThemeIndex index) {
        if (index.themes == null) {
            index.themes = new ArrayList<>();
        }
        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(userThemePackIndexFile.toPath()), StandardCharsets.UTF_8)) {
            gson.toJson(index, writer);
        } catch (Exception e) {
            IngameIME_Forge.logDebugInfo("[ThemeManager] Failed to write user theme index: {}", e.getMessage());
        }
    }

    private void updateThemeIndexEntry(String themeId, boolean add) {
        ensureUserThemePackDirectory();
        String entry = themeId + "/theme.json";
        ResourceThemeIndex index = readThemeIndex();
        if (index.themes == null) {
            index.themes = new ArrayList<>();
        }
        index.themes.remove(entry);
        if (add) {
            index.themes.add(entry);
        }
        writeThemeIndex(index);
    }

    public void reloadThemes() {
        IngameIME_Forge.logDebugInfo("[ThemeManager] Reloading themes...");
        themes.clear();
        clearCache();
        loadAllFromResourcePacks();
        loadAllFromThirdPartyThemePackFiles();
        loadAllFromUserThemePackFiles();
        ensureStandardThemes();
        loadCurrentTheme();
        notifyThemeChanged();
        IngameIME_Forge.logDebugInfo(
            "[ThemeManager] Reloaded {} themes, current: {}",
            themes.size(),
            currentTheme != null ? currentTheme.getId() : "null");
    }

    private void clearCache() {
        textureCache.values().forEach(loc -> Minecraft.getMinecraft().getTextureManager().deleteTexture(loc));
        textureCache.clear();
        textureSizeCache.clear();
        resourceThemeSources.clear();
        userThemeBaseDirs.clear();
    }

    public void scanForNewThemes() {
        reloadThemes();
    }

    private void loadAllFromUserThemePackFiles() {
        ResourceThemeIndex index = readThemeIndex();
        if (index.themes == null || index.themes.isEmpty()) {
            return;
        }

        for (String entry : index.themes) {
            File themeFile = resolveUserThemeFile(entry);
            if (themeFile != null && themeFile.isFile()) {
                loadThemeFromLocalPackFile(themeFile, userThemePackThemesDir, "user-pack");
            }
        }
    }

    private void loadAllFromThirdPartyThemePackFiles() {
        ensureThirdPartyThemePackDirectory();
        for (File themeFile : collectThemeJsonFiles(thirdPartyThemePackThemesDir)) {
            loadThemeFromLocalPackFile(themeFile, thirdPartyThemePackThemesDir, "third-party-pack");
        }
    }

    private List<File> collectThemeJsonFiles(File rootDir) {
        List<File> result = new ArrayList<>();
        if (rootDir == null || !rootDir.exists()) {
            return result;
        }

        try (java.util.stream.Stream<Path> stream = Files.walk(rootDir.toPath())) {
            stream.filter(Files::isRegularFile)
                .map(Path::toFile)
                .filter(file -> "theme.json".equalsIgnoreCase(file.getName()))
                .forEach(result::add);
        } catch (IOException e) {
            IngameIME_Forge.logDebugInfo(
                "[ThemeManager] Error scanning theme files in '{}': {} - {}",
                rootDir.getAbsolutePath(),
                e.getClass().getSimpleName(),
                e.getMessage());
        }

        return result;
    }

    private File resolveUserThemeFile(String entry) {
        if (entry == null) {
            return null;
        }

        String path = entry.trim().replace('\\', '/');
        if (path.isEmpty()) {
            return null;
        }
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        int colon = path.indexOf(':');
        if (colon >= 0 && colon + 1 < path.length()) {
            path = path.substring(colon + 1);
        }
        if (path.startsWith("themes/")) {
            path = path.substring("themes/".length());
        }
        if (!path.endsWith(".json")) {
            path = path + ".json";
        }
        return new File(userThemePackThemesDir, path);
    }

    private void loadThemeFromLocalPackFile(File themeFile, File themeRoot, String sourceTag) {
        try (Reader reader = new InputStreamReader(Files.newInputStream(themeFile.toPath()), StandardCharsets.UTF_8)) {
            Theme theme = gson.fromJson(reader, Theme.class);
            if (theme == null) {
                return;
            }

            String fileName = themeFile.getName();
            String idFromFile = fileName.endsWith(".json")
                ? fileName.substring(0, fileName.length() - 5)
                : fileName;

            if (theme.getId() == null || theme.getId().isEmpty()) {
                theme.setId(idFromFile);
            }
            if (theme.getName() == null || theme.getName().isEmpty()) {
                theme.setName(theme.getId());
            }

            String relativeParent = "";
            File parent = themeFile.getParentFile();
            if (parent != null) {
                String base = themeRoot.getAbsolutePath();
                String full = parent.getAbsolutePath();
                if (full.startsWith(base)) {
                    relativeParent = full.substring(base.length()).replace('\\', '/');
                    while (relativeParent.startsWith("/")) {
                        relativeParent = relativeParent.substring(1);
                    }
                }
            }

            String basePath = relativeParent.isEmpty() ? "themes" : "themes/" + relativeParent;
            themes.put(theme.getId(), theme);
            resourceThemeSources.put(theme.getId(), new ResourceThemeSource("ingameime", basePath));
            userThemeBaseDirs.put(theme.getId(), parent != null ? parent : themeRoot);

            IngameIME_Forge.logDebugInfo(
                "[ThemeManager] Loaded {} theme: {} ({}) from {}",
                sourceTag,
                theme.getId(),
                theme.getName(),
                themeFile.getAbsolutePath());
        } catch (Exception e) {
            IngameIME_Forge.logDebugInfo(
                "[ThemeManager] Error loading {} theme file '{}': {} - {}",
                sourceTag,
                themeFile.getAbsolutePath(),
                e.getClass().getSimpleName(),
                e.getMessage());
        }
    }

    private void loadAllFromResourcePacks() {
        try {
            List<IResource> indexResources = Minecraft.getMinecraft().getResourceManager().getAllResources(RESOURCE_THEME_INDEX);
            for (IResource indexResource : indexResources) {
                loadThemesFromResourceIndex(indexResource);
            }
        } catch (Exception e) {
            IngameIME_Forge.logDebugInfo(
                "[ThemeManager] No resource-pack theme index found at {}: {}",
                RESOURCE_THEME_INDEX,
                e.getClass().getSimpleName());
        }
    }

    private void loadThemesFromResourceIndex(IResource indexResource) {
        try (Reader reader = new InputStreamReader(indexResource.getInputStream(), StandardCharsets.UTF_8)) {
            ResourceThemeIndex index = gson.fromJson(reader, ResourceThemeIndex.class);
            if (index == null || index.themes == null || index.themes.isEmpty()) {
                return;
            }

            for (String themeEntry : index.themes) {
                ResourceLocation themeLocation = parseThemeResourceLocation(
                    indexResource.getResourceLocation().getNamespace(),
                    themeEntry);
                if (themeLocation != null) {
                    loadThemeFromResource(themeLocation);
                }
            }
        } catch (Exception e) {
            IngameIME_Forge.logDebugInfo(
                "[ThemeManager] Failed to parse resource-pack theme index '{}': {} - {}",
                indexResource.getResourceLocation(),
                e.getClass().getSimpleName(),
                e.getMessage());
        }
    }

    private ResourceLocation parseThemeResourceLocation(String defaultNamespace, String themeEntry) {
        if (themeEntry == null) {
            return null;
        }

        String entry = themeEntry.trim().replace('\\', '/');
        if (entry.isEmpty()) {
            return null;
        }
        while (entry.startsWith("/")) {
            entry = entry.substring(1);
        }

        try {
            if (entry.indexOf(':') >= 0) {
                ResourceLocation location = new ResourceLocation(entry);
                String path = location.getPath();
                if (!path.endsWith(".json")) {
                    path = path + ".json";
                }
                if (!path.startsWith("themes/")) {
                    path = "themes/" + path;
                }
                return new ResourceLocation(location.getNamespace(), path);
            }

            if (!entry.endsWith(".json")) {
                entry = entry + ".json";
            }
            if (!entry.startsWith("themes/")) {
                entry = "themes/" + entry;
            }
            return new ResourceLocation(defaultNamespace, entry);
        } catch (Exception e) {
            IngameIME_Forge.logDebugInfo("[ThemeManager] Invalid resource-pack theme entry '{}'", themeEntry);
            return null;
        }
    }

    private void loadThemeFromResource(ResourceLocation location) {
        try (IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(location);
             Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            Theme theme = gson.fromJson(reader, Theme.class);
            if (theme == null) {
                return;
            }

            String path = location.getPath();
            String pathId = path;
            int slash = path.lastIndexOf('/');
            if (slash >= 0) {
                pathId = path.substring(slash + 1);
            }
            if (pathId.endsWith(".json")) {
                pathId = pathId.substring(0, pathId.length() - 5);
            }

            if (theme.getId() == null || theme.getId().isEmpty()) {
                theme.setId(pathId);
            }
            if (theme.getName() == null || theme.getName().isEmpty()) {
                theme.setName(theme.getId());
            }

            String basePath = slash >= 0 ? path.substring(0, slash) : "";
            themes.put(theme.getId(), theme);
            resourceThemeSources.put(
                theme.getId(),
                new ResourceThemeSource(location.getNamespace(), basePath));
            userThemeBaseDirs.remove(theme.getId());
            IngameIME_Forge.logDebugInfo(
                "[ThemeManager] Loaded resource-pack theme: {} ({}) from {}",
                theme.getId(),
                theme.getName(),
                location);
        } catch (Exception e) {
            IngameIME_Forge.logDebugInfo(
                "[ThemeManager] Error loading resource-pack theme '{}': {} - {}",
                location,
                e.getClass().getSimpleName(),
                e.getMessage());
        }
    }

    public ResourceLocation getThemeTexture(String key) {
        if (textureCache.containsKey(key)) {
            return textureCache.get(key);
        }

        String[] parts = key.split(":", 2);
        String themeId = parts[0];
        Theme theme = themes.get(themeId);
        if (theme == null) {
            return null;
        }
        String fileName = parts.length > 1 ? parts[1] : theme.getTextureFile();
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }

        File userThemeTexture = resolveUserThemeTexture(themeId, fileName);
        if (userThemeTexture != null && userThemeTexture.isFile()) {
            return loadExternalTexture(key, userThemeTexture);
        }

        ResourceLocation textureResource = resolveResourceTexture(themeId, fileName);
        if (textureResource == null) {
            return null;
        }
        return loadResourceTexture(key, textureResource);
    }

    private File resolveUserThemeTexture(String themeId, String fileName) {
        File baseDir = userThemeBaseDirs.get(themeId);
        if (baseDir == null) {
            return null;
        }

        if (fileName.indexOf(':') >= 0) {
            return null;
        }

        String normalized = fileName.replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return new File(baseDir, normalized);
    }

    private ResourceLocation resolveResourceTexture(String themeId, String fileName) {
        try {
            if (fileName.indexOf(':') >= 0) {
                return new ResourceLocation(fileName);
            }

            ResourceThemeSource source = resourceThemeSources.get(themeId);
            if (source == null) {
                return null;
            }

            String normalized = fileName.replace('\\', '/');
            while (normalized.startsWith("/")) {
                normalized = normalized.substring(1);
            }
            String fullPath = source.basePath.isEmpty() ? normalized : source.basePath + "/" + normalized;
            return new ResourceLocation(source.namespace, fullPath);
        } catch (Exception e) {
            IngameIME_Forge.logDebugInfo("[ThemeManager] Invalid texture resource path '{}:{}'", themeId, fileName);
            return null;
        }
    }

    private ResourceLocation loadResourceTexture(String cacheKey, ResourceLocation resourceLocation) {
        try (InputStream in = Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation).getInputStream()) {
            BufferedImage img = ImageIO.read(in);
            if (img == null) {
                return null;
            }
            textureSizeCache.put(cacheKey, new Integer[]{img.getWidth(), img.getHeight()});
            DynamicTexture dyn = new DynamicTexture(img);
            ResourceLocation loc = Minecraft.getMinecraft().getTextureManager()
                .getDynamicTextureLocation("ingameime_" + cacheKey.replace(":", "_"), dyn);
            textureCache.put(cacheKey, loc);
            return loc;
        } catch (Exception e) {
            IngameIME_Forge.logDebugInfo(
                "[ThemeManager] Failed to load resource-pack texture '{}': {} - {}",
                resourceLocation,
                e.getClass().getSimpleName(),
                e.getMessage());
            return null;
        }
    }

    public ResourceLocation loadExternalTexture(String cacheKey, File file) {
        try {
            BufferedImage img = ImageIO.read(file);
            if (img == null) {
                return null;
            }
            textureSizeCache.put(cacheKey, new Integer[]{img.getWidth(), img.getHeight()});
            DynamicTexture dyn = new DynamicTexture(img);
            ResourceLocation loc = Minecraft.getMinecraft().getTextureManager()
                .getDynamicTextureLocation("ingameime_" + cacheKey.replace(":", "_"), dyn);
            textureCache.put(cacheKey, loc);
            return loc;
        } catch (Exception e) {
            return null;
        }
    }

    public Integer[] getTextureSize(String key) {
        return textureSizeCache.getOrDefault(key, new Integer[]{16, 16});
    }

    private void clearThemeTextureCache(String themeId) {
        List<String> keysToRemove = new ArrayList<>();
        for (String key : textureCache.keySet()) {
            if (key.equals(themeId) || key.startsWith(themeId + ":")) {
                keysToRemove.add(key);
            }
        }
        for (String key : keysToRemove) {
            ResourceLocation loc = textureCache.remove(key);
            if (loc != null) {
                Minecraft.getMinecraft().getTextureManager().deleteTexture(loc);
            }
            textureSizeCache.remove(key);
        }
    }

    public void saveCustomThemeToResourcePack(Theme theme) {
        ensureUserThemePackDirectory();

        File folder = new File(userThemePackThemesDir, theme.getId());
        if (!folder.exists()) {
            folder.mkdirs();
        }

        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(new File(folder, "theme.json").toPath()), StandardCharsets.UTF_8)) {
            gson.toJson(theme, writer);
            IngameIME_Forge.logDebugInfo(
                "[ThemeManager] Saved theme to resource pack: {} ({})",
                theme.getId(),
                theme.getName());
        } catch (IOException e) {
            IngameIME_Forge.logDebugInfo("[ThemeManager] Failed to save resource-pack theme '{}': {}", theme.getId(), e.getMessage());
        }

        updateThemeIndexEntry(theme.getId(), true);
        themes.put(theme.getId(), theme);
        resourceThemeSources.put(theme.getId(), new ResourceThemeSource("ingameime", "themes/" + theme.getId()));
        userThemeBaseDirs.put(theme.getId(), folder);
        clearThemeTextureCache(theme.getId());

        IngameIME_Forge.logDebugInfo(
            "[ThemeManager] User theme pack path: {} (enable this resource pack in game if needed)",
            userThemePackDir.getAbsolutePath());
    }

    public void saveCustomTheme(Theme theme) {
        saveCustomThemeToResourcePack(theme);
    }

    private void loadCurrentTheme() {
        String lastId = loadLastThemeId();
        if (lastId != null && themes.containsKey(lastId)) {
            currentTheme = themes.get(lastId);
            return;
        }

        if (themes.containsKey("default")) {
            currentTheme = themes.get("default");
            return;
        }

        currentTheme = themes.isEmpty() ? null : themes.values().iterator().next();
    }

    private String loadLastThemeId() {
        if (lastThemeFile.exists()) {
            try {
                List<String> lines = Files.readAllLines(lastThemeFile.toPath());
                if (!lines.isEmpty()) {
                    return lines.get(0).trim();
                }
            } catch (IOException ignored) {
            }
        }
        return null;
    }

    private void ensureStandardThemes() {
        if (!themes.containsKey("default")) {
            Theme defaultTheme = Theme.createCustomTheme("default", "Default Theme");
            defaultTheme.setTextColor(0xFF000000);
            defaultTheme.setBackgroundColor(0xEBEBEBEB);
            defaultTheme.setIndexColor(0xFF555555);
            defaultTheme.setSelectedBackgroundColor(0xEBEBEBEB);
            defaultTheme.setCursorColor(0xFF000000);
            defaultTheme.setBorderColor(0x80000000);
            saveCustomThemeToResourcePack(defaultTheme);
        }

        if (!themes.containsKey("dark")) {
            Theme darkTheme = Theme.createCustomTheme("dark", "Dark Theme");
            darkTheme.setTextColor(0xFFFFFFFF);
            darkTheme.setBackgroundColor(0x80333333);
            darkTheme.setIndexColor(0xFFAAAAAA);
            darkTheme.setSelectedBackgroundColor(0x80555555);
            darkTheme.setCursorColor(0xFFFFFFFF);
            darkTheme.setBorderColor(0x80FFFFFF);
            saveCustomThemeToResourcePack(darkTheme);
        }

        if (!themes.containsKey("light")) {
            Theme lightTheme = Theme.createCustomTheme("light", "Light Theme");
            lightTheme.setTextColor(0xFF000000);
            lightTheme.setBackgroundColor(0xF0FFFFFF);
            lightTheme.setIndexColor(0xFF555555);
            lightTheme.setSelectedBackgroundColor(0xE0EEEEEE);
            lightTheme.setCursorColor(0xFF000000);
            lightTheme.setBorderColor(0x80000000);
            saveCustomThemeToResourcePack(lightTheme);
        }
    }

    public void setThemeAndNotify(String id) {
        if (themes.containsKey(id)) {
            currentTheme = themes.get(id);
            try (Writer writer = new OutputStreamWriter(Files.newOutputStream(lastThemeFile.toPath()), StandardCharsets.UTF_8)) {
                writer.write(id);
            } catch (IOException ignored) {
            }
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

    public void addThemeChangeListener(ThemeChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    private void notifyThemeChanged() {
        for (ThemeChangeListener listener : listeners) {
            listener.onThemeChanged(currentTheme);
        }
    }

    public interface ThemeChangeListener {
        void onThemeChanged(Theme newTheme);
    }

    public void deleteCustomTheme(String id) {
        themes.remove(id);
        resourceThemeSources.remove(id);
        userThemeBaseDirs.remove(id);
        clearThemeTextureCache(id);

        File resourceFolder = new File(userThemePackThemesDir, id);
        if (resourceFolder.exists()) {
            deleteRecursive(resourceFolder);
            IngameIME_Forge.logDebugInfo("[ThemeManager] Deleted resource-pack theme: {}", id);
        }

        updateThemeIndexEntry(id, false);
    }

    private void deleteRecursive(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursive(child);
            }
        }
        file.delete();
    }
}

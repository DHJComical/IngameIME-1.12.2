package com.dhj.ingameime.theme.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Validates theme identifiers and confines user-theme paths to a configured root. */
public final class ThemePathPolicy {
    private static final Pattern THEME_ID_PATTERN = Pattern.compile("[a-z0-9][a-z0-9_-]{0,63}");
    private static final Pattern INDEX_ENTRY_PATTERN =
        Pattern.compile("([a-z0-9][a-z0-9_-]{0,63})/theme\\.json");
    private static final Pattern SIMPLE_FILE_NAME_PATTERN =
        Pattern.compile("[a-zA-Z0-9][a-zA-Z0-9_.-]{0,127}");
    private static final String THEMES_PREFIX = "themes/";

    private final Path root;

    /**
     * Creates a policy rooted at the directory containing user themes.
     *
     * @param root directory under which all resolved theme paths must remain
     */
    public ThemePathPolicy(Path root) {
        if (root == null) {
            throw new IllegalArgumentException("Theme root must not be null");
        }
        this.root = root.toAbsolutePath().normalize();
    }

    /**
     * Validates a stable theme identifier.
     *
     * @param themeId identifier to validate
     * @return the unchanged validated identifier
     */
    public String requireValidThemeId(String themeId) {
        if (themeId == null || !THEME_ID_PATTERN.matcher(themeId).matches()) {
            throw new IllegalArgumentException(
                "Theme ID must contain 1-64 lowercase letters, digits, underscores, or hyphens");
        }
        return themeId;
    }

    /**
     * Validates a file name (such as a texture file) that must stay a plain file directly inside
     * its theme directory: no path separators, no parent references, no leading dots.
     *
     * @param fileName file name to validate
     * @return the unchanged validated file name
     */
    public static String requireSafeFileName(String fileName) {
        if (fileName == null || !SIMPLE_FILE_NAME_PATTERN.matcher(fileName).matches()) {
            throw new IllegalArgumentException(
                "File name must be 1-128 letters, digits, dots, underscores, or hyphens without path separators");
        }
        return fileName;
    }

    /** Resolves the directory assigned to a theme identifier. */
    public Path resolveThemeDirectory(String themeId) throws IOException {
        String validThemeId = requireValidThemeId(themeId);
        return requireWithinRoot(root.resolve(validThemeId));
    }

    /** Resolves the theme.json file assigned to a theme identifier. */
    public Path resolveThemeFile(String themeId) throws IOException {
        Path themeDirectory = resolveThemeDirectory(themeId);
        return requireWithinRoot(themeDirectory.resolve("theme.json"));
    }

    /**
     * Resolves a theme index entry to the confined theme.json file.
     *
     * <p>Accepts the plain {@code <theme-id>/theme.json} form as well as legacy entries carrying
     * a {@code namespace:} prefix and/or a leading {@code themes/} segment. Anything else —
     * including absolute paths, backslashes, parent references, other file names, or nested
     * directories — is rejected.
     *
     * @param entry index entry to resolve
     * @return the confined theme.json path
     */
    public Path resolveIndexEntry(String entry) throws IOException {
        if (entry == null) {
            throw new IllegalArgumentException("Theme index entry must not be null");
        }

        String normalized = entry.trim();
        int colon = normalized.indexOf(':');
        if (colon >= 0) {
            normalized = normalized.substring(colon + 1);
        }
        if (normalized.startsWith(THEMES_PREFIX)) {
            normalized = normalized.substring(THEMES_PREFIX.length());
        }

        Matcher matcher = INDEX_ENTRY_PATTERN.matcher(normalized);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                "Theme index entry must use the format <theme-id>/theme.json: " + entry);
        }
        return resolveThemeFile(matcher.group(1));
    }

    private Path requireWithinRoot(Path candidate) throws IOException {
        Path normalizedCandidate = candidate.toAbsolutePath().normalize();
        if (!normalizedCandidate.startsWith(root)) {
            throw new IOException("Theme path escapes user theme root: " + candidate);
        }

        if (Files.exists(root, LinkOption.NOFOLLOW_LINKS)) {
            Path realRoot = root.toRealPath();
            Path existingPath = closestExistingPath(normalizedCandidate);
            if (!existingPath.toRealPath().startsWith(realRoot)) {
                throw new IOException("Theme path escapes user theme root: " + candidate);
            }
        }
        return normalizedCandidate;
    }

    private Path closestExistingPath(Path path) {
        Path existingPath = path;
        while (!Files.exists(existingPath, LinkOption.NOFOLLOW_LINKS)) {
            existingPath = existingPath.getParent();
        }
        return existingPath;
    }
}

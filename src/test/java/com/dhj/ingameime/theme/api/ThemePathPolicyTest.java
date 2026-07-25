package com.dhj.ingameime.theme.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ThemePathPolicyTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void acceptsEditorCompatibleThemeIds() {
        ThemePathPolicy policy = new ThemePathPolicy(temporaryDirectory);
        for (String id : Arrays.asList(
                "default", "dark", "light", "custom_id", "theme_123", "with-hyphen", "9lives")) {
            assertEquals(id, policy.requireValidThemeId(id));
        }
    }

    @Test
    void acceptsMaxLengthAndRejectsOverlongThemeIds() {
        ThemePathPolicy policy = new ThemePathPolicy(temporaryDirectory);
        StringBuilder maxLengthId = new StringBuilder();
        for (int i = 0; i < 64; i++) {
            maxLengthId.append('a');
        }

        assertEquals(maxLengthId.toString(), policy.requireValidThemeId(maxLengthId.toString()));
        assertThrows(
                IllegalArgumentException.class,
                () -> policy.requireValidThemeId(maxLengthId.toString() + "a"));
    }

    @Test
    void rejectsThemeIdsOutsideSafeAsciiRule() {
        ThemePathPolicy policy = new ThemePathPolicy(temporaryDirectory);
        List<String> invalidIds = Arrays.asList(
                "", " ", ".", "..", "../escape", "theme/escape", "theme\\escape",
                "/absolute", "C:\\absolute", "Uppercase", "theme id");

        assertThrows(IllegalArgumentException.class, () -> policy.requireValidThemeId(null));
        for (String id : invalidIds) {
            assertThrows(IllegalArgumentException.class, () -> policy.requireValidThemeId(id));
        }
    }

    @Test
    void resolvesThemePathsInsideRoot() throws IOException {
        ThemePathPolicy policy = new ThemePathPolicy(temporaryDirectory);
        Path directory = temporaryDirectory.resolve("custom_id").toAbsolutePath();
        Path file = directory.resolve("theme.json");

        assertEquals(directory, policy.resolveThemeDirectory("custom_id"));
        assertEquals(file, policy.resolveThemeFile("custom_id"));
        assertEquals(file, policy.resolveIndexEntry("custom_id/theme.json"));
        assertEquals(file, policy.resolveIndexEntry("themes/custom_id/theme.json"));
        assertEquals(file, policy.resolveIndexEntry("ingameime:themes/custom_id/theme.json"));
        assertEquals(file, policy.resolveIndexEntry("ingameime:custom_id/theme.json"));

        Path hyphenDirectory = temporaryDirectory.resolve("with-hyphen").toAbsolutePath();
        assertEquals(
                hyphenDirectory.resolve("theme.json"),
                policy.resolveIndexEntry("with-hyphen/theme.json"));
    }

    @Test
    void rejectsEscapingAndMalformedIndexEntries() {
        ThemePathPolicy policy = new ThemePathPolicy(temporaryDirectory);
        List<String> entries = Arrays.asList(
                "", "custom_id", "themes/", "../outside.json", "custom_id/../../outside.json",
                "/custom_id/theme.json", "C:\\outside\\theme.json", "custom_id\\theme.json",
                "custom_id/other.json", "custom_id/nested/theme.json", "themes/../outside/theme.json",
                "custom_id/theme.json/", "custom_id/Theme.json");

        assertThrows(IllegalArgumentException.class, () -> policy.resolveIndexEntry(null));
        for (String entry : entries) {
            assertThrows(IllegalArgumentException.class, () -> policy.resolveIndexEntry(entry));
        }
    }

    @Test
    void rejectsExistingSymlinkThatEscapesRoot() throws IOException {
        Path root = Files.createDirectory(temporaryDirectory.resolve("themes"));
        Path outside = Files.createDirectory(temporaryDirectory.resolve("outside"));
        try {
            Files.createSymbolicLink(root.resolve("custom_id"), outside);
        } catch (IOException | UnsupportedOperationException | SecurityException e) {
            Assumptions.assumeTrue(false, "Symbolic links unavailable: " + e.getMessage());
        }

        ThemePathPolicy policy = new ThemePathPolicy(root);
        IOException error = assertThrows(IOException.class, () -> policy.resolveThemeFile("custom_id"));
        assertTrue(error.getMessage().contains("escapes user theme root"));
    }

    @Test
    void resolvesThroughSymlinkedRoot() throws IOException {
        Path realRoot = Files.createDirectory(temporaryDirectory.resolve("real-themes"));
        Path linkedRoot = temporaryDirectory.resolve("linked-themes");
        try {
            Files.createSymbolicLink(linkedRoot, realRoot);
        } catch (IOException | UnsupportedOperationException | SecurityException e) {
            Assumptions.assumeTrue(false, "Symbolic links unavailable: " + e.getMessage());
        }

        ThemePathPolicy policy = new ThemePathPolicy(linkedRoot);
        Path expected = linkedRoot.toAbsolutePath().normalize()
                .resolve("custom_id")
                .resolve("theme.json");
        assertEquals(expected, policy.resolveThemeFile("custom_id"));
    }

    @Test
    void treatsRootWithTrailingSeparatorAsSameRoot() throws IOException {
        ThemePathPolicy plain = new ThemePathPolicy(temporaryDirectory);
        ThemePathPolicy trailing =
                new ThemePathPolicy(Paths.get(temporaryDirectory.toString() + "/"));

        assertEquals(plain.resolveThemeDirectory("custom_id"), trailing.resolveThemeDirectory("custom_id"));
        assertEquals(plain.resolveThemeFile("custom_id"), trailing.resolveThemeFile("custom_id"));
    }

    @Test
    void validatesPlainTextureFileNames() {
        for (String name : Arrays.asList("overlay.png", "bg_2.PNG", "my-texture.jpg", "a")) {
            assertEquals(name, ThemePathPolicy.requireSafeFileName(name));
        }

        List<String> unsafeNames = Arrays.asList(
                "", ".", "..", ".hidden", "../secret.png", "a/b.png", "a\\b.png",
                "sub/dir/overlay.png", "C:\\secret.png", "name:with-colon");
        assertThrows(IllegalArgumentException.class, () -> ThemePathPolicy.requireSafeFileName(null));
        for (String name : unsafeNames) {
            assertThrows(IllegalArgumentException.class, () -> ThemePathPolicy.requireSafeFileName(name));
        }
    }
}

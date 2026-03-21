package com.dhj.ingameime;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;
import java.util.Arrays;

public class Config {
    public static final String[] CATEGORIES = new String[]{"api", "uiless", "general", "modetext", "debug", "theme"};
    private static final String PREFIX = IngameIME_Forge.MOD_ID + ".config.";

    private static Configuration config;

    public static String API_Windows = "TextServiceFramework";
    public static boolean UiLess_Windows = true;

    public static boolean TurnOffOnMouseMove = true;

    public static String AlphaModeText = "A";
    public static String NativeModeText = "中";

    public static boolean DebugLog = false;

    public static void init(File configFile) {
        if (config == null) {
            config = new Configuration(configFile);
            config.load();
        }
        sync();
    }

    public static void sync() {
        final Property P_API_Windows = config.get(
                CATEGORIES[0],
                "Windows",
                API_Windows,
                "Config the API to use on Windows platform. \nAvailable: TextServiceFramework, Imm32"
        ).setLanguageKey(PREFIX + CATEGORIES[0] + ".windows").setValidValues(new String[]{"TextServiceFramework", "Imm32"}).setRequiresMcRestart(true);
        if (Arrays.stream(P_API_Windows.getValidValues()).noneMatch(it -> it.equals(P_API_Windows.getString()))) {
            P_API_Windows.set(P_API_Windows.getDefault());
        }
        API_Windows = P_API_Windows.getString();

        UiLess_Windows = config.get(
                CATEGORIES[1],
                "Windows",
                UiLess_Windows,
                "Config if render in-game candidate list."
        ).setLanguageKey(PREFIX + CATEGORIES[1] + ".windows").setRequiresMcRestart(true).getBoolean();

        TurnOffOnMouseMove = config.get(
                CATEGORIES[2],
                "TurnOffOnMouseMove",
                TurnOffOnMouseMove,
                "Turn off Input Method on mouse move."
        ).setLanguageKey(PREFIX + CATEGORIES[2] + ".turn_off_on_mouse_move").getBoolean();

        AlphaModeText = config.get(
                CATEGORIES[3],
                "AlphaMode",
                AlphaModeText,
                "Text to display when in Alpha mode."
        ).setLanguageKey(PREFIX + CATEGORIES[3] + ".alpha_mode").getString();

        NativeModeText = config.get(
                CATEGORIES[3],
                "NativeMode",
                NativeModeText,
                "Text to display when in Native mode."
        ).setLanguageKey(PREFIX + CATEGORIES[3] + ".native_mode").getString();

        DebugLog = config.get(
                CATEGORIES[4],
                "DebugLog",
                DebugLog,
                "Config if print debug log."
        ).setLanguageKey(PREFIX + CATEGORIES[4] + ".debug_log").getBoolean();

        // Theme category - just a placeholder to show in config GUI
        // Actual theme configuration is handled by ThemeManager
        config.get(
                CATEGORIES[5],
                "_comment",
                "Open the in-game config GUI to access the Theme Editor",
                "Theme configuration is managed through the Theme Editor in the config GUI"
        ).setLanguageKey(PREFIX + CATEGORIES[5] + ".comment");

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static Configuration getConfig() {
        return config;
    }
}

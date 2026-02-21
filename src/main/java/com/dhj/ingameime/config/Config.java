package com.dhj.ingameime.config;

import com.dhj.ingameime.Tags;
import com.dhj.ingameime.theme.ThemeManager;
import com.dhj.ingameime.theme.ThemeType;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;
import java.util.Arrays;

public class Config {
    public static final String[] CATEGORIES = new String[]{"api", "uiless", "general", "modetext", "debug", "theme"};
    private static final String PREFIX = Tags.MOD_ID + ".config.";

    private static Configuration config;
    
    // 主题变更监听器
    private static ThemeChangeListener themeChangeListener;

    public static String API_Windows = "TextServiceFramework";
    public static boolean UiLess_Windows = true;

    public static boolean TurnOffOnMouseMove = true;

    public static String AlphaModeText = "A";
    public static String NativeModeText = "中";

    public static boolean DebugLog = false;
    
    // 主题配置
    public static ThemeType CurrentTheme = ThemeType.DEFAULT;

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
        
        // 主题配置 - 保存旧值用于比较
        ThemeType oldTheme = CurrentTheme;
        
        Property themeProp = config.get(
                CATEGORIES[5],
                "currentTheme",
                CurrentTheme.getId(),
                "Select the current theme.\nAvailable: default, dark, light, custom"
        ).setLanguageKey(PREFIX + CATEGORIES[5] + ".current_theme").setValidValues(ThemeType.getIds());
        
        String themeId = themeProp.getString();
        CurrentTheme = ThemeType.fromId(themeId);
        themeProp.set(CurrentTheme.getId());

        // 如果主题发生变化，通知监听器
        if (oldTheme != CurrentTheme && themeChangeListener != null) {
            themeChangeListener.onThemeChanged(CurrentTheme);
        }

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static Configuration getConfig() {
        return config;
    }
    
    /**
     * 设置当前主题
     */
    public static void setCurrentTheme(ThemeType themeType) {
        ThemeType oldTheme = CurrentTheme;
        CurrentTheme = themeType;
        if (config != null) {
            config.get(CATEGORIES[5], "currentTheme", CurrentTheme.getId()).set(themeType.getId());
            if (config.hasChanged()) {
                config.save();
            }
        }
        
        // 如果主题发生变化，通知监听器
        if (oldTheme != themeType && themeChangeListener != null) {
            themeChangeListener.onThemeChanged(themeType);
        }
    }
    
    /**
     * 获取当前主题ID
     */
    public static String getCurrentThemeId() {
        return CurrentTheme.getId();
    }
    
    /**
     * 设置主题变更监听器
     */
    public static void setThemeChangeListener(ThemeChangeListener listener) {
        themeChangeListener = listener;
    }
    
    /**
     * 主题变更监听器接口
     */
    public interface ThemeChangeListener {
        void onThemeChanged(ThemeType newTheme);
    }
}

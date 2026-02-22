package com.dhj.ingameime.theme;

import com.dhj.ingameime.theme.api.Theme;
import com.dhj.ingameime.theme.api.ThemeManager;

/**
 * Theme type enumeration
 */
public enum ThemeType {
    DEFAULT("default"),
    DARK("dark"),
    LIGHT("light"),
    CUSTOM("custom");
    
    private final String id;
    
    ThemeType(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
    }
    
    /**
     * Get the display name (from ThemeManager).
     */
    public String getDisplayName() {
        Theme theme = ThemeManager.getInstance().getTheme(id);
        if (theme != null) {
            return theme.getName();
        }
        // If the theme is not loaded, return the ID as a fallback.
        return id;
    }
    
    /**
     * Get theme type by ID
     */
    public static ThemeType fromId(String id) {
        if (id == null || id.isEmpty()) {
            return DEFAULT;
        }
        
        for (ThemeType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        
        // If it is not a predefined theme, it is considered a custom theme.
        return CUSTOM;
    }
    
    /**
     * Retrieve all predefined theme types (excluding CUSTOM).
     */
    public static ThemeType[] getPredefinedThemes() {
        return new ThemeType[]{DEFAULT, DARK, LIGHT};
    }
    
    /**
     * Get the array of display names for all theme types
     */
    public static String[] getDisplayNames() {
        ThemeType[] types = values();
        String[] names = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            names[i] = types[i].getDisplayName();
        }
        return names;
    }
    
    /**
     * Get the array of IDs for all theme types
     */
    public static String[] getIds() {
        ThemeType[] types = values();
        String[] ids = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            ids[i] = types[i].getId();
        }
        return ids;
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }
}
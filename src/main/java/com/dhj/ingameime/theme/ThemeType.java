package com.dhj.ingameime.theme;

/**
 * 主题类型枚举
 */
public enum ThemeType {
    DEFAULT("default", "默认主题"),
    DARK("dark", "深色主题"),
    LIGHT("light", "浅色主题"),
    CUSTOM("custom", "自定义主题");
    
    private final String id;
    private final String displayName;
    
    ThemeType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }
    
    public String getId() {
        return id;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 根据ID获取主题类型
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
        
        // 如果不是预定义主题，则认为是自定义主题
        return CUSTOM;
    }
    
    /**
     * 获取所有预定义主题类型（不包括CUSTOM）
     */
    public static ThemeType[] getPredefinedThemes() {
        return new ThemeType[]{DEFAULT, DARK, LIGHT};
    }
    
    /**
     * 获取所有主题类型的显示名称数组
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
     * 获取所有主题类型的ID数组
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
        return displayName;
    }
}
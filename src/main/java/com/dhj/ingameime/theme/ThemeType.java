package com.dhj.ingameime.theme;

/**
 * 主题类型枚举
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
     * 获取显示名称（从ThemeManager获取）
     */
    public String getDisplayName() {
        Theme theme = ThemeManager.getInstance().getTheme(id);
        if (theme != null) {
            return theme.getName();
        }
        // 如果主题未加载，返回ID作为后备
        return id;
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
        return getDisplayName();
    }
}
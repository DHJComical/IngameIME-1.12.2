package com.dhj.ingameime.theme;

/**
 * 主题类，包含所有可配置的UI属性
 */
public class Theme {
    private String id;
    private String name;
    private int textColor;
    private int backgroundColor;
    private int indexColor;
    private int selectedBackgroundColor;
    private int cursorColor;
    private int padding;
    private int candidatePadding;
    private int borderWidth;
    private int borderColor;
    
    // 默认构造函数用于JSON反序列化
    public Theme() {
    }
    
    public Theme(String id, String name, int textColor, int backgroundColor, int indexColor, 
                 int selectedBackgroundColor, int cursorColor, int padding, int candidatePadding,
                 int borderWidth, int borderColor) {
        this.id = id;
        this.name = name;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        this.indexColor = indexColor;
        this.selectedBackgroundColor = selectedBackgroundColor;
        this.cursorColor = cursorColor;
        this.padding = padding;
        this.candidatePadding = candidatePadding;
        this.borderWidth = borderWidth;
        this.borderColor = borderColor;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getTextColor() {
        return textColor;
    }
    
    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }
    
    public int getBackgroundColor() {
        return backgroundColor;
    }
    
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
    
    public int getIndexColor() {
        return indexColor;
    }
    
    public void setIndexColor(int indexColor) {
        this.indexColor = indexColor;
    }
    
    public int getSelectedBackgroundColor() {
        return selectedBackgroundColor;
    }
    
    public void setSelectedBackgroundColor(int selectedBackgroundColor) {
        this.selectedBackgroundColor = selectedBackgroundColor;
    }
    
    public int getCursorColor() {
        return cursorColor;
    }
    
    public void setCursorColor(int cursorColor) {
        this.cursorColor = cursorColor;
    }
    
    public int getPadding() {
        return padding;
    }
    
    public void setPadding(int padding) {
        this.padding = padding;
    }
    
    public int getCandidatePadding() {
        return candidatePadding;
    }
    
    public void setCandidatePadding(int candidatePadding) {
        this.candidatePadding = candidatePadding;
    }
    
    public int getBorderWidth() {
        return borderWidth;
    }
    
    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
    }
    
    public int getBorderColor() {
        return borderColor;
    }
    
    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
    }
    
    /**
     * 获取带透明度的背景颜色
     */
    public int getBackgroundColorWithAlpha(float alpha) {
        int a = (int)(alpha * 255) << 24;
        return (backgroundColor & 0x00FFFFFF) | a;
    }
    
    /**
     * 获取带透明度的边框颜色
     */
    public int getBorderColorWithAlpha(float alpha) {
        int a = (int)(alpha * 255) << 24;
        return (borderColor & 0x00FFFFFF) | a;
    }
    
    /**
     * 复制主题
     */
    public Theme copy() {
        return new Theme(
            id + "_copy",
            name + " (副本)",
            textColor,
            backgroundColor,
            indexColor,
            selectedBackgroundColor,
            cursorColor,
            padding,
            candidatePadding,
            borderWidth,
            borderColor
        );
    }
    
    /**
     * 创建自定义主题
     */
    public static Theme createCustomTheme(String id, String name) {
        return new Theme(
            id,
            name,
            0xFF000000,  // 文字颜色
            0xEBEBEBEB,  // 背景颜色
            0xFF555555,  // 索引颜色
            0xEBEBEBEB,  // 选中项背景
            0xFF000000,  // 光标颜色
            3,           // 内边距
            5,           // 候选框内边距
            1,           // 边框宽度
            0x80000000   // 边框颜色
        );
    }
}
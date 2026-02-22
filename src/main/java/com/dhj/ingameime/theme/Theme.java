package com.dhj.ingameime.theme;

import com.google.gson.annotations.JsonAdapter;

/**
 * Theme class, containing all configurable UI properties.
 */
public class Theme {
    private String id;
    private String name;

    @JsonAdapter(ColorTypeAdapter.class)
    private int textColor;
    @JsonAdapter(ColorTypeAdapter.class)
    private int backgroundColor;
    @JsonAdapter(ColorTypeAdapter.class)
    private int indexColor;
    @JsonAdapter(ColorTypeAdapter.class)
    private int selectedBackgroundColor;
    @JsonAdapter(ColorTypeAdapter.class)
    private int cursorColor;
    @JsonAdapter(ColorTypeAdapter.class)
    private int borderColor;

    private int padding;
    private int candidatePadding;
    private int borderWidth;
    
    // The default constructor is used for JSON deserialization.
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
        padding = Math.max(0, padding);
        this.padding = padding;
    }
    
    public int getCandidatePadding() {
        return candidatePadding;
    }
    
    public void setCandidatePadding(int candidatePadding) {
        candidatePadding = Math.max(0, candidatePadding);
        this.candidatePadding = candidatePadding;
    }
    
    public int getBorderWidth() {
        return borderWidth;
    }
    
    public void setBorderWidth(int borderWidth) {
        borderWidth = Math.max(0, borderWidth);
        this.borderWidth = borderWidth;
    }
    
    public int getBorderColor() {
        return borderColor;
    }
    
    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
    }
    
    /**
     * Get background color with transparency
     */
    public int getBackgroundColorWithAlpha(float alpha) {
        alpha = Math.max(0.0f, Math.min(1.0f, alpha));
        int a = (int)(alpha * 255) << 24;
        return (backgroundColor & 0x00FFFFFF) | a;
    }
    
    /**
     * Get the border color with transparency
     */
    public int getBorderColorWithAlpha(float alpha) {
        alpha = Math.max(0.0f, Math.min(1.0f, alpha));
        int a = (int)(alpha * 255) << 24;
        return (borderColor & 0x00FFFFFF) | a;
    }
    
    /**
     * Copy theme
     */
    public Theme copy() {
        return new Theme(
            id + "_copy",
            name + " (copy)",
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
     * Create a custom theme
     */
    public static Theme createCustomTheme(String id, String name) {
        return new Theme(
            id,
            name,
            0xFF000000,  // Text color
            0xEBEBEBEB,  // Background color
            0xFF555555,  // Index color
            0xEBEBEBEB,  // Selected background color
            0xFF000000,  // Cursor color
            3,           // Padding
            5,           // Candidate padding
            1,           // Border width
            0x80000000   // Border color
        );
    }
}
package com.dhj.ingameime.theme.api;

import com.dhj.ingameime.theme.ColorTypeAdapter;
import java.util.ArrayList;
import java.util.List;

public class Theme {
    private String id;
    private String name;

    private int textColor, backgroundColor, indexColor, selectedBackgroundColor, cursorColor, borderColor;
    private int padding, candidatePadding, borderWidth;
    private String textureFile = "";
    private int u = 0, v = 0, sliceSize = 0, cornerSize = 0, textureWidth = 256, textureHeight = 256;
    private List<ThemeDecoration> decorations = new ArrayList<>();

    public Theme() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getTextColor() { return textColor; }
    public void setTextColor(int c) { this.textColor = c; }
    public int getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(int c) { this.backgroundColor = c; }
    public int getIndexColor() { return indexColor; }
    public void setIndexColor(int c) { this.indexColor = c; }
    public int getSelectedBackgroundColor() { return selectedBackgroundColor; }
    public void setSelectedBackgroundColor(int c) { this.selectedBackgroundColor = c; }
    public int getCursorColor() { return cursorColor; }
    public void setCursorColor(int c) { this.cursorColor = c; }
    public int getBorderColor() { return borderColor; }
    public void setBorderColor(int c) { this.borderColor = c; }
    public int getPadding() { return padding; }
    public void setPadding(int p) { this.padding = Math.max(0, p); }
    public int getCandidatePadding() { return candidatePadding; }
    public void setCandidatePadding(int p) { this.candidatePadding = Math.max(0, p); }
    public int getBorderWidth() { return borderWidth; }
    public void setBorderWidth(int b) { this.borderWidth = Math.max(0, b); }

    public String getTextureFile() { return textureFile; }
    public int getU() { return u; } public void setU(int i) { u = i; }
    public int getV() { return v; } public void setV(int i) { v = i; }
    public int getSliceSize() { return sliceSize; } public void setSliceSize(int i) { sliceSize = i; }
    public int getCornerSize() { return cornerSize; } public void setCornerSize(int i) { cornerSize = i; }
    public int getTextureWidth() { return textureWidth; } public void setTextureWidth(int i) { textureWidth = i; }
    public int getTextureHeight() { return textureHeight; } public void setTextureHeight(int i) { textureHeight = i; }

    public List<ThemeDecoration> getDecorations() { return decorations; }

    public static Theme createCustomTheme(String id, String name) {
        return new Theme(id, name, 0xFF000000, 0xEBEBEBEB, 0xFF555555, 0xEBEBEBEB, 0xFF000000, 3, 5, 1, 0x80000000);
    }

    public Theme(String id, String name, int txt, int bg, int idx, int sel, int cur, int pad, int cpad, int bwd, int bcl) {
        this.id = id; this.name = name; this.textColor = txt; this.backgroundColor = bg;
        this.indexColor = idx; this.selectedBackgroundColor = sel; this.cursorColor = cur;
        this.padding = pad; this.candidatePadding = cpad; this.borderWidth = bwd; this.borderColor = bcl;
    }

    // Manual serialization helpers for 1.7.10 (Gson @JsonAdapter not available)
    public static void registerTypeAdapter(com.google.gson.GsonBuilder builder) {
        builder.registerTypeAdapter(Integer.class, new ColorTypeAdapter());
        builder.registerTypeAdapter(int.class, new ColorTypeAdapter());
    }
}

package com.dhj.ingameime.gui;

import com.dhj.ingameime.theme.api.Theme;
import com.dhj.ingameime.theme.api.ThemeManager;
import com.dhj.ingameime.theme.api.ThemeRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

public class Widget extends Gui implements ThemeManager.ThemeChangeListener {
    public int offsetX, offsetY;
    public int TextColor;
    public int Background;
    public int Padding = 1;
    public int X, Y;
    public int Width, Height;
    public boolean DrawInline = true;
    protected boolean isDirty = true;
    
    public Widget() {
        updateThemeColors();
        ThemeManager.getInstance().addThemeChangeListener(this);
    }
    
    protected void updateThemeColors() {
        Theme theme = ThemeManager.getInstance().getCurrentTheme();
        if (theme != null) {
            TextColor = theme.getTextColor();
            Background = theme.getBackgroundColor();
            Padding = theme.getPadding();
        }
    }
    
    /**
     * Force update theme colors
     */
    public void refreshThemeColors() {
        updateThemeColors();
        isDirty = true;
    }
    
    /**
     * Theme change listener callback
     */
    @Override
    public void onThemeChanged(Theme newTheme) {
        refreshThemeColors();
    }

    public boolean isActive() {
        return false;
    }

    public void layout() {
        Minecraft mc = Minecraft.getMinecraft();

        int totalWidth = Width + 2 * Padding;
        int totalHeight = Height + 2 * Padding;

        X = offsetX;
        Y = offsetY;
        if (!DrawInline) {
            Y += mc.fontRenderer.FONT_HEIGHT;
        }

        ScaledResolution scaledresolution = new ScaledResolution(mc);
        int displayHeight = scaledresolution.getScaledHeight();
        int displayWidth = scaledresolution.getScaledWidth();

        if (X + totalWidth > displayWidth) X = Math.max(0, displayWidth - totalWidth);
        if (Y + totalHeight > displayHeight) {
            int yAbove = offsetY - totalHeight - 2;
            if (yAbove >= 0) {
                Y = yAbove;
            } else {
                Y = displayHeight - totalHeight;
            }
        }

        isDirty = false;
    }

    public void draw() {
        Theme theme = ThemeManager.getInstance().getCurrentTheme();
        if (theme == null) return;
        ThemeRenderer.render(theme, X, Y, Width + 2 * Padding, Height + 2 * Padding, getComponentId());
    }

    protected String getComponentId() {
        return "generic";
    }

    public void setPos(int x, int y) {
        if (offsetX == x && offsetY == y) return;
        offsetX = x;
        offsetY = y;
        isDirty = true;
    }
}

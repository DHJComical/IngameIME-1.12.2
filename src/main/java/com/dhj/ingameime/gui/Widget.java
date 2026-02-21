package com.dhj.ingameime.gui;

import com.dhj.ingameime.theme.Theme;
import com.dhj.ingameime.theme.ThemeManager;
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
     * 强制更新主题颜色
     */
    public void refreshThemeColors() {
        updateThemeColors();
        isDirty = true;
    }
    
    /**
     * 主题变更监听器回调
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
            int yAbove = offsetY - totalHeight;
            if (yAbove >= 0) {
                Y = yAbove;
            } else {
                Y = displayHeight - totalHeight;
            }
        }

        isDirty = false;
    }

    public void draw() {
        drawRect(X, Y, X + Width + 2 * Padding, Y + Height + 2 * Padding, Background);
    }

    public void setPos(int x, int y) {
        if (offsetX == x && offsetY == y) return;
        offsetX = x;
        offsetY = y;
        isDirty = true;
    }
}
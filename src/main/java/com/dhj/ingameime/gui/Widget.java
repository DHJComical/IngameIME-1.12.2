package com.dhj.ingameime.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

public class Widget extends Gui {
    public int offsetX, offsetY;
    public int TextColor = 0xFF_00_00_00;
    public int Background = 0xEB_EB_EB_EB;
    public int Padding = 1;
    public int X, Y;
    public int Width, Height;
    public boolean DrawInline = true;
    protected boolean isDirty = true;

    public boolean isActive() {
        return false;
    }

    public void layout() {
        int totalWidth = Width + 2 * Padding;
        int totalHeight = Height + 2 * Padding;

        X = offsetX;
        Y = offsetY;
        if (!DrawInline) {
            Y += Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
        }

        ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
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
package com.dhj.ingameime.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import static com.dhj.ingameime.Config.AlphaModeText;
import static com.dhj.ingameime.Config.NativeModeText;

public class WidgetInputMode extends Widget {
    public final long ActiveTime = 3000;
    private long LastActive = 0;
    private boolean isNativeMode = false;

    public WidgetInputMode() {
        DrawInline = false;
        updateThemeColors();
    }

    @Override
    protected void updateThemeColors() {
        super.updateThemeColors();
    }

    @Override
    public boolean isActive() {
        return System.currentTimeMillis() - LastActive <= ActiveTime;
    }

    public void setActive(boolean active) {
        if (active) LastActive = System.currentTimeMillis();
        else LastActive = 0;
    }

    public void setMode(boolean nativeMode) {
        isNativeMode = nativeMode;
        setActive(true);
        isDirty = true;
        layout();
    }

    @Override
    public void layout() {
        if (!isDirty) return;
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        Height = font.FONT_HEIGHT;
        Width = font.getStringWidth(isNativeMode ? NativeModeText : AlphaModeText);
        super.layout();
    }

    @Override
    public void draw() {
        if (!isActive()) return;
        if (isDirty) layout();
        super.draw();
        String text = isNativeMode ? NativeModeText : AlphaModeText;
        Minecraft.getMinecraft().fontRenderer.drawString(text, X + Padding, Y + Padding, TextColor);
    }
}

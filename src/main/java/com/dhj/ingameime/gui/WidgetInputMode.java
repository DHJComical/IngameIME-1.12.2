package com.dhj.ingameime.gui;

import ingameime.InputMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import static com.dhj.ingameime.config.Config.AlphaModeText;
import static com.dhj.ingameime.config.Config.NativeModeText;

public class WidgetInputMode extends Widget {
    public final long ActiveTime = 3000;
    private long LastActive = 0;
    private InputMode Mode = InputMode.AlphaNumeric;

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

    public void setMode(InputMode mode) {
        Mode = mode;
        setActive(true);
        isDirty = true;
        layout();
    }

    @Override
    public void layout() {
        if (!isDirty) return;
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        Height = font.FONT_HEIGHT;
        if (Mode == InputMode.AlphaNumeric)
            Width = font.getStringWidth(AlphaModeText);
        else
            Width = font.getStringWidth(NativeModeText);
        super.layout();
    }

    @Override
    public void draw() {
        if (!isActive()) return;
        if (isDirty) layout();
        super.draw();
        if (Mode == InputMode.AlphaNumeric)
            Minecraft.getMinecraft().fontRenderer.drawString(AlphaModeText, X + Padding, Y + Padding, TextColor);
        else
            Minecraft.getMinecraft().fontRenderer.drawString(NativeModeText, X + Padding, Y + Padding, TextColor);
    }
}
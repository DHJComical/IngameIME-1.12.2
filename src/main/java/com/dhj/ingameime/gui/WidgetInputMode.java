package com.dhj.ingameime.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import static com.dhj.ingameime.config.Config.AlphaModeText;
import static com.dhj.ingameime.config.Config.NativeModeText;
import static com.dhj.ingameime.config.Config.UnsupportedModeText;

public class WidgetInputMode extends Widget {
    public final long ActiveTime = 3000;
    private long LastActive = 0;
    private int mode = 0;  // 0=Alpha, 1=Native, 2=Unsupported

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

    public void setMode(int mode) {
        this.mode = mode;
        setActive(true);
        isDirty = true;
        layout();
    }

    private String getModeText() {
        switch (mode) {
            case 1:
                return NativeModeText;
            case 2:
                return UnsupportedModeText;
            default:
                return AlphaModeText;
        }
    }

    @Override
    public void layout() {
        if (!isDirty) return;
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        if (font == null) {
            return;
        }
        Height = font.FONT_HEIGHT;
        Width = font.getStringWidth(getModeText());
        super.layout();
    }

    @Override
    public void draw() {
        if (!isActive()) return;
        if (isDirty) layout();
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        if (font == null) {
            return;
        }
        super.draw();
        font.drawString(getModeText(), X + Padding, Y + Padding, TextColor);
    }
}

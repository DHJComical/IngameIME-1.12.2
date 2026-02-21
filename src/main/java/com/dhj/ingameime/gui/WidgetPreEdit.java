package com.dhj.ingameime.gui;

import com.dhj.ingameime.ClientProxy;
import com.dhj.ingameime.Internal;
import com.dhj.ingameime.theme.Theme;
import com.dhj.ingameime.theme.ThemeManager;
import ingameime.PreEditRect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;

public class WidgetPreEdit extends Widget {
    private final int CursorWidth = 3;
    private String Content = null;
    private int Cursor = -1;

    @Override
    protected void updateThemeColors() {
        super.updateThemeColors();
    }

    public void setContent(String content, int cursor) {
        Cursor = cursor;
        Content = content;
        isDirty = true;
        layout();
    }

    @Override
    public void layout() {
        if (!isDirty) return;
        if (isActive()) {
            FontRenderer font = Minecraft.getMinecraft().fontRenderer;
            Width = font.getStringWidth(Content) + CursorWidth;
            Height = font.FONT_HEIGHT;
        } else {
            Width = Height = 0;
        }

        X = offsetX - Padding;
        Y = offsetY - Padding;

        isDirty = false;

        if (!Internal.LIBRARY_LOADED || Internal.InputCtx == null) return;
        PreEditRect rect = new PreEditRect();
        rect.setX(X);
        rect.setY(Y);
        rect.setHeight(Height);
        rect.setWidth(Width);
        Internal.InputCtx.setPreEditRect(rect);
    }

    @Override
    public boolean isActive() {
        return Content != null && !Content.isEmpty();
    }

    @Override
    public void draw() {
        if (!isActive()) return;

        if (isDirty) layout();

        WidgetCandidateList list = ClientProxy.Screen.CandidateList;
        if (list != null && list.isActive()) {
            Minecraft mc = Minecraft.getMinecraft();
            list.DrawInline = true;
            int targetX = X;
            int targetY = getTargetY(list, mc);
            list.setPos(targetX, targetY);
        }
        super.draw();

        FontRenderer font = Minecraft.getMinecraft().fontRenderer;

        String beforeCursor = "";
        String afterCursor = "";
        if (Content != null && Cursor >= 0 && Cursor <= Content.length()) {
            beforeCursor = Content.substring(0, Cursor);
            afterCursor = Content.substring(Cursor);
        } else if (Content != null) {
            beforeCursor = Content;
        }

        int x = font.drawString(beforeCursor, X + Padding, Y + Padding, TextColor);
        Theme theme = ThemeManager.getInstance().getCurrentTheme();
        int cursorColor = (theme != null) ? theme.getCursorColor() : TextColor;
        drawRect(x + 1, Y + Padding, x + 2, Y + Padding + Height, cursorColor);
        font.drawString(afterCursor, x + CursorWidth, Y + Padding, TextColor);
    }

    private int getTargetY(WidgetCandidateList list, Minecraft mc) {
        int myTotalHeight = Height + 2 * Padding;
        int listExpectedHeight = (list.Height > 0 ? list.Height : mc.fontRenderer.FONT_HEIGHT) + 2 * list.Padding;
        ScaledResolution sr = new ScaledResolution(mc);
        int displayHeight = sr.getScaledHeight();
        int targetY = Y + myTotalHeight;
        if (targetY + listExpectedHeight >= displayHeight - 5) {
            targetY = Y - listExpectedHeight;
        }
        return targetY;
    }
}
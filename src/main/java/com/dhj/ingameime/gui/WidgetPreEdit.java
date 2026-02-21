package com.dhj.ingameime.gui;

import com.dhj.ingameime.ClientProxy;
import com.dhj.ingameime.Internal;
import com.dhj.ingameime.theme.Theme;
import com.dhj.ingameime.theme.ThemeManager;
import ingameime.PreEditRect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class WidgetPreEdit extends Widget {
    private final int CursorWidth = 3;
    private String Content = null;
    private int Cursor = -1;
    
    @Override
    protected void updateThemeColors() {
        super.updateThemeColors();
        // 预编辑框可以使用与Widget相同的颜色
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
        super.layout();

        WidgetCandidateList list = ClientProxy.Screen.CandidateList;
        list.setPos(X, Y + Height);
        // Check if overlap
        if (list.Y < Y + Height) {
            list.setPos(X, Y - list.Height);
        }

        // Update Rect
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
        super.draw();
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        String beforeCursor = Content.substring(0, Cursor);
        String afterCursor = Content.substring(Cursor);
        int x = font.drawString(beforeCursor, X + Padding, Y + Padding, TextColor);
        
        // 使用主题中的光标颜色
        Theme theme = ThemeManager.getInstance().getCurrentTheme();
        int cursorColor = (theme != null) ? theme.getCursorColor() : TextColor;
        drawRect(x + 1, Y + Padding, x + 2, Y + Padding + Height, cursorColor);
        
        font.drawString(afterCursor, x + CursorWidth, Y + Padding, TextColor);
    }
}

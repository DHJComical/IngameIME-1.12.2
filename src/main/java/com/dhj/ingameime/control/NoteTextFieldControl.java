package com.dhj.ingameime.control;

import com.chaosthedude.notes.gui.GuiNoteTextField;
import com.dhj.ingameime.mixins.notes.AccessorGuiNoteTextField;
import net.minecraft.client.gui.FontRenderer;

import javax.annotation.Nonnull;
import java.awt.*;

public class NoteTextFieldControl extends AbstractControl<GuiNoteTextField> {

    public NoteTextFieldControl(GuiNoteTextField control) {
        super(control);
    }

    @Override
    public void writeText(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        controlObject.insert(text);
        controlObject.updateVisibleLines();
    }

    @Override
    public boolean isVisible() {
        AccessorGuiNoteTextField accessor = (AccessorGuiNoteTextField) controlObject;
        return accessor.isVisibleField() && accessor.isEnabled() && accessor.isFocused();
    }

    @Nonnull
    @Override
    public Point getCursorPos() {
        AccessorGuiNoteTextField accessor = (AccessorGuiNoteTextField) controlObject;
        FontRenderer font = accessor.getFontRenderer();
        int baseX = controlObject.xPosition + accessor.getMargin() - 1;
        int baseY = controlObject.yPosition + accessor.getMargin() - 1;
        if (font == null || !controlObject.cursorIsValid()) {
            return new Point(baseX, baseY);
        }

        int cursorLine = accessor.invokeGetCursorY();
        String line = controlObject.getLine(cursorLine);
        int cursorColumn = Math.min(accessor.invokeGetCursorX(), line.length());

        int cursorX = baseX + font.getStringWidth(line.substring(0, cursorColumn));
        int cursorY = baseY + (controlObject.getRenderSafeCursorY() * font.FONT_HEIGHT);
        return new Point(cursorX, cursorY);
    }
}

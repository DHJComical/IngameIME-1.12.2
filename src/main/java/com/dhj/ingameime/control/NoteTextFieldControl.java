package com.dhj.ingameime.control;

import com.chaosthedude.notes.gui.GuiNoteTextField;
import com.dhj.ingameime.ClientProxy;
import com.dhj.ingameime.mixins.notes.AccessorGuiNoteTextField;
import net.minecraft.client.gui.FontRenderer;

import javax.annotation.Nonnull;
import java.awt.*;

public class NoteTextFieldControl extends AbstractControl<GuiNoteTextField> {

    protected NoteTextFieldControl(GuiNoteTextField control) {
        super(control);
    }

    @Override
    public void writeText(String text) {
        if (this.controlObject != null && text != null && !text.isEmpty()) {
            this.controlObject.insert(text);
            this.controlObject.updateVisibleLines();
        }
    }

    @Override
    public boolean isVisible() {
        AccessorGuiNoteTextField accessor = (AccessorGuiNoteTextField) this.controlObject;
        return accessor.isVisibleField() && accessor.isEnabled() && accessor.isFocused();
    }

    @Nonnull
    @Override
    public Point getCursorPos() {
        AccessorGuiNoteTextField accessor = (AccessorGuiNoteTextField) this.controlObject;
        FontRenderer font = accessor.getFontRenderer();
        int baseX = this.controlObject.xPosition + accessor.getMargin() - 1;
        int baseY = this.controlObject.yPosition + accessor.getMargin() - 1;

        if (font == null || !this.controlObject.cursorIsValid()) {
            return new Point(baseX, baseY);
        }

        int cursorLine = accessor.invokeGetCursorY();
        String line = this.controlObject.getLine(cursorLine);
        int cursorColumn = Math.min(accessor.invokeGetCursorX(), line.length());

        int cursorX = baseX + font.getStringWidth(line.substring(0, cursorColumn));
        int cursorY = baseY + this.controlObject.getRenderSafeCursorY() * font.FONT_HEIGHT;
        return new Point(cursorX, cursorY);
    }

    public static void onFocusChange(GuiNoteTextField object, boolean focused) {
        ClientProxy.INSTANCE.onControlFocus(new NoteTextFieldControl(object), focused, false);
    }
}

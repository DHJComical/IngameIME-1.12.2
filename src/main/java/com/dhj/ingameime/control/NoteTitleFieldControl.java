package com.dhj.ingameime.control;

import com.chaosthedude.notes.gui.GuiNoteTitleField;
import com.dhj.ingameime.mixins.notes.AccessorGuiNoteTitleField;

import javax.annotation.Nonnull;
import java.awt.*;

public class NoteTitleFieldControl extends AbstractControl<GuiNoteTitleField> {

    public NoteTitleFieldControl(GuiNoteTitleField control) {
        super(control);
    }

    @Override
    public void writeText(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        controlObject.writeText(text);
    }

    @Override
    public boolean isVisible() {
        AccessorGuiNoteTitleField accessor = (AccessorGuiNoteTitleField) controlObject;
        return controlObject.getVisible() && accessor.isEnabled() && controlObject.isFocused();
    }

    @Nonnull
    @Override
    public Point getCursorPos() {
        AccessorGuiNoteTitleField accessor = (AccessorGuiNoteTitleField) controlObject;
        return AbstractControl.getCursorPos(
                accessor.getFontRenderer(),
                controlObject.getText(),
                controlObject.xPosition,
                controlObject.yPosition,
                controlObject.getWidth(),
                controlObject.height,
                accessor.getLineScrollOffset(),
                controlObject.getCursorPosition(),
                controlObject.getSelectionEnd(),
                controlObject.getEnableBackgroundDrawing()
        );
    }
}

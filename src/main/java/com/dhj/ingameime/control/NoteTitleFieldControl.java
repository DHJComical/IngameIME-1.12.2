package com.dhj.ingameime.control;

import com.chaosthedude.notes.gui.GuiNoteTitleField;
import com.dhj.ingameime.ClientProxy;
import com.dhj.ingameime.mixins.notes.AccessorGuiNoteTitleField;

import javax.annotation.Nonnull;
import java.awt.*;

public class NoteTitleFieldControl extends AbstractControl<GuiNoteTitleField> {

    protected NoteTitleFieldControl(GuiNoteTitleField control) {
        super(control);
    }

    @Override
    public void writeText(String text) {
        if (this.controlObject != null && text != null && !text.isEmpty()) {
            this.controlObject.writeText(text);
        }
    }

    @Override
    public boolean isVisible() {
        AccessorGuiNoteTitleField accessor = (AccessorGuiNoteTitleField) this.controlObject;
        return this.controlObject.getVisible() && accessor.isEnabled() && this.controlObject.isFocused();
    }

    @Nonnull
    @Override
    public Point getCursorPos() {
        AccessorGuiNoteTitleField accessor = (AccessorGuiNoteTitleField) this.controlObject;
        return getCursorPos(
                accessor.getFontRenderer(), this.controlObject.getText(),
                this.controlObject.xPosition, this.controlObject.yPosition, this.controlObject.getWidth(), this.controlObject.height,
                accessor.getLineScrollOffset(), this.controlObject.getCursorPosition(), this.controlObject.getSelectionEnd(),
                this.controlObject.getEnableBackgroundDrawing()
        );
    }

    public static void onFocusChange(GuiNoteTitleField object, boolean focused) {
        ClientProxy.INSTANCE.onControlFocus(new NoteTitleFieldControl(object), focused, false);
    }
}

package com.dhj.ingameime.control;

import com.dhj.ingameime.ClientProxy;
import com.dhj.ingameime.IngameIME_Forge;
import com.dhj.ingameime.UnicodeTextHelper;
import com.dhj.ingameime.mixins.vanilla.AccessorGuiTextField;
import net.minecraft.client.gui.GuiTextField;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;

public class VanillaTextFieldControl<T extends GuiTextField> extends AbstractControl<GuiTextField> {

    protected VanillaTextFieldControl(T control) {
        super(control);
    }

    @Override
    public void writeText(String text) throws IOException {
        if (this.controlObject == null || text == null || text.isEmpty()) {
            return;
        }

        text = UnicodeTextHelper.repairUtf8Mojibake(text);
        if (text == null || text.isEmpty()) {
            return;
        }

        if (requiresDirectWrite(text)) {
            IngameIME_Forge.logDebugInfo(
                    "[IME DirectWrite] targetObject={} text='{}' utf16=[{}] cp=[{}]",
                    this.controlObject.getClass().getName(),
                    UnicodeTextHelper.debugEscaped(text),
                    UnicodeTextHelper.debugUtf16(text),
                    UnicodeTextHelper.debugCodePoints(text)
            );
            this.controlObject.writeText(text);
        } else {
            super.writeText(text);
        }
    }

    private static boolean requiresDirectWrite(String text) {
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            i += Character.charCount(codePoint);
            if (Character.charCount(codePoint) > 1
                    || UnicodeTextHelper.isEmojiFormatCodePoint(codePoint)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isVisible() {
        return this.controlObject.getVisible();
    }

    @Nonnull
    @Override
    public Point getCursorPos() {
        AccessorGuiTextField accessor = (AccessorGuiTextField) this.controlObject;
        return AbstractControl.getCursorPos(
                accessor.getFont(), this.controlObject.getText(),
                this.controlObject.xPosition, this.controlObject.yPosition, this.controlObject.width, this.controlObject.height,
                accessor.getLineScrollOffset(), this.controlObject.getCursorPosition(), this.controlObject.getSelectionEnd(),
                this.controlObject.getEnableBackgroundDrawing()
        );
    }

    /**
     * Try to set the GuiTextField object focus.
     *
     * @param object The field to be set
     * @return Success or not
     */
    @SuppressWarnings("UnusedReturnValue")
    public static boolean onFocusChange(GuiTextField object, boolean focused) {
        ClientProxy.INSTANCE.onControlFocus(new VanillaTextFieldControl<>(object), focused, false);
        return true;
    }
}

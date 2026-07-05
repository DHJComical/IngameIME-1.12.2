package com.dhj.ingameime.control;

import com.dhj.ingameime.ClientProxy;
import com.dhj.ingameime.mixins.ftb.AccessorFTBTextBox;
import com.feed_the_beast.ftblib.lib.gui.TextBox;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;
import java.awt.*;

public class FTBTextFieldControl extends AbstractControl<TextBox> {

    public FTBTextFieldControl(TextBox controlObject) {
        super(controlObject);
    }

    @Override
    public boolean isVisible() {
        return controlObject.shouldDraw();
    }

    @Override
    @Nonnull
    public Point getCursorPos() {
        AccessorFTBTextBox acc = (AccessorFTBTextBox) controlObject;

        return getCursorPos(
                Minecraft.getMinecraft().fontRenderer,
                acc.getText(),
                controlObject.getX(),
                controlObject.getY(),
                controlObject.width - 10,
                controlObject.height,
                acc.getLineScrollOffset(),
                acc.getCursorPosition(),
                acc.getSelectionEnd(),
                true
        );
    }

    public static void onFocusChange(TextBox textField, boolean isFocused) {
        ClientProxy.getIMEventHandler().onControlFocus(new FTBTextFieldControl(textField), isFocused, false);
    }

    @Override
    public void writeText(String text) {
        controlObject.writeText(text);
    }
}

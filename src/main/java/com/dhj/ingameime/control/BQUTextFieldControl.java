package com.dhj.ingameime.control;

import betterquesting.api2.client.gui.controls.PanelTextField;
import betterquesting.api2.client.gui.misc.IGuiRect;
import com.dhj.ingameime.ClientProxy;
import com.dhj.ingameime.IMStates;
import com.dhj.ingameime.mixins.betterquesting.AccessorBQUPanelTextField;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;
import java.awt.*;

public class BQUTextFieldControl extends AbstractControl<PanelTextField<?>> {

    public BQUTextFieldControl(PanelTextField<?> controlObject) {
        super(controlObject);
    }

    @Override
    public boolean isVisible() {
        return controlObject.isEnabled();
    }

    @Override
    @Nonnull
    public Point getCursorPos() {
        AccessorBQUPanelTextField acc = (AccessorBQUPanelTextField) controlObject;
        IGuiRect rect = controlObject.getTransform();

        return getCursorPos(
                Minecraft.getMinecraft().fontRenderer,
                acc.getText(),
                rect.getX() - controlObject.getScrollX(),
                rect.getY() - controlObject.getScrollY(),
                rect.getWidth() - 8,
                rect.getHeight(),
                0,
                acc.getSelectStart(),
                acc.getSelectEnd(),
                true
        );
    }

    @Override
    public void writeText(String text) {
        controlObject.writeText(text);
    }

    public static void onFocusChange(PanelTextField<?> textField, boolean isFocused) {
        if (isFocused) {
            ClientProxy.getIMEventHandler().onControlFocus(new BQUTextFieldControl(textField), true, false);
        } else {
            if (IMStates.isControlObject(textField, false)) {
                ClientProxy.getIMEventHandler().onControlFocus(new BQUTextFieldControl(textField), false, false);
            }
        }
    }
}

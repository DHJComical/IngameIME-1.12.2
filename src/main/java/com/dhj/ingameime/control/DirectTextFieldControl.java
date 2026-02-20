package com.dhj.ingameime.control;

import com.dhj.ingameime.ClientProxy;
import net.minecraft.client.gui.GuiTextField;

public class DirectTextFieldControl<T extends GuiTextField> extends VanillaTextFieldControl<T> {

    protected DirectTextFieldControl(T control) {
        super(control);
    }

    @Override
    public void writeText(String text) {
        if (this.controlObject != null) {
            this.controlObject.writeText(text);
        }
    }

    public static boolean onFocusChange(GuiTextField object, boolean focused) {
        ClientProxy.INSTANCE.onControlFocus(new DirectTextFieldControl<>(object), focused, false);
        return focused;
    }
}
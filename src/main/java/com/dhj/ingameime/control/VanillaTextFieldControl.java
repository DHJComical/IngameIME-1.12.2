package com.dhj.ingameime.control;

import com.dhj.ingameime.IMStates;
import com.dhj.ingameime.mixins.vanilla.AccessorGuiTextField;
import net.minecraft.client.gui.GuiTextField;

public class VanillaTextFieldControl<T extends GuiTextField> extends AbstractControl<GuiTextField> {

    protected VanillaTextFieldControl(T control) {
        super(control);
    }

    @Override
    public boolean isVisible() {
        return this.controlObject.getVisible();
    }

    @Override
    public int getCursorX() {
        AccessorGuiTextField accessor = (AccessorGuiTextField) this.controlObject;
        return AbstractControl.getCursorX(accessor.getFont(), this.controlObject.getText(),
                this.controlObject.x, this.controlObject.getWidth(),
                accessor.getLineScrollOffset(), this.controlObject.getCursorPosition(),
                this.controlObject.getEnableBackgroundDrawing());
    }

    @Override
    public int getCursorY() {
        return AbstractControl.getCursorY(this.controlObject.y, this.controlObject.height, this.controlObject.getEnableBackgroundDrawing());
    }

    /**
     * Try to set the GuiTextField object focus.
     * @param object The field to be set
     * @return Success or not
     */
    @SuppressWarnings("UnusedReturnValue")
    public static boolean onFocus(GuiTextField object) {
        IMStates.setCommonControl(new VanillaTextFieldControl<>(object));
        return true;
    }
}

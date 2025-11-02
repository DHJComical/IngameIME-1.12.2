package com.dhj.ingameime.control;

import com.dhj.ingameime.IMStates;
import com.dhj.ingameime.mixins.flux.AccessorGuiTextField;
import sonar.fluxnetworks.client.gui.basic.GuiTextField;

public class FluxTextFieldControl<T extends GuiTextField> extends AbstractControl<GuiTextField> {

    protected FluxTextFieldControl(T control) {
        super(control);
    }

    @Override
    public void writeText(String text) {
        this.controlObject.writeText(text);
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
     */
    public static void onFocus(GuiTextField object) {
        IMStates.setCommonControl(new FluxTextFieldControl<>(object));
    }
}

package com.dhj.ingameime.control;

import com.dhj.ingameime.ClientProxy;
import com.dhj.ingameime.mixins.flux.AccessorGuiTextField;
import sonar.fluxnetworks.client.gui.basic.GuiTextField;

public class FluxTextFieldControl<T extends GuiTextField> extends AbstractControl<GuiTextField> {

    protected FluxTextFieldControl(T control) {
        super(control);
    }

    @Override
    public boolean isVisible() {
        return this.controlObject.getVisible();
    }

//    private String text;
//    private int x;
//    private int y;

    @Override
    public int getCursorX() {
        AccessorGuiTextField accessor = (AccessorGuiTextField) this.controlObject;
        String text = this.controlObject.isTextInvisible ? this.controlObject.getInvisibleText(this.controlObject.getText().length()) : this.controlObject.getText();
//        if (!Objects.equals(this.text, text) || this.x != this.controlObject.x || this.y != this.controlObject.y) {
//            IngameIME_Forge.LOG.info("text: {}, x: {}, y: {}", text, this.controlObject.x, this.controlObject.y);
//        }
//        this.text = text;
//        this.x = this.controlObject.x;
//        this.y = this.controlObject.y;
        return AbstractControl.getCursorX(accessor.getFont(), text,
                this.controlObject.x + 4, this.controlObject.getWidth(),
                accessor.getLineScrollOffset(), this.controlObject.getCursorPosition(), this.controlObject.getSelectionEnd(),
                this.controlObject.getEnableBackgroundDrawing());
    }

    @Override
    public int getCursorY() {
        return AbstractControl.getCursorY(this.controlObject.y + (this.controlObject.height - 8) / 2, this.controlObject.height, this.controlObject.getEnableBackgroundDrawing());
    }

    /**
     * Try to set the GuiTextField object focus.
     *
     * @param object The field to be set
     */
    public static void onFocusChange(GuiTextField object, boolean focused) {
        ClientProxy.INSTANCE.onControlFocus(new FluxTextFieldControl<>(object), focused, false);
    }
}

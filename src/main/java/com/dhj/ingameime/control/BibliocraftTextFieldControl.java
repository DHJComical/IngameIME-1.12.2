package com.dhj.ingameime.control;

import com.dhj.ingameime.ClientProxy;
import com.dhj.ingameime.mixins.bibliocraft.AccessorGuiBiblioTextField;
import jds.bibliocraft.gui.GuiBiblioTextField;

import javax.annotation.Nonnull;
import java.awt.*;

/**
 * Control wrapper for Bibliocraft's GuiBiblioTextField
 */
public class BibliocraftTextFieldControl<T extends GuiBiblioTextField> extends AbstractControl<GuiBiblioTextField> {

    protected BibliocraftTextFieldControl(T control) {
        super(control);
    }

    @Override
    public boolean isVisible() {
        return this.controlObject.getVisible();
    }

    @Nonnull
    @Override
    public Point getCursorPos() {
        AccessorGuiBiblioTextField accessor = (AccessorGuiBiblioTextField) this.controlObject;
        return AbstractControl.getCursorPos(
                accessor.getFontRenderer(), this.controlObject.getText(),
                accessor.getXPos(), accessor.getYPos(), accessor.getWidth(), accessor.getHeight(),
                accessor.getLineScrollOffset(), this.controlObject.getCursorPosition(), this.controlObject.getSelectionEnd(),
                this.controlObject.getEnableBackgroundDrawing()
        );
    }

    /**
     * Try to set the GuiBiblioTextField object focus.
     *
     * @param object The field to be set
     * @return Success or not
     */
    @SuppressWarnings("UnusedReturnValue")
    public static boolean onFocusChange(GuiBiblioTextField object, boolean focused) {
        ClientProxy.INSTANCE.onControlFocus(new BibliocraftTextFieldControl<>(object), focused, false);
        return true;
    }
}

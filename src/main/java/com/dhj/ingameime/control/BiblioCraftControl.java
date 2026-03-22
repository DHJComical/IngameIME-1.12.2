package com.dhj.ingameime.control;

import com.dhj.ingameime.mixins.bibliocraft.AccessorGuiBiblioTextField;
import jds.bibliocraft.gui.GuiBiblioTextField;
import net.minecraft.client.Minecraft;

import java.awt.*;

/**
 * Control implementation for BiblioCraft's GuiBiblioTextField
 */
public class BiblioCraftControl extends AbstractControl<GuiBiblioTextField> {
    public BiblioCraftControl(GuiBiblioTextField textField) {
        super(textField);
    }

    @Override
    public boolean isVisible() {
        AccessorGuiBiblioTextField accessor = (AccessorGuiBiblioTextField) controlObject;
        return accessor.isEnabled() && accessor.isFocused();
    }

    @Override
    public Point getCursorPos() {
        AccessorGuiBiblioTextField accessor = (AccessorGuiBiblioTextField) controlObject;
        
        // Get text field position
        int x = accessor.getXPos();
        int y = accessor.getYPos();
        int width = accessor.getWidth();
        int height = accessor.getHeight();
        
        // Get text and cursor position
        String text = controlObject.getText();
        int cursorPosition = accessor.getCursorPosition();
        
        // Use the helper method from AbstractControl
        return getCursorPos(
            Minecraft.getMinecraft().fontRenderer,
            text,
            x, y, width, height,
            0, // lineScrollOffset (not exposed in GuiBiblioTextField)
            cursorPosition,
            cursorPosition, // selectionEnd
            true // enableBackgroundDrawing
        );
    }
}

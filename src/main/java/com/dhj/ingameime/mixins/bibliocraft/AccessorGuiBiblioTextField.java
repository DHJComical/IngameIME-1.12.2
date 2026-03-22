package com.dhj.ingameime.mixins.bibliocraft;

import jds.bibliocraft.gui.GuiBiblioTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GuiBiblioTextField.class, remap = false)
public interface AccessorGuiBiblioTextField {
    @Accessor("xPos")
    int getXPos();

    @Accessor("yPos")
    int getYPos();

    @Accessor("width")
    int getWidth();

    @Accessor("height")
    int getHeight();

    @Accessor("isEnabled")
    boolean isEnabled();

    @Accessor("isFocused")
    boolean isFocused();

    @Accessor("isFocused")
    void setFocused(boolean focused);

    @Accessor("cursorPosition")
    int getCursorPosition();
}

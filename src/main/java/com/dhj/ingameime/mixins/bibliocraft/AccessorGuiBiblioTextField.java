package com.dhj.ingameime.mixins.bibliocraft;

import jds.bibliocraft.gui.GuiBiblioTextField;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GuiBiblioTextField.class, remap = false)
public interface AccessorGuiBiblioTextField {
    @Accessor("lineScrollOffset")
    int getLineScrollOffset();

    @Accessor("fontRenderer")
    FontRenderer getFontRenderer();

    @Accessor("xPos")
    int getXPos();

    @Accessor("yPos")
    int getYPos();

    @Accessor("width")
    int getWidth();

    @Accessor("height")
    int getHeight();
}

package com.dhj.ingameime.mixins.flux;

import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import sonar.fluxnetworks.client.gui.basic.GuiTextField;

@Mixin(value = GuiTextField.class, remap = false)
public interface AccessorGuiTextField {
    @Accessor("lineScrollOffset")
    int getLineScrollOffset();

    @Accessor("fontRenderer")
    FontRenderer getFont();
}

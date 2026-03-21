package com.dhj.ingameime.mixins.vanilla;

import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiTextField.class)
public interface MixinGuiTextFieldAccess {
    @Accessor("yPosition")
    int getYPosition();

    @Accessor("yPosition")
    void setYPosition(int y);
}

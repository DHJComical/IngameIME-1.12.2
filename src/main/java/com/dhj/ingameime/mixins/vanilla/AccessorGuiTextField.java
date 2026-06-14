package com.dhj.ingameime.mixins.vanilla;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiTextField.class)
public interface AccessorGuiTextField {
    @Accessor("lineScrollOffset")
    int getLineScrollOffset();

    @Accessor("field_146211_a")
    FontRenderer getFont();

    @Accessor("isEnabled")
    boolean isEnabled();
}

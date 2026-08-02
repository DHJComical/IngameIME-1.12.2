package com.dhj.ingameime.mixins.journeymap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(targets = "journeymap.client.ui.component.TextBoxButton", remap = false)
public interface AccessorTextBoxButton {
    @Accessor("textBox")
    Object getTextBox();
}

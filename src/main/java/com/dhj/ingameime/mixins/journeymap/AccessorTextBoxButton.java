package com.dhj.ingameime.mixins.journeymap;

import journeymap.client.ui.component.TextBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;

@Pseudo
@Mixin(targets = "journeymap.client.ui.component.buttons.TextBoxButton", remap = false)
public interface AccessorTextBoxButton {
    @Invoker("getTextBox")
    TextBox ingameime$getTextBox();
}

package com.dhj.ingameime.mixins.betterquesting;

import betterquesting.api2.client.gui.controls.PanelTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = PanelTextField.class, remap = false)
public interface AccessorBQUPanelTextField {
    @Accessor("text")
    String getText();

    @Accessor("selectStart")
    int getSelectStart();

    @Accessor("selectEnd")
    int getSelectEnd();

}
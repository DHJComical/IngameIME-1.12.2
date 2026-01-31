package com.dhj.ingameime.mixins.betterquesting;

import betterquesting.api2.client.gui.controls.PanelTextField;
import betterquesting.api2.client.gui.misc.IGuiRect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = PanelTextField.class, remap = false)
public interface AccessorPanelTextField {

    @Accessor("text")
    String getText();

    @Accessor("selectStart")
    int getSelectStart();

    @Accessor("transform")
    IGuiRect getTransform();

    @Invoker("getScrollX")
    int invokeGetScrollX();

    @Invoker("getScrollY")
    int invokeGetScrollY();

    @Invoker("isActive")
    boolean invokeIsActive();
}

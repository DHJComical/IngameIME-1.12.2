package com.dhj.ingameime.mixins.betterquesting;

import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.PanelTextField;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import com.dhj.ingameime.control.BQUTextFieldControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiScreenCanvas.class, remap = false)
public abstract class MixinBQUAutoFocus {

    @Inject(method = "addPanel", at = @At("TAIL"))
    private void onAddPanel(IGuiPanel panel, CallbackInfo ci) {
        if (panel instanceof PanelTextField) {
            PanelTextField<?> textField = (PanelTextField<?>) panel;
            textField.onMouseClick(
                    textField.getTransform().getX() + 1,
                    textField.getTransform().getY() + 1,
                    0
            );
            BQUTextFieldControl.onFocusChange(textField, true);
            org.lwjgl.input.Keyboard.enableRepeatEvents(true);
        }
    }
}
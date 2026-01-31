package com.dhj.ingameime.mixins.betterquesting;

import betterquesting.api2.client.gui.controls.PanelTextField;
import com.dhj.ingameime.IngameIME_Forge;
import com.dhj.ingameime.control.PanelTextFieldControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PanelTextField.class, remap = false)
public abstract class MixinPanelTextField {

    @Shadow
    private boolean isFocused;

    private boolean lastFocusState = false;

    @Inject(method = "onMouseClick", at = @At("RETURN"), remap = false)
    private void onMouseClickPost(int mx, int my, int button, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (this.isFocused != lastFocusState) {
                PanelTextFieldControl.onFocusChange(this, this.isFocused);
                lastFocusState = this.isFocused;
            }
        } catch (Throwable t) {
            IngameIME_Forge.LOG.error("IngameIME failed to handle PanelTextField focus change.", t);
        }
    }
}

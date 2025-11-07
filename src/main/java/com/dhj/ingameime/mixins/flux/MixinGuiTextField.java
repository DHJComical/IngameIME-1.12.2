package com.dhj.ingameime.mixins.flux;

import com.dhj.ingameime.IngameIME_Forge;
import com.dhj.ingameime.control.FluxTextFieldControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sonar.fluxnetworks.client.gui.basic.GuiTextField;

@Mixin(value = GuiTextField.class, remap = false)
public class MixinGuiTextField {
    @Inject(method = "setFocused(Z)V", at = @At("TAIL"))
    private void onSetFocus(boolean isFocusedIn, CallbackInfo ci) {
        GuiTextField self = (GuiTextField) (Object) this;

        try {
            FluxTextFieldControl.onFocusChange(self, isFocusedIn);
        } catch (Throwable t) {
            IngameIME_Forge.LOG.error("IngameIME failed to handle focus change. This is a compatibility issue but the game was prevented from crashing.", t);
            System.err.println("IngameIME caught an error during focus change, preventing a crash: " + t.getMessage());
        }
    }
}

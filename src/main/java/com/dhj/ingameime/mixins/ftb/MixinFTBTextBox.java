package com.dhj.ingameime.mixins.ftb;

import com.dhj.ingameime.IngameIME_Forge;
import com.dhj.ingameime.Internal;
import com.dhj.ingameime.control.FTBTextFieldControl;
import com.feed_the_beast.ftblib.lib.gui.TextBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TextBox.class, remap = false)
public class MixinFTBTextBox {

    @Inject(method = "setFocused(Z)V", at = @At("TAIL"))
    private void onSetFocus(boolean isFocusedIn, CallbackInfo ci) {
        TextBox self = (TextBox) (Object) this;
        try {
            FTBTextFieldControl.onFocusChange(self, isFocusedIn);
        } catch (Throwable t) {
            IngameIME_Forge.LOG.error("IngameIME failed to handle FTB focus change.", t);
        }
    }

}
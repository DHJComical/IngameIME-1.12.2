package com.dhj.ingameime.mixins.bibliocraft;

import com.dhj.ingameime.IngameIME_Forge;
import com.dhj.ingameime.control.BibliocraftTextFieldControl;
import jds.bibliocraft.gui.GuiBiblioTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiBiblioTextField.class, remap = false)
public abstract class MixinGuiBiblioTextField {
    @Inject(method = "setFocused(Z)V", at = @At("HEAD"))
    private void onSetFocus(boolean par1, CallbackInfo ci) {
        GuiBiblioTextField self = (GuiBiblioTextField) (Object) this;

        try {
            BibliocraftTextFieldControl.onFocusChange(self, par1);
        } catch (Throwable t) {
            IngameIME_Forge.LOG.error("IngameIME failed to handle Bibliocraft text field focus change. This is a compatibility issue but the game was prevented from crashing.", t);
            System.err.println("IngameIME caught an error during Bibliocraft focus change: " + t.getMessage());
        }
    }
}

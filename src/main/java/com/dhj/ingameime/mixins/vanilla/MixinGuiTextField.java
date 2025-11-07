package com.dhj.ingameime.mixins.vanilla;

import com.dhj.ingameime.IngameIME_Forge;
import com.dhj.ingameime.control.JEITextFieldControl;
import com.dhj.ingameime.control.VanillaTextFieldControl;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.common.Loader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiTextField.class)
public abstract class MixinGuiTextField {
    @Inject(method = "setFocused(Z)V", at = @At("HEAD"))
    private void onSetFocus(boolean isFocusedIn, CallbackInfo ci) {
        GuiTextField self = (GuiTextField) (Object) this;
        boolean wasFocused = self.isFocused();
        if (wasFocused == isFocusedIn) return;

        try {
            if (isFocusedIn) {
                if (!Loader.isModLoaded(JEITextFieldControl.JEI_MOD_ID) || !JEITextFieldControl.onFocus(self)) {
                    VanillaTextFieldControl.onFocus(self);
                }
            } else {
                VanillaTextFieldControl.onLoseFocus(self);
            }
        } catch (Throwable t) {
            IngameIME_Forge.LOG.error("IngameIME failed to handle focus change. This is a compatibility issue but the game was prevented from crashing.", t);
            System.err.println("IngameIME caught an error during focus change, preventing a crash: " + t.getMessage());
        }
    }
}
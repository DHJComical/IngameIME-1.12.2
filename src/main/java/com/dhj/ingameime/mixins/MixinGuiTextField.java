package com.dhj.ingameime.mixins;

import com.dhj.ingameime.IMStates;
import com.dhj.ingameime.Internal;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiTextField.class)
public abstract class MixinGuiTextField extends Gui {

     private static final Logger LOGGER = LogManager.getLogger("IngameIME");


    @Inject(method = "setFocused(Z)V", at = @At("TAIL"))
    private void onSetFocus(boolean isFocusedIn, CallbackInfo ci) {
        GuiTextField self = (GuiTextField) (Object) this;

        try {
            if (isFocusedIn) {
                IMStates.ActiveControl = self;
                Internal.setActivated(true);
            } else {
                if (IMStates.ActiveControl == self) {
                    IMStates.ActiveControl = null;
                    Internal.setActivated(false);
                }
            }
        } catch (Throwable t) {
            LOGGER.error("IngameIME failed to handle focus change. This is a compatibility issue but the game was prevented from crashing.", t);
            System.err.println("IngameIME caught an error during focus change, preventing a crash: " + t.getMessage());
        }
    }
}
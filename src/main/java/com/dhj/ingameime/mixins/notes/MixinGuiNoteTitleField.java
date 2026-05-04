package com.dhj.ingameime.mixins.notes;

import com.chaosthedude.notes.gui.GuiNoteTitleField;
import com.dhj.ingameime.IngameIME_Forge;
import com.dhj.ingameime.control.NoteTitleFieldControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiNoteTitleField.class, remap = false)
public abstract class MixinGuiNoteTitleField {
    @Inject(method = "setFocused(Z)V", at = @At("TAIL"))
    private void onSetFocus(boolean focused, CallbackInfo ci) {
        GuiNoteTitleField self = (GuiNoteTitleField) (Object) this;

        try {
            NoteTitleFieldControl.onFocusChange(self, focused);
        } catch (Throwable t) {
            IngameIME_Forge.LOG.error("IngameIME failed to handle Notes title field focus change. This is a compatibility issue but the game was prevented from crashing.", t);
            System.err.println("IngameIME caught an error during Notes title field focus change: " + t.getMessage());
        }
    }
}

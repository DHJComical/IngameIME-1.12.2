package com.dhj.ingameime.mixins.notes;

import com.chaosthedude.notes.gui.GuiNoteTextField;
import com.dhj.ingameime.IngameIME_Forge;
import com.dhj.ingameime.control.NoteTextFieldControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiNoteTextField.class, remap = false)
public abstract class MixinGuiNoteTextField {
    @Inject(method = "setFocused(Z)V", at = @At("TAIL"))
    private void onSetFocus(boolean focused, CallbackInfo ci) {
        GuiNoteTextField self = (GuiNoteTextField) (Object) this;

        try {
            NoteTextFieldControl.onFocusChange(self, focused);
        } catch (Throwable t) {
            IngameIME_Forge.LOG.error("IngameIME failed to handle Notes text field focus change. This is a compatibility issue but the game was prevented from crashing.", t);
            System.err.println("IngameIME caught an error during Notes text field focus change: " + t.getMessage());
        }
    }
}

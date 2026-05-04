package com.dhj.ingameime.mixins.notes;

import com.chaosthedude.notes.gui.GuiNoteTitleField;
import com.dhj.ingameime.ClientProxy;
import com.dhj.ingameime.IngameIME_Forge;
import com.dhj.ingameime.control.NoteTitleFieldControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiNoteTitleField.class, remap = false)
public abstract class MixinGuiNoteTitleField {

    @Shadow
    private boolean isFocused;

    @Unique
    private boolean ingameime$lastFocusState;

    @Inject(method = "setFocused", at = @At("RETURN"), remap = false)
    private void ingameime$onSetFocused(boolean focused, CallbackInfo ci) {
        try {
            if (this.isFocused != this.ingameime$lastFocusState) {
                ClientProxy.INSTANCE.onControlFocus(new NoteTitleFieldControl((GuiNoteTitleField) (Object) this), this.isFocused, false);
                this.ingameime$lastFocusState = this.isFocused;
            }
        } catch (Throwable t) {
            IngameIME_Forge.LOG.error("IngameIME failed to handle GuiNoteTitleField focus change.", t);
        }
    }
}

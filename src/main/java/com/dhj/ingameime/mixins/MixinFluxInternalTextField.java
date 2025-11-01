package com.dhj.ingameime.mixins;

import com.dhj.ingameime.ClientProxy;
import com.dhj.ingameime.IMStates;
import com.dhj.ingameime.IngameIME_Forge;
import com.dhj.ingameime.Internal;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = sonar.fluxnetworks.client.gui.basic.GuiTextField.class, remap = false)
public abstract class MixinFluxInternalTextField {

    @Shadow public int x;
    @Shadow public int y;
    @Shadow public FontRenderer fontRenderer;
    @Shadow private String text;
    @Shadow private int lineScrollOffset;
    @Shadow public abstract int getCursorPosition();
    @Shadow public abstract int getWidth();

    @Inject(method = "setFocused(Z)V", at = @At("TAIL"))
    private void onSetFocus(boolean isFocusedIn, CallbackInfo ci) {
        Object self = this;
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
            IngameIME_Forge.LOG.error("IngameIME caught an error during Flux Internal TextField focus change: {}", t.getMessage());
        }
    }

    @Inject(method = "drawTextBox()V", at = @At("HEAD"))
    private void onDrawTextBox(CallbackInfo ci) {
        Object self = this;
        if (IMStates.ActiveControl == self) {

            String textBeforeCursor = this.text.substring(this.lineScrollOffset, this.getCursorPosition());
            String trimmedText = this.fontRenderer.trimStringToWidth(textBeforeCursor, this.getWidth());
            int cursorX = this.x + 4 + this.fontRenderer.getStringWidth(trimmedText);

            int cursorY = this.y + this.fontRenderer.FONT_HEIGHT + 2;
            ClientProxy.Screen.setCaretPos(cursorX, cursorY);
        }
    }
}
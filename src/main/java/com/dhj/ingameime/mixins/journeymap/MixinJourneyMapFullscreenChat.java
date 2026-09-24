package com.dhj.ingameime.mixins.journeymap;

import com.dhj.ingameime.ClientProxy;
import com.dhj.ingameime.IMStates;
import com.dhj.ingameime.control.VanillaTextFieldControl;
import com.dhj.ingameime.mixins.vanilla.AccessorGuiChat;
import journeymap.client.ui.fullscreen.MapChat;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "journeymap.client.ui.fullscreen.Fullscreen", remap = false)
public abstract class MixinJourneyMapFullscreenChat {
    @Inject(method = "openChat(Ljava/lang/String;)V", at = @At("TAIL"))
    private void ingameime$registerMapChatInput(String initialText, CallbackInfo ci) {
        if (ClientProxy.INSTANCE == null) {
            return;
        }

        MapChat chat = ((AccessorJourneyMapFullscreen) this).ingameime$getChat();
        if (chat == null || chat.isHidden()) {
            return;
        }

        Keyboard.enableRepeatEvents(true);

        GuiTextField inputField = ((AccessorGuiChat) chat).getInputField();
        if (inputField == null) {
            return;
        }

        if (!inputField.isFocused()) {
            inputField.setFocused(true);
        }
        if (ClientProxy.getIMEventHandler() != IMStates.OpenedAuto
            || !IMStates.isControlObject(inputField, false)) {
            VanillaTextFieldControl.onFocusChange(inputField, true);
        }
    }
}

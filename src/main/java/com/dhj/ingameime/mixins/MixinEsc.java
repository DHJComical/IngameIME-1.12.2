package com.dhj.ingameime.mixins;

import com.dhj.ingameime.Internal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(GuiScreen.class)
public abstract class MixinEsc {

    @Shadow
    protected abstract void actionPerformed(GuiButton button) throws IOException;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Inject(method = "handleKeyboardInput", at = @At("HEAD"), cancellable = true)
    private void onHandleKeyboardInput(CallbackInfo ci) {
        if (Keyboard.getEventKeyState()) {
            if (Keyboard.getEventKey() == Keyboard.KEY_ESCAPE && Internal.getActivated()) {

                Internal.setActivated(false);

                final GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
                if (currentScreen != null) {
                    Minecraft.getMinecraft().addScheduledTask(() -> {
                        try {
                            GuiButton cancelButton = new GuiButton(1, 0, 0, "");
                            this.actionPerformed(cancelButton);
                        } catch (IOException ignored) {

                        }
                    });
                }

                ci.cancel();
            }
        }
    }
}
package com.dhj.ingameime.mixins.vanilla;

import com.dhj.ingameime.ClientProxy;
import com.dhj.ingameime.Internal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Unique
    private static boolean imeActivatedBeforeFullscreen;

    @Inject(method = "toggleFullscreen", at = @At(value = "HEAD"))
    private void preToggleFullscreen(CallbackInfo ci) {
        imeActivatedBeforeFullscreen = Internal.getActivated();
        Internal.destroyInputCtx();
    }

    @Inject(method = "toggleFullscreen", at = @At(value = "RETURN"))
    private void postToggleFullscreen(CallbackInfo ci) {
        Internal.createInputCtx();
        Internal.setActivated(imeActivatedBeforeFullscreen);
    }

    @Inject(method = "displayGuiScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;onGuiClosed()V"))
    private void onGuiScreenClosed(GuiScreen screen, CallbackInfo ci) {
        if (ClientProxy.INSTANCE != null) ClientProxy.INSTANCE.onScreenClose();
    }

    @Inject(method = "displayGuiScreen", at = @At(value = "RETURN"))
    private void onGuiScreenDisplayed(GuiScreen screen, CallbackInfo ci) {
        if (ClientProxy.INSTANCE != null && screen != null)
            ClientProxy.INSTANCE.onScreenOpen(screen);
    }
}
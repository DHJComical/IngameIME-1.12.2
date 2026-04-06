package com.dhj.ingameime.mixins.vanilla;

import com.dhj.ingameime.ClientProxy;
import com.dhj.ingameime.IngameIME_Forge;
import com.dhj.ingameime.Internal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiEditSign;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Shadow
    public GuiScreen currentScreen;

    @Unique
    private static boolean ingameIME$imeActivatedBeforeFullscreen;

    @Inject(method = "toggleFullscreen", at = @At(value = "HEAD"))
    private void preToggleFullscreen(CallbackInfo ci) {
        ingameIME$imeActivatedBeforeFullscreen = Internal.getActivated();
        Internal.destroyInputCtx();
    }

    @Inject(method = "toggleFullscreen", at = @At(value = "RETURN"))
    private void postToggleFullscreen(CallbackInfo ci) {
        Internal.createInputCtx();
        Internal.setActivated(ingameIME$imeActivatedBeforeFullscreen);
    }

    @Inject(method = "displayGuiScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;onGuiClosed()V"))
    private void onGuiScreenClosed(GuiScreen screen, CallbackInfo ci) {
        if (ingameime$shouldForceAlphaOnClose(currentScreen)) {
            IngameIME_Forge.logDebugInfo(
                "Forcing alpha mode before closing sign-like screen: {}",
                currentScreen.getClass().getName());
            Internal.forceAlphaMode();
        }

        if (ClientProxy.INSTANCE != null) ClientProxy.INSTANCE.onScreenClose();
    }

    @Inject(method = "displayGuiScreen", at = @At(value = "RETURN"))
    private void onGuiScreenDisplayed(GuiScreen screen, CallbackInfo ci) {
        if (ClientProxy.INSTANCE != null && screen != null)
            ClientProxy.INSTANCE.onScreenOpen(screen);
    }

    @Unique
    private static boolean ingameime$shouldForceAlphaOnClose(GuiScreen closingScreen) {
        if (closingScreen == null) {
            return false;
        }

        if (closingScreen instanceof GuiEditSign) {
            return true;
        }

        String className = closingScreen.getClass()
            .getName()
            .toLowerCase(Locale.ROOT);
        return className.contains("sign") && (className.contains("gui") || className.contains("screen"));
    }
}

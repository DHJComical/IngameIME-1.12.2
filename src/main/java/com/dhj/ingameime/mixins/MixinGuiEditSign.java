package com.dhj.ingameime.mixins;

import com.dhj.ingameime.Internal;
import net.minecraft.client.gui.inventory.GuiEditSign;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(GuiEditSign.class)
public class MixinGuiEditSign {

    @Inject(method = "initGui", at = @At("RETURN"))
    private void onInitGui(CallbackInfo ci) {
        Internal.setActivated(true);
    }

    @Inject(method = "onGuiClosed", at = @At("HEAD"))
    private void onGuiClosed(CallbackInfo ci) {
        Internal.setActivated(false);
    }
}
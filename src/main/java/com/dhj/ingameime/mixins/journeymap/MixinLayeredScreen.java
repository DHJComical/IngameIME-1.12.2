package com.dhj.ingameime.mixins.journeymap;

import com.dhj.ingameime.ClientProxy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "journeymap.api.v2.client.ui.component.LayeredScreen", remap = false)
public abstract class MixinLayeredScreen {
    @Inject(method = "popLayer()V", at = @At("HEAD"))
    private void ingameime$onLayerClosed(CallbackInfo ci) {
        if (ClientProxy.INSTANCE != null) {
            ClientProxy.INSTANCE.onScreenClose();
        }
    }
}

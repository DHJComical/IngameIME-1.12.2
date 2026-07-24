package com.dhj.ingameime.mixins.vanilla;

import com.dhj.ingameime.Internal;
import com.dhj.ingameime.LinuxKeyEventConverter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "org.lwjgl.opengl.LinuxKeyboard", remap = false)
public abstract class MixinLinuxKeyboard {
    @Invoker("mapEventToKeySym")
    protected abstract long ingameime$mapEventToKeySym(long eventAddress, int state);

    @Inject(method = "handleKeyEvent", at = @At("HEAD"), cancellable = true)
    private void ingameime$processKeyEvent(
        long eventAddress,
        long millis,
        int eventType,
        int x11Keycode,
        int state,
        CallbackInfo ci) {
        long keyval = ingameime$mapEventToKeySym(eventAddress, state);
        boolean release = LinuxKeyEventConverter.isRelease(eventType);
        boolean handled = Internal.processLinuxKeyEvent(keyval, x11Keycode, state, release);
        if (LinuxKeyEventConverter.shouldCancelLwjglHandler(release, handled)) {
            ci.cancel();
        }
    }
}

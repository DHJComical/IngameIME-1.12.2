package com.dhj.ingameime.mixins.betterquesting;

import betterquesting.api2.client.gui.controls.PanelTextField;
import com.dhj.ingameime.control.BQUTextFieldControl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PanelTextField.class, remap = false)
public abstract class MixinBQUPanelTextField {

    @Shadow private boolean isFocused;

    @Inject(method = "onMouseClick", at = @At("RETURN"))
    private void onPostClick(int mx, int my, int button, CallbackInfoReturnable<Boolean> cir) {
        PanelTextField<?> self = (PanelTextField<?>) (Object) this;
        BQUTextFieldControl.onFocusChange(self, this.isFocused);
    }

    @Inject(method = "initPanel", at = @At("TAIL"))
    private void onInitAuto(CallbackInfo ci) {
        PanelTextField<?> self = (PanelTextField<?>) (Object) this;
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen != null && screen.getClass().getName().contains("GuiQuestSearch")) {
            this.isFocused = true;
            BQUTextFieldControl.onFocusChange(self, true);
        }
    }

}
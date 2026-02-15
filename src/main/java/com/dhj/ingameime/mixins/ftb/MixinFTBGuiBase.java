package com.dhj.ingameime.mixins.ftb;

import com.feed_the_beast.ftblib.lib.gui.GuiBase;
import com.feed_the_beast.ftblib.lib.gui.TextBox;
import com.feed_the_beast.ftblib.lib.gui.Widget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.input.Keyboard.enableRepeatEvents;

@Mixin(value = GuiBase.class, remap = false)
public abstract class MixinFTBGuiBase {

    @Inject(method = "onPostInit", at = @At("RETURN"))
    private void autoFocusTextBox(CallbackInfo ci) {
        GuiBase self = (GuiBase) (Object) this;

        for (Widget widget : self.widgets) {
            if (widget instanceof TextBox) {
                ((TextBox) widget).setFocused(true);
                enableRepeatEvents(true);
                break;
            }
        }
    }
}
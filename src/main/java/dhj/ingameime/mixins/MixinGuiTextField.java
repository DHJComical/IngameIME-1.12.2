package dhj.ingameime.mixins;

import dhj.ingameime.IMStates;
import dhj.ingameime.Internal;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiTextField.class)
public abstract class MixinGuiTextField extends Gui {

    @Shadow
    private boolean isFocused;

    @Inject(method = "setFocused(Z)V", at = @At("TAIL"))
    private void onSetFocus(boolean isFocusedIn, CallbackInfo ci) {
        GuiTextField self = (GuiTextField) (Object) this;

        if (isFocusedIn) {
            IMStates.ActiveControl = self;
            Internal.setActivated(true);
        } else {
            if (IMStates.ActiveControl == self) {
                IMStates.ActiveControl = null;
                Internal.setActivated(false);
            }
        }
    }

}
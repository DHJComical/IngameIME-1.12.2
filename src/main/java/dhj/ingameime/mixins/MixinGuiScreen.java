package dhj.ingameime.mixins;

import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiScreen.class)
public interface MixinGuiScreen {
    @Invoker()
    void callKeyTyped(char typedChar, int keyCode);
}

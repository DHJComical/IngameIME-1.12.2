package com.dhj.ingameime.mixins.vanilla;

import net.minecraft.client.gui.GuiSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiSlot.class)
public interface MixinGuiSlot {
    @Invoker("func_148132_a")
    void invokeHandleMouseInput(int mouseX, int mouseY);
}

package com.dhj.ingameime.mixins;

import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.io.IOException;

@Mixin(GuiScreen.class)
public interface MixinGuiScreen {
    @Invoker("keyTyped")
    void callKeyTyped(char typedChar, int keyCode) throws IOException;
}
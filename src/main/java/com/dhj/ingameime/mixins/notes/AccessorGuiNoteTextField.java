package com.dhj.ingameime.mixins.notes;

import com.chaosthedude.notes.gui.GuiNoteTextField;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = GuiNoteTextField.class, remap = false)
public interface AccessorGuiNoteTextField {
    @Accessor("fontRenderer")
    FontRenderer getFontRenderer();

    @Accessor("margin")
    int getMargin();

    @Accessor("isFocused")
    boolean isFocused();

    @Accessor("isEnabled")
    boolean isEnabled();

    @Accessor("visible")
    boolean isVisibleField();

    @Invoker("getCursorX")
    int invokeGetCursorX();

    @Invoker("getCursorY")
    int invokeGetCursorY();
}

package com.dhj.ingameime.mixins.notes;

import com.chaosthedude.notes.gui.GuiNoteTitleField;
import net.minecraft.client.gui.FontRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GuiNoteTitleField.class, remap = false)
public interface AccessorGuiNoteTitleField {
    @Accessor("fontRenderer")
    FontRenderer getFontRenderer();

    @Accessor("lineScrollOffset")
    int getLineScrollOffset();

    @Accessor("isEnabled")
    boolean isEnabled();
}

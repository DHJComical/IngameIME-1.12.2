package com.dhj.ingameime.mixins.ftb;

import com.feed_the_beast.ftblib.lib.gui.TextBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TextBox.class, remap = false)
public interface AccessorFTBTextBox {
    @Accessor("text")
    String getText();

    @Accessor("cursorPosition")
    int getCursorPosition();

    @Accessor("selectionEnd")
    int getSelectionEnd();

    @Accessor("lineScrollOffset")
    int getLineScrollOffset();
}
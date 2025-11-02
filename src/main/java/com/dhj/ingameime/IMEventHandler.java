package com.dhj.ingameime;

import com.dhj.ingameime.control.IControl;
import net.minecraft.client.gui.GuiScreen;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IMEventHandler {
    IMStates onScreenClose();

    IMStates onControlFocus(@Nonnull IControl control, boolean focused);

    IMStates onScreenOpen(@Nullable GuiScreen screen);

    IMStates onToggleKey();

    IMStates onMouseMove();
}

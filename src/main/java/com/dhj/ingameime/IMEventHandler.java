package com.dhj.ingameime;

import com.dhj.ingameime.control.IControl;
import net.minecraft.client.gui.GuiScreen;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IMEventHandler {
    /**
     * Called before the new screen is displayed.
     */
    IMStates onScreenClose();

    /**
     * Called after the new screen is displayed.
     * @param screen New screen
     * @return New IMStates to be set
     */
    IMStates onScreenOpen(@Nullable GuiScreen screen);

    /**
     * Called when focus change.
     * @param control The caller. Can be anything so need to be checked
     * @param focused Focus or lose focus
     * @param isOverlay Whether OverlayControl or not
     * @return New IMStates to be set
     */
    IMStates onControlFocus(@Nonnull IControl control, boolean focused, boolean isOverlay);

    /**
     * Called when the manual toggle key is pressed.
     * @return New IMStates to be set
     */
    IMStates onToggleKey();

    /**
     * Called when the mouse is moved.
     * @return New IMStates to be set
     */
    IMStates onMouseMove();
}

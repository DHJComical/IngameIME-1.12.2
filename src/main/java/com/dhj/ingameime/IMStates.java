package com.dhj.ingameime;

import com.dhj.ingameime.control.IControl;
import com.dhj.ingameime.control.NoControl;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiEditSign;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.dhj.ingameime.IngameIME_Forge.LOG;

public enum IMStates implements IMEventHandler {
    /**
     * IME is disabled.
     */
    Disabled {
        @Override
        public IMStates onScreenOpen(@Nullable GuiScreen screen) {
            if (screen instanceof GuiEditSign) {
                Internal.setActivated(true); // What's wrong with you ojng
                return OpenedInternal;
            }
            return this;
        }

        @Override
        public IMStates onControlFocus(@Nonnull IControl control, boolean focused, boolean isOverlay) {
            if (focused) {
                setControl(control, isOverlay);
                LOG.info("Opened by control focus: {}", control.getClass().getSimpleName());
                Internal.setActivated(true);
                return OpenedAuto;
            } else {
                return this;
            }
        }

        @Override
        public IMStates onToggleKey() {
            LOG.info("Turned on by toggle key");
            Internal.setActivated(true);
            return OpenedManual;
        }
    },
    /**
     * IME is opened manually by internal code.
     */
    OpenedInternal {
        @Override
        public void onLeaveState() {
            NoControl.NO_CONTROL.setCursorX(0);
            NoControl.NO_CONTROL.setCursorY(0);
        }
    },
    /**
     * IME is opened manually by the user. Will turn off when mouse move.
     */
    OpenedManual {
        @Override
        public IMStates onMouseMove() {
            if (!Config.TurnOffOnMouseMove.getBoolean()) return this;
            Internal.setActivated(false);
            LOG.info("Turned off by mouse move");
            return Disabled;
        }
    },
    /**
     * IME is opened automatically by internal code.
     */
    OpenedAuto {
        @Override
        public IMStates onControlFocus(@Nonnull IControl control, boolean focused, boolean isOverlay) {
            // Handle active control lose focus
            Object object = control.getControlObject();
            boolean changed = !isControlObject(object, isOverlay);
            if (!focused) {
                if (!changed) {
                    Internal.setActivated(false);
                    setControl(NoControl.NO_CONTROL, isOverlay);
                    if (IMStates.getActiveControl() != NoControl.NO_CONTROL) {
                        Internal.setActivated(true);
                        LOG.info("Focus changed from Overlay {} to Common {}", control.getClass().getSimpleName(), IMStates.getActiveControl().getClass().getSimpleName());
                        return this;
                    }
                    LOG.info("Turned off by losing control focus: {}", control.getClass().getSimpleName());
                    return Disabled;
                }
                return this;
            }

            // Update active focused control
            if (changed) Internal.setActivated(false); // Simply empty the typing list
            setControl(control, isOverlay);
            if (changed) LOG.info("Opened by control focus: {}", control.getClass().getSimpleName());
            Internal.setActivated(true);
            ClientProxy.Screen.WInputMode.setActive(true);
            return this;
        }
    };

    @Override
    public IMStates onControlFocus(@Nonnull IControl control, boolean focused, boolean isOverlay) {
        // Update control but do not change status
        if (focused) {
            setControl(control, isOverlay);
        } else if (isControlObject(control.getControlObject(), isOverlay)) {
            setControl(NoControl.NO_CONTROL, isOverlay);
        }
        return this;
    }

    @Override
    public IMStates onScreenClose() {
        Internal.setActivated(false);
        // Empty controls
        setControl(NoControl.NO_CONTROL, false);
        setControl(NoControl.NO_CONTROL, true);
        return Disabled;
    }

    @Override
    public IMStates onScreenOpen(GuiScreen screen) {
        return this;
    }

    @Override
    public IMStates onMouseMove() {
        return this;
    }

    @Override
    public IMStates onToggleKey() {
        LOG.info("Turned off by toggle key");
        Internal.setActivated(false);
        return Disabled;
    }

    @Nonnull
    private static IControl CommonControl = NoControl.NO_CONTROL;
    @Nonnull
    private static IControl OverlayControl = NoControl.NO_CONTROL;

    public static void setControl(@Nonnull IControl control, boolean isOverlay) {
        if (isOverlay) {
            OverlayControl = control;
        } else {
            CommonControl = control;
        }
    }

    public static boolean isControlObject(Object controlObject, boolean isOverlay) {
        return isOverlay ? IMStates.OverlayControl.getControlObject() == controlObject :
                IMStates.CommonControl.getControlObject() == controlObject;
    }

    /**
     * @return The control to be rendered and call typed etc.
     */
    public static @Nonnull IControl getActiveControl() {
        IMEventHandler eventHandler = ClientProxy.getIMEventHandler();
        return eventHandler == IMStates.OpenedManual || eventHandler == IMStates.OpenedInternal ? NoControl.NO_CONTROL :
                (OverlayControl == NoControl.NO_CONTROL ? CommonControl : OverlayControl);
    }
}

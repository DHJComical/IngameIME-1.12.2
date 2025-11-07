package com.dhj.ingameime;

import com.dhj.ingameime.control.IControl;
import com.dhj.ingameime.control.NoControl;
import net.minecraft.client.gui.GuiScreen;

import javax.annotation.Nonnull;

import static com.dhj.ingameime.IngameIME_Forge.LOG;

public enum IMStates implements IMEventHandler {
    Disabled {
        @Override
        public IMStates onControlFocus(@Nonnull IControl control, boolean focused, boolean isOverlay) {
            if (focused) {
                setControl(control, isOverlay);
                LOG.info("Opened by control focus: {}", control.getClass().getSimpleName());
                Internal.setActivated(true);
                return OpenedAuto;
            } else {
                return this; // Do nothing
            }
        }

        @Override
        public IMStates onToggleKey() {
            LOG.info("Turned on by toggle key");
            Internal.setActivated(true);
            return OpenedManual;
        }

    }, OpenedManual {
        @Override
        public IMStates onControlFocus(@Nonnull IControl control, boolean focused, boolean isOverlay) {
            // Ignore all focus event
            return this;
        }

        @Override
        public IMStates onMouseMove() {
            if (!Config.TurnOffOnMouseMove.getBoolean()) return this;
            LOG.info("Turned off by mouse move");
            Internal.setActivated(false);
            return Disabled;
        }
    }, OpenedAuto {
        @Override
        public IMStates onControlFocus(@Nonnull IControl control, boolean focused, boolean isOverlay) {
            // Handle active control lose focus
            Object object = control.getControlObject();
            boolean changed = !(isOverlay ? isOverlayControlObject(object) : isCommonControlObject(object));
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
            if (changed) Internal.setActivated(false); // Empty the typing list when changed
            setControl(control, isOverlay);
            if (changed) LOG.info("Opened by control focus: {}", control.getClass().getSimpleName());
            Internal.setActivated(true);
            ClientProxy.Screen.WInputMode.setActive(true);
            return this; // Still OpenAuto
        }
    };

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

    public static boolean isOverlayControlObject(Object controlObject) {
        return IMStates.OverlayControl.getControlObject() == controlObject;
    }

    public static boolean isCommonControlObject(Object controlObject) {
        return IMStates.CommonControl.getControlObject() == controlObject;
    }

    public static @Nonnull IControl getActiveControl() {
        return OverlayControl == NoControl.NO_CONTROL ? CommonControl : OverlayControl;
    }
}

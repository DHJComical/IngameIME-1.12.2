package com.dhj.ingameime;

import com.dhj.ingameime.control.IControl;
import com.dhj.ingameime.control.NoControl;
import net.minecraft.client.gui.GuiScreen;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.dhj.ingameime.IngameIME_Forge.LOG;

public enum IMStates implements IMEventHandler {
    Disabled {
        @Override
        public IMStates onControlFocus(@Nonnull IControl control, boolean focused) {
            if (focused) {
                ActiveControl = control;
                LOG.info("Opened by control focus: {}", ActiveControl.getClass());
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

    }, OpenedManual {
        @Override
        public IMStates onControlFocus(@Nonnull IControl control, boolean focused) {
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
        public IMStates onControlFocus(@Nonnull IControl control, boolean focused) {
            // Ignore not active focus one
            if (!focused && control != ActiveControl) return this;

            if (!focused) {
                LOG.info("Turned off by losing control focus: {}", ActiveControl.getClass());
                Internal.setActivated(false);
                return Disabled;
            }

            // Update active focused control
            if (ActiveControl != control) {
                ActiveControl = control;
                LOG.info("Opened by control focus: {}", ActiveControl.getClass());
                Internal.setActivated(true);
                ClientProxy.Screen.WInputMode.setActive(true);
            }
            return this;
        }
    };

    @Nullable
    public static GuiScreen ActiveScreen = null;
    @Nullable
    private static IControl ActiveControl = null;
    @Nonnull
    private static IControl CommonControl = NoControl.NO_CONTROL;
    @Nonnull
    private static IControl OverlayControl = NoControl.NO_CONTROL;

    public static void setOverlayControl(@Nonnull IControl control) {
        if (OverlayControl.getControlObject() != control.getControlObject()) {
            Internal.setActivated(false);
        }
        OverlayControl = control;
        LOG.info("Overlay control set to {}", control.getClass().getSimpleName());
        Internal.setActivated(getActiveControl() != NoControl.NO_CONTROL);
    }

    public static void setCommonControl(@Nonnull IControl control) {
        if (OverlayControl != NoControl.NO_CONTROL) {
            CommonControl = control;
            return;
        }
        if (CommonControl.getControlObject() != control.getControlObject()) {
            Internal.setActivated(false);
        }
        CommonControl = control;
        LOG.info("Common control set to {}", control.getClass().getSimpleName());
        Internal.setActivated(getActiveControl() != NoControl.NO_CONTROL);
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

    @Override
    public IMStates onScreenClose() {
        if (ActiveScreen != null) LOG.info("Screen closed: {}", ActiveScreen.getClass());
        Internal.setActivated(false);
        IMStates.setOverlayControl(NoControl.NO_CONTROL);
        IMStates.setCommonControl(NoControl.NO_CONTROL);
        ActiveScreen = null;
        return Disabled;
    }

    @Override
    public IMStates onScreenOpen(GuiScreen screen) {
        if (ActiveScreen == screen) return this;
        ActiveScreen = screen;
        if (ActiveScreen != null) LOG.info("Screen Opened: {}", ActiveScreen.getClass());
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
}

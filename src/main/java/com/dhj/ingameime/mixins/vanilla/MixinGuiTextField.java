package com.dhj.ingameime.mixins.vanilla;

import com.dhj.ingameime.IngameIME_Forge;
import com.dhj.ingameime.Internal;
import com.dhj.ingameime.control.DirectTextFieldControl;
import com.dhj.ingameime.control.JEITextFieldControl;
import com.dhj.ingameime.control.VanillaTextFieldControl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.common.Loader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiTextField.class)
public abstract class MixinGuiTextField {
    @Unique
    private int ingameime$lastCursorPosition = -1;
    @Unique
    private boolean ingameime$lastLeadingSlash = false;

    @Inject(method = "setFocused(Z)V", at = @At("HEAD"))
    private void onSetFocus(boolean isFocusedIn, CallbackInfo ci) {
        GuiTextField self = (GuiTextField) (Object) this;

        try {
            GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
            if (currentScreen != null) {
                String screenClassName = currentScreen.getClass().getName();
                if (screenClassName.equals("journeymap.client.ui.fullscreen.Fullscreen")) {
                    if (!ingameime$isJourneyMapSearchField(self, currentScreen)) {
                        return;
                    }
                }
            }

            if (Loader.isModLoaded(JEITextFieldControl.JEI_MOD_ID)
                && JEITextFieldControl.onFocusChange(self, isFocusedIn)) {
                return;
            }
            if (currentScreen != null && currentScreen.getClass().getName().startsWith("hunternif.mc.atlas")) {
                DirectTextFieldControl.onFocusChange(self, isFocusedIn);
                return;
            }
            VanillaTextFieldControl.onFocusChange(self, isFocusedIn);
        } catch (Throwable t) {
            IngameIME_Forge.LOG.error(
                "IngameIME failed to handle focus change. This is a compatibility issue but the game was prevented from crashing.",
                t);
            System.err.println("IngameIME caught an error during focus change, preventing a crash: " + t.getMessage());
        }
    }

    @Inject(method = "setFocused(Z)V", at = @At("TAIL"))
    private void onSetFocusTail(boolean isFocusedIn, CallbackInfo ci) {
        GuiTextField self = (GuiTextField) (Object) this;

        if (!isFocusedIn) {
            ingameime$lastCursorPosition = -1;
            ingameime$lastLeadingSlash = false;
            return;
        }

        ingameime$lastCursorPosition = self.getCursorPosition();
        ingameime$handleChatCommandMode(self);
    }

    @Inject(method = "setCursorPosition(I)V", at = @At("TAIL"))
    private void onCursorPositionChanged(int pos, CallbackInfo ci) {
        GuiTextField self = (GuiTextField) (Object) this;
        if (!self.isFocused()) {
            return;
        }

        int cursor = self.getCursorPosition();
        if (cursor == ingameime$lastCursorPosition) {
            return;
        }

        ingameime$lastCursorPosition = cursor;
        ingameime$handleChatCommandMode(self);
    }

    @Unique
    private void ingameime$handleChatCommandMode(GuiTextField self) {
        if (!ingameime$isChatInputField(self)) {
            ingameime$lastLeadingSlash = false;
            return;
        }

        String text = self.getText();
        boolean leadingSlash = text != null && text.startsWith("/");

        if (leadingSlash) {
            if (self.getCursorPosition() == 1) {
                Internal.forceAlphaMode();
            }
        } else if (ingameime$lastLeadingSlash) {
            Internal.forceNativeMode();
        }

        ingameime$lastLeadingSlash = leadingSlash;
    }

    @Unique
    private boolean ingameime$isChatInputField(GuiTextField self) {
        GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
        if (!(currentScreen instanceof GuiChat)) {
            return false;
        }
        return ((AccessorGuiChat) currentScreen).getInputField() == self;
    }

    @Unique
    private boolean ingameime$isJourneyMapSearchField(GuiTextField field, GuiScreen screen) {
        try {
            java.lang.reflect.Field fieldX = screen.getClass().getDeclaredField("searchTextX");
            fieldX.setAccessible(true);
            Object searchTextX = fieldX.get(screen);
            if (searchTextX != null && searchTextX == field) {
                return true;
            }

            java.lang.reflect.Field fieldZ = screen.getClass().getDeclaredField("searchTextZ");
            fieldZ.setAccessible(true);
            Object searchTextZ = fieldZ.get(screen);
            if (searchTextZ != null && searchTextZ == field) {
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}

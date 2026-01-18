package com.dhj.ingameime.control;

import codechicken.nei.guihook.GuiContainerManager;
import com.dhj.ingameime.mixins.vanilla.AccessorGuiScreen;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;

public abstract class AbstractControl<T> implements IControl {
    protected final T controlObject;

    public AbstractControl(T controlObject) {
        this.controlObject = controlObject;
    }

    @Override
    public T getControlObject() {
        return this.controlObject;
    }

    @Override
    public void writeText(String text) throws IOException {
        writeCurrentScreenText(text);
    }

    /**
     * Universal write method.
     */
    public static void writeCurrentScreenText(String text) throws IOException {
        if (Loader.isModLoaded("NotEnoughItems") && writeCurrentScreenTextNEI(text)) return;
        final GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen != null) {
            for (char c : text.toCharArray()) {
                ((AccessorGuiScreen) screen).callKeyTyped(c, Keyboard.KEY_NONE);
            }
        }
    }

    @Optional.Method(modid = "NotEnoughItems")
    private static boolean writeCurrentScreenTextNEI(String string) {
        if (GuiContainerManager.getManager() != null) {
            for (char c : string.toCharArray()) {
                GuiContainerManager.getManager().keyTyped(c, Keyboard.KEY_NONE);
            }
            return true;
        }
        return false;
    }

    /**
     * Get cursor position like vanilla text field.
     */
    protected static @Nonnull Point getCursorPos(
        @Nonnull FontRenderer font, @Nonnull String text,
        int x, int y, int width, int height,
        int lineScrollOffset, int cursorPosition, int selectionEnd,
        boolean enableBackgroundDrawing
    ) {
        if (font == null) {
            int cursorY = (enableBackgroundDrawing ? y + (height - 8) / 2 : y) - 1;
            int currentDrawX = enableBackgroundDrawing ? x + 4 : x;
            return new Point(currentDrawX - 1, cursorY);
        }

        String visibleText = font.trimStringToWidth(text.substring(lineScrollOffset), width);
        int cursorY = (enableBackgroundDrawing ? y + (height - 8) / 2 : y) - 1;

        int cursorPosRelative = cursorPosition - lineScrollOffset;
        int selectionEndRelative = selectionEnd - lineScrollOffset;
        int currentDrawX = enableBackgroundDrawing ? x + 4 : x;

        if (selectionEndRelative > visibleText.length()) {
            selectionEndRelative = visibleText.length();
        }

        if (!visibleText.isEmpty()) {
            if (selectionEndRelative != cursorPosRelative) {
                // Perform when Ctrl + A
                return new Point(currentDrawX - 1, cursorY);
            }
            String rawTextBeforeCursor = visibleText.substring(0, cursorPosRelative);
            currentDrawX += font.getStringWidth(rawTextBeforeCursor);
        }

        return new Point(currentDrawX - 1, cursorY);
    }

//    /**
//     * Get cursor position like vanilla text field. Will calculate GuiContainer offset.
//     *
//     * @param screen any screens
//     */
//    @SuppressWarnings("SameParameterValue")
//    protected static @Nonnull Point getContainerCursorPos(
//            @Nullable GuiScreen screen, @Nonnull FontRenderer font, @Nonnull String text,
//            int x, int y, int width, int height,
//            int lineScrollOffset, int cursorPosition, int selectionEnd,
//            boolean enableBackgroundDrawing
//    ) {
//        Point position = getCursorPos(font, text, x, y, width, height, lineScrollOffset, cursorPosition, selectionEnd, enableBackgroundDrawing);
//        if (screen instanceof GuiContainer) {
//            GuiContainer container = (GuiContainer) screen;
//            position.x += container.getGuiLeft();
//            position.y += container.getGuiTop();
//        }
//        return position;
//    }
}

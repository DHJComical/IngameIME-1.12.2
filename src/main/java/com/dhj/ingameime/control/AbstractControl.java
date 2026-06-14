package com.dhj.ingameime.control;

import codechicken.nei.guihook.GuiContainerManager;
import com.dhj.ingameime.IngameIME_Forge;
import com.dhj.ingameime.UnicodeTextHelper;
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
        text = UnicodeTextHelper.repairUtf8Mojibake(text);
        if (text == null || text.isEmpty()) {
            return;
        }

        if (Loader.isModLoaded("NotEnoughItems") && writeCurrentScreenTextNEI(text)) return;
        final GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen != null) {
            for (int i = 0; i < text.length(); ) {
                int codePoint = text.codePointAt(i);
                i += Character.charCount(codePoint);

                IngameIME_Forge.logDebugInfo(
                        "[IME KeyTyped] screen={} cp=U+{} utf16=[{}]",
                        screen.getClass().getName(),
                        Integer.toHexString(codePoint).toUpperCase(),
                        UnicodeTextHelper.debugUtf16(new String(Character.toChars(codePoint)))
                );

                sendCodePointToScreen(screen, codePoint);
            }
        }
    }

    @Optional.Method(modid = "NotEnoughItems")
    private static boolean writeCurrentScreenTextNEI(String string) {
        if (GuiContainerManager.getManager() != null) {
            for (int i = 0; i < string.length(); ) {
                int codePoint = string.codePointAt(i);
                i += Character.charCount(codePoint);

                IngameIME_Forge.logDebugInfo(
                        "[IME KeyTyped NEI] cp=U+{} utf16=[{}]",
                        Integer.toHexString(codePoint).toUpperCase(),
                        UnicodeTextHelper.debugUtf16(new String(Character.toChars(codePoint)))
                );

                sendCodePointToNEI(codePoint);
            }
            return true;
        }
        return false;
    }

    private static void sendCodePointToScreen(GuiScreen screen, int codePoint) throws IOException {
        if (codePoint <= Character.MAX_VALUE) {
            ((AccessorGuiScreen) screen).callKeyTyped((char) codePoint, Keyboard.KEY_NONE);
            return;
        }

        char[] chars = Character.toChars(codePoint);
        ((AccessorGuiScreen) screen).callKeyTyped(chars[0], Keyboard.KEY_NONE);
        ((AccessorGuiScreen) screen).callKeyTyped(chars[1], 0);
    }

    @Optional.Method(modid = "NotEnoughItems")
    private static void sendCodePointToNEI(int codePoint) {
        if (codePoint <= Character.MAX_VALUE) {
            GuiContainerManager.getManager().keyTyped((char) codePoint, Keyboard.KEY_NONE);
            return;
        }

        char[] chars = Character.toChars(codePoint);
        GuiContainerManager.getManager().keyTyped(chars[0], Keyboard.KEY_NONE);
        GuiContainerManager.getManager().keyTyped(chars[1], 0);
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

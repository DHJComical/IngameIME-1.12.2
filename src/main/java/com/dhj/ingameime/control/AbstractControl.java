package com.dhj.ingameime.control;

import com.dhj.ingameime.mixins.vanilla.AccessorGuiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
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

    /**
     * Universal write method.
     */
    @Override
    public void writeText(String text) throws IOException {
        final GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen != null) {
            for (char c : text.toCharArray()) {
                ((AccessorGuiScreen) screen).callKeyTyped(c, Keyboard.KEY_NONE);
            }
        }
    }

    /**
     * Get cursor X like vanilla text field.
     */
    protected static int getCursorX(@Nonnull FontRenderer font, @Nonnull String text, int x, int width, int lineScrollOffset, int cursorPosition, boolean enableBackgroundDrawing) {
        int cursorPosRelative = cursorPosition - lineScrollOffset;
        String visibleText = font.trimStringToWidth(text.substring(lineScrollOffset), width);
        int currentDrawX = enableBackgroundDrawing ? x + 4 : x;

        if (!visibleText.isEmpty()) {
            if (cursorPosRelative > visibleText.length()) return currentDrawX; // Perform when Ctrl + A
            String rawTextBeforeCursor = visibleText.substring(0, cursorPosRelative);
            currentDrawX += font.getStringWidth(rawTextBeforeCursor);
        }

        return currentDrawX;
    }

    /**
     * Get cursor Y like vanilla text field.
     */
    protected static int getCursorY(int y, int height, boolean enableBackgroundDrawing) {
        return (enableBackgroundDrawing ? y + (height - 8) / 2 : y) - 1;
    }
}

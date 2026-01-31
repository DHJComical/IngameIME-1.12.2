package com.dhj.ingameime.control;

import com.dhj.ingameime.ClientProxy;
import com.dhj.ingameime.mixins.betterquesting.AccessorPanelTextField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import javax.annotation.Nonnull;
import java.awt.*;
import java.lang.reflect.Method;

/**
 * Control for BetterQuesting's PanelTextField (1.7.10)
 * Refactored to use Mixin Accessors instead of Reflection.
 */
public class PanelTextFieldControl extends AbstractControl<Object> {

    protected PanelTextFieldControl(Object control) {
        super(control);
    }

    @Override
    public boolean isVisible() {
        if (controlObject instanceof AccessorPanelTextField) {
            return ((AccessorPanelTextField) controlObject).invokeIsActive();
        }
        return true;
    }

    @Nonnull
    @Override
    public Point getCursorPos() {
        if (!(controlObject instanceof AccessorPanelTextField)) {
            return new Point(0, 0);
        }

        try {
            AccessorPanelTextField accessor = (AccessorPanelTextField) controlObject;

            String text = accessor.getText();
            int selectStart = accessor.getSelectStart();
            Object transform = accessor.getTransform();

            // Transform 坐标获取
            Method getXMethod = transform.getClass().getMethod("getX");
            Method getYMethod = transform.getClass().getMethod("getY");
            int x = (int) getXMethod.invoke(transform);
            int y = (int) getYMethod.invoke(transform);

            int scrollX = accessor.invokeGetScrollX();
            int scrollY = accessor.invokeGetScrollY();

            FontRenderer font = Minecraft.getMinecraft().fontRenderer;
            String textBeforeCursor = text.substring(0, Math.min(selectStart, text.length()));
            int cursorX = x + 4 + font.getStringWidth(textBeforeCursor) - scrollX;
            int cursorY = y + 4 - scrollY;

            return new Point(cursorX - 1, cursorY - 1);
        } catch (Exception e) {
            return new Point(0, 0);
        }
    }

    public static void onFocusChange(Object object, boolean focused) {
        ClientProxy.INSTANCE.onControlFocus(new PanelTextFieldControl(object), focused, false);
    }
}

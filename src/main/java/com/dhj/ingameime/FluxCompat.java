package com.dhj.ingameime;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class FluxCompat {

    private boolean isFluxLoaded;
    private Class<?> guiFluxCoreClass;
    private Class<?> fluxTextFieldClass;

    private Field isFocusedField;
    private Field fontRendererField;
    private Field xField;
    private Field yField;
    private Field heightField;
    private Method getTextMethod;
    private Method getCursorPositionMethod;
    private Method getWidthMethod;

    public FluxCompat() {
        this.isFluxLoaded = Loader.isModLoaded("fluxnetworks");
        if (this.isFluxLoaded) {
            try {
                this.guiFluxCoreClass = Class.forName("sonar.fluxnetworks.client.gui.basic.GuiFluxCore");
                this.fluxTextFieldClass = Class.forName("sonar.fluxnetworks.client.gui.basic.GuiTextField");

                this.isFocusedField = fluxTextFieldClass.getDeclaredField("isFocused");
                this.isFocusedField.setAccessible(true);

                this.fontRendererField = fluxTextFieldClass.getDeclaredField("fontRenderer");
                this.fontRendererField.setAccessible(true);

                this.xField = fluxTextFieldClass.getDeclaredField("x");
                this.xField.setAccessible(true);

                this.yField = fluxTextFieldClass.getDeclaredField("y");
                this.yField.setAccessible(true);

                this.heightField = fluxTextFieldClass.getDeclaredField("height");
                this.heightField.setAccessible(true);

                this.getTextMethod = fluxTextFieldClass.getMethod("getText");
                this.getCursorPositionMethod = fluxTextFieldClass.getMethod("getCursorPosition");
                this.getWidthMethod = fluxTextFieldClass.getMethod("getWidth");

                IngameIME_Forge.LOG.info("Flux Networks compatibility module initialized successfully.");
            } catch (Exception e) {
                IngameIME_Forge.LOG.error("Failed to find Flux Networks classes/fields, compatibility module disabled.", e);
                this.isFluxLoaded = false;
            }
        }
    }

    @SubscribeEvent
    public void onGuiDraw(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!this.isFluxLoaded) {
            return;
        }

        GuiScreen gui = event.getGui();
        Object activeTextField = null;

        if (guiFluxCoreClass.isInstance(gui)) {
            Class<?> currentClass = gui.getClass();
            while (currentClass != null && currentClass != Object.class) {
                for (Field field : currentClass.getDeclaredFields()) {
                    if (fluxTextFieldClass.isAssignableFrom(field.getType())) {
                        try {
                            field.setAccessible(true);
                            Object textFieldObj = field.get(gui);
                            if (textFieldObj == null) continue;

                            boolean isFocused = (boolean) this.isFocusedField.get(textFieldObj);

                            if (isFocused) {
                                activeTextField = textFieldObj;

                                FontRenderer fontRenderer = (FontRenderer) this.fontRendererField.get(textFieldObj);
                                int x = this.xField.getInt(textFieldObj);
                                int y = this.yField.getInt(textFieldObj);
                                int height = this.heightField.getInt(textFieldObj);

                                String text = (String) this.getTextMethod.invoke(textFieldObj);
                                int cursorPosition = (int) this.getCursorPositionMethod.invoke(textFieldObj);
                                int width = (int) this.getWidthMethod.invoke(textFieldObj);

                                String textBeforeCursor = text.substring(0, cursorPosition);
                                String trimmedText = fontRenderer.trimStringToWidth(textBeforeCursor, width);

                                int cursorX = x + 4 + fontRenderer.getStringWidth(trimmedText);
                                int cursorY = y + (height - 8) / 2 + fontRenderer.FONT_HEIGHT + 2;

                                ClientProxy.Screen.setCaretPos(cursorX, cursorY);
                                break;
                            }
                        } catch (Exception ignored) {

                        }
                    }
                }
                if (activeTextField != null) break;
                currentClass = currentClass.getSuperclass();
            }
        }

        if (activeTextField != null) {
            if (IMStates.ActiveControl != activeTextField) {
                IMStates.ActiveControl = activeTextField;
                Internal.setActivated(true);
            }
        } else {
            if (fluxTextFieldClass.isInstance(IMStates.ActiveControl)) {
                IMStates.ActiveControl = null;
                Internal.setActivated(false);
            }
        }
    }
}
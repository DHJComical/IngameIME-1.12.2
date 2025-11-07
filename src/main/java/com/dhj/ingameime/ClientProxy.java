package com.dhj.ingameime;

import com.dhj.ingameime.control.IControl;
import com.dhj.ingameime.gui.OverlayScreen;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;

public class ClientProxy extends CommonProxy implements IMEventHandler {
    public static ClientProxy INSTANCE = null;
    public static OverlayScreen Screen = new OverlayScreen();

    private static final KeyBinding KeyBind = new KeyBinding("ingameime.key.desc", Keyboard.KEY_NONE, "In game IME");
    private static IMEventHandler IMEventHandler = IMStates.Disabled;
    private static boolean IsKeyDown = false;

    @SubscribeEvent
    public void onRenderScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!IMStates.getActiveControl().isVisible()) return;
        ClientProxy.Screen.setCaretPos(IMStates.getActiveControl().getCursorX(), IMStates.getActiveControl().getCursorY());
        ClientProxy.Screen.draw();

        if (KeyBind.isKeyDown()) {
            IsKeyDown = true;
        } else if (IsKeyDown) {
            IsKeyDown = false;
            onToggleKey();
        }

        if (Config.TurnOffOnMouseMove.getBoolean()) {
            if (IMEventHandler == IMStates.OpenedManual && (Mouse.getDX() > 0 || Mouse.getDY() > 0)) {
                onMouseMove();
            }
        }
    }

    public void preInit(@Nonnull FMLPreInitializationEvent event) {
        INSTANCE = this;
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());
        ClientRegistry.registerKeyBinding(KeyBind);
        Internal.loadLibrary();
        Internal.createInputCtx();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public IMStates onScreenClose() {
        IMEventHandler = IMEventHandler.onScreenClose();
        return null;
    }

    @Override
    public IMStates onControlFocus(@Nonnull IControl control, boolean focused, boolean isOverlay) {
        IMEventHandler = IMEventHandler.onControlFocus(control, focused, isOverlay);
        return null;
    }

    @Override
    public IMStates onScreenOpen(GuiScreen screen) {
        IMEventHandler = IMEventHandler.onScreenOpen(screen);
        return null;
    }

    @Override
    public IMStates onToggleKey() {
        IMEventHandler = IMEventHandler.onToggleKey();
        return null;
    }

    @Override
    public IMStates onMouseMove() {
        IMEventHandler = IMEventHandler.onMouseMove();
        return null;
    }
}
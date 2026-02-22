package com.dhj.ingameime;

import com.dhj.ingameime.config.Config;
import com.dhj.ingameime.control.IControl;
import com.dhj.ingameime.gui.OverlayScreen;
import com.dhj.ingameime.theme.api.ThemeManager;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import java.awt.*;

public class ClientProxy extends CommonProxy implements IMEventHandler {
    public static ClientProxy INSTANCE = null;
    public static OverlayScreen Screen = new OverlayScreen();

    private static final KeyBinding KeyBind = new KeyBinding("ingameime.key.desc", Keyboard.KEY_NONE, "ingameime.key.category");
    private static IMEventHandler IMEventHandler = IMStates.Disabled;
    private static boolean IsKeyDown = false;

    public ClientProxy() {
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onRenderScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (IMStates.getActiveControl().isVisible()) {
            Point position = IMStates.getActiveControl().getCursorPos();
            ClientProxy.Screen.setCaretPos(position.x, position.y);
            ClientProxy.Screen.draw();
        }

        if (Keyboard.isKeyDown(ClientProxy.KeyBind.getKeyCode())) {
            IsKeyDown = true;
        } else if (IsKeyDown) {
            IsKeyDown = false;
            onToggleKey();
        }

        if (Mouse.getDX() > 0 || Mouse.getDY() > 0) {
            onMouseMove();
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (KeyBind.isPressed()) {
            onToggleKey();
        }
    }

    @SubscribeEvent
    public void onConfigChanged(@Nonnull ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(Tags.MOD_ID)) {
            Config.sync();
        }
    }

    public static IMEventHandler getIMEventHandler() {
        return IMEventHandler;
    }

    @Override
    public void preInit(@Nonnull FMLPreInitializationEvent event) {
        Config.init(event.getSuggestedConfigurationFile());
        ClientRegistry.registerKeyBinding(KeyBind);
        Internal.loadLibrary();
        Internal.createInputCtx();
        MinecraftForge.EVENT_BUS.register(this);
        ThemeManager.getInstance();
    }

    @Override
    public IMStates onScreenClose() {
        IMEventHandler newEventHandler = IMEventHandler.onScreenClose();
        if (newEventHandler != IMEventHandler) {
            IMEventHandler.onLeaveState();
            IMEventHandler = newEventHandler;
            IMEventHandler.onGetState();
        }
        return null;
    }

    @Override
    public IMStates onControlFocus(@Nonnull IControl control, boolean focused, boolean isOverlay) {
        IMEventHandler newEventHandler = IMEventHandler.onControlFocus(control, focused, isOverlay);
        if (newEventHandler != IMEventHandler) {
            IMEventHandler.onLeaveState();
            IMEventHandler = newEventHandler;
            IMEventHandler.onGetState();
        }
        return null;
    }

    @Override
    public IMStates onScreenOpen(GuiScreen screen) {
        IngameIME_Forge.logDebugInfo("Screen opened {}", screen);
        IMEventHandler newEventHandler = IMEventHandler.onScreenOpen(screen);
        if (newEventHandler != IMEventHandler) {
            IMEventHandler.onLeaveState();
            IMEventHandler = newEventHandler;
            IMEventHandler.onGetState();
        }
        return null;
    }

    @Override
    public IMStates onToggleKey() {
        IMEventHandler newEventHandler = IMEventHandler.onToggleKey();
        if (newEventHandler != IMEventHandler) {
            IMEventHandler.onLeaveState();
            IMEventHandler = newEventHandler;
            IMEventHandler.onGetState();
        }
        return null;
    }

    @Override
    public IMStates onMouseMove() {
        IMEventHandler newEventHandler = IMEventHandler.onMouseMove();
        if (newEventHandler != IMEventHandler) {
            IMEventHandler.onLeaveState();
            IMEventHandler = newEventHandler;
            IMEventHandler.onGetState();
        }
        return null;
    }
}
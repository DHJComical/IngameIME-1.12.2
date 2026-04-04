package com.dhj.ingameime.mixins.vanilla;

import com.dhj.ingameime.IngameIME_Forge;
import com.dhj.ingameime.control.DirectTextFieldControl;
import com.dhj.ingameime.control.JEITextFieldControl;
import com.dhj.ingameime.control.VanillaTextFieldControl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.common.Loader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiTextField.class)
public abstract class MixinGuiTextField {
    @Inject(method = "setFocused(Z)V", at = @At("HEAD"))
    private void onSetFocus(boolean isFocusedIn, CallbackInfo ci) {
        GuiTextField self = (GuiTextField) (Object) this;

        try {
            GuiScreen currentScreen = Minecraft.getMinecraft().currentScreen;
            if (currentScreen != null) {
                String screenClassName = currentScreen.getClass().getName();
                if (screenClassName.equals("journeymap.client.ui.fullscreen.Fullscreen")) {
                    if (!isJourneyMapSearchField(self, currentScreen)) {
                        return;
                    }
                }
            }
            
            if (Loader.isModLoaded(JEITextFieldControl.JEI_MOD_ID) && JEITextFieldControl.onFocusChange(self, isFocusedIn)) {
                return;
            }
            if (currentScreen != null && currentScreen.getClass().getName().startsWith("hunternif.mc.atlas")) {
                DirectTextFieldControl.onFocusChange(self, isFocusedIn);
                return;
            }
            VanillaTextFieldControl.onFocusChange(self, isFocusedIn);
        } catch (Throwable t) {
            IngameIME_Forge.LOG.error("IngameIME failed to handle focus change. This is a compatibility issue but the game was prevented from crashing.", t);
            System.err.println("IngameIME caught an error during focus change, preventing a crash: " + t.getMessage());
        }
    }
    
    /**
     * 检查是否是 JourneyMap 的搜索框
     * 只有搜索框才应该唤起输入法
     */
    private boolean isJourneyMapSearchField(GuiTextField field, GuiScreen screen) {
        try {
            // 检查字段是否是搜索框（searchTextX 或 searchTextZ）
            // 通过反射获取 screen 中的 searchTextX 和 searchTextZ 字段
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
        } catch (Exception e) {
            // 反射失败，默认允许
        }
        return false;
    }
}
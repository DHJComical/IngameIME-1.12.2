package dhj.ingameime.mixins;

import dhj.ingameime.ClientProxy;
import dhj.ingameime.Internal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    private static final Logger log = LogManager.getLogger(MixinMinecraft.class);

    @Inject(method = "toggleFullscreen", at = @At(value = "HEAD"))
    void preToggleFullscreen(CallbackInfo ci) {
        Internal.destroyInputCtx();
    }

    @Inject(method = "toggleFullscreen", at = @At(value = "RETURN"))
    void postToggleFullscreen(CallbackInfo ci) {
        Internal.createInputCtx();
    }

    @Inject(method = "displayGuiScreen", at = @At("RETURN"))
    private void postDisplayScreen(GuiScreen guiScreenIn, CallbackInfo ci) {
        try {
            // 如果还没初始化好 Screen proxy，就跳过
            if (ClientProxy.Screen == null) {
                return;
            }

            // 重置光标
            ClientProxy.Screen.setCaretPos(0, 0);

            // guiScreenIn 为 null 则说明刚关闭所有界面
            if (guiScreenIn == null) {
                ClientProxy.INSTANCE.onScreenClose();
            } else {
                ClientProxy.INSTANCE.onScreenOpen(guiScreenIn);
            }
        }catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}


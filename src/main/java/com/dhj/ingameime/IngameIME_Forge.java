package com.dhj.ingameime;

import com.dhj.ingameime.config.Config;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = Tags.MOD_ID,
        version = Tags.VERSION,
        name = Tags.MOD_NAME,
        clientSideOnly = true,
        acceptedMinecraftVersions = "[1.12.2]",
        acceptableRemoteVersions = "*",
        dependencies = "required-after:mixinbooter@[8.0,)",
        guiFactory = "com.dhj.ingameime.config.ConfigGuiFactory"
)
public class IngameIME_Forge {
    public static final Logger LOG = LogManager.getLogger(Tags.MOD_NAME);
    @SidedProxy(clientSide = "com.dhj.ingameime.ClientProxy", serverSide = "com.dhj.ingameime.CommonProxy")
    public static CommonProxy proxy;

    /**
     * For some logs which are not important most time.
     */
    public static void logDebugInfo(String message, Object... params) {
        if (Config.DebugLog) LOG.info(message, params);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }
}

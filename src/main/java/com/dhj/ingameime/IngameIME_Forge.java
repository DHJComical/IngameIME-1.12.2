package com.dhj.ingameime;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = IngameIME_Forge.MOD_ID,
        version = Tags.VERSION,
        name = IngameIME_Forge.MOD_NAME,
        acceptedMinecraftVersions = "[1.7.10]",
        acceptableRemoteVersions = "*",
        dependencies = "required-after:unimixins",
        guiFactory = "com.dhj.ingameime.ConfigGuiFactory"
)
public class IngameIME_Forge {
    public static final String MOD_ID = "ingameime";
    public static final String MOD_NAME = "IngameIME";

    public static final Logger LOG = LogManager.getLogger(MOD_NAME);
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

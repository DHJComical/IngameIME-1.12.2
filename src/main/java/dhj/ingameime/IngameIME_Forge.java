package dhj.ingameime;

import com.dhj.imgameime.ingameime.Tags;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = Tags.MOD_ID,
        version = Tags.VERSION,
        name = Tags.MOD_NAME,
        acceptedMinecraftVersions = "[1.12.2]",
        acceptableRemoteVersions = "*",
        dependencies = "required-after:mixinbooter;after:jei"
)
public class IngameIME_Forge {
    public static final Logger LOG = LogManager.getLogger(Tags.MOD_NAME);
    @SidedProxy(clientSide = "dhj.ingameime.ClientProxy", serverSide = "dhj.ingameime.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }
}

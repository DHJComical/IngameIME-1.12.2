package com.dhj.ingameime.mixins;

import net.minecraftforge.fml.common.Loader;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class MixinLate implements ILateMixinLoader {
    @Override
    public List<String> getMixinConfigs() {
        List<String> mixins = new ArrayList<>();
        if (Loader.isModLoaded("fluxnetworks")) {
            mixins.add("mixins.ingameime.flux.json");
        }
        return mixins;
    }
}

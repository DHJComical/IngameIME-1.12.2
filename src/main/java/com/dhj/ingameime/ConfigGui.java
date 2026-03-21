package com.dhj.ingameime;

import com.dhj.ingameime.theme.ThemeEditorConfigElement;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ConfigGui extends GuiConfig {
    public ConfigGui(GuiScreen parent) {
        super(parent, getConfigElements(), IngameIME_Forge.MOD_ID, false, false, IngameIME_Forge.MOD_NAME);
    }

    private static @Nonnull List<IConfigElement> getConfigElements() {
        List<IConfigElement> list = new ArrayList<>();
        Configuration config = Config.getConfig();
        for (String category : Config.CATEGORIES) {
            list.addAll(new ConfigElement(config.getCategory(category)).getChildElements());
        }
        // Add theme editor entry
        list.add(new ThemeEditorConfigElement());

        return list;
    }
}

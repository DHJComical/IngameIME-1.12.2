package com.dhj.ingameime.theme;

import com.dhj.ingameime.theme.api.Theme;
import com.dhj.ingameime.theme.api.ThemeManager;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.config.IConfigElement;
import net.minecraft.client.resources.I18n;

/**
 * Theme Editor Configuration Entry
 */
public class ThemeEditorEntry extends GuiConfigEntries.ButtonEntry implements ThemeManager.ThemeChangeListener {
    public ThemeEditorEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
        super(owningScreen, owningEntryList, configElement);
        ThemeManager.getInstance().addThemeChangeListener(this);
        updateButtonText();
    }

    private void updateButtonText() {
        ThemeManager themeManager = ThemeManager.getInstance();
        Theme currentTheme = themeManager.getCurrentTheme();
        if (currentTheme != null) {
            this.btnValue.displayString = I18n.format(
                "ingameime.config.theme.current_theme_button",
                currentTheme.getName()
            );
        } else {
            this.btnValue.displayString = I18n.format(
                "ingameime.config.theme.editor_button"
            );
        }
    }

    @Override
    public void valueButtonPressed(int slotIndex) {
        if (owningScreen.mc != null) {
            owningScreen.mc.displayGuiScreen(new ThemeEditorGui(owningScreen));
        }
    }

    @Override
    public boolean isDefault() {
        return false;
    }

    @Override
    public void setToDefault() {
    }

    @Override
    public boolean isChanged() {
        return false;
    }

    @Override
    public void updateValueButtonText() {
        updateButtonText();
    }

    @Override
    public void onThemeChanged(Theme newTheme) {
        updateButtonText();
    }

    @Override
    public void undoChanges() {
    }

    @Override
    public boolean saveConfigElement() {
        return false;
    }

    @Override
    public Object getCurrentValue() {
        return null;
    }

    @Override
    public Object[] getCurrentValues() {
        return new Object[0];
    }
}

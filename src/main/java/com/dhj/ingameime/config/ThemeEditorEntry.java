package com.dhj.ingameime.config;

import com.dhj.ingameime.theme.ThemeEditorGui;
import com.dhj.ingameime.theme.ThemeManager;
import com.dhj.ingameime.theme.Theme;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.config.GuiConfigEntries.ButtonEntry;

/**
 * 主题编辑器配置条目
 */
public class ThemeEditorEntry extends ButtonEntry implements ThemeManager.ThemeChangeListener {
    public ThemeEditorEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
        super(owningScreen, owningEntryList, configElement);
        ThemeManager.getInstance().addThemeChangeListener(this);
        updateButtonText();
    }
    
    private void updateButtonText() {
        ThemeManager themeManager = ThemeManager.getInstance();
        Theme currentTheme = themeManager.getCurrentTheme();
        if (currentTheme != null) {
            // 使用翻译键，支持主题名称参数
            this.btnValue.displayString = I18n.format(
                "ingameime.config.theme.current_theme_button",
                currentTheme.getName()
            );
        } else {
            // 使用翻译键
            this.btnValue.displayString = I18n.format(
                "ingameime.config.theme.editor_button"
            );
        }
    }
    
    @Override
    public void valueButtonPressed(int slotIndex) {
        // 打开主题编辑器
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
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight,
                          int mouseX, int mouseY, boolean isSelected, float partial) {
        super.drawEntry(slotIndex, x, y, listWidth, slotHeight, mouseX, mouseY, isSelected, partial);
    }
    
    @Override
    public boolean mousePressed(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
        return super.mousePressed(index, x, y, mouseEvent, relativeX, relativeY);
    }
    
    @Override
    public void mouseReleased(int index, int x, int y, int mouseEvent, int relativeX, int relativeY) {
        super.mouseReleased(index, x, y, mouseEvent, relativeX, relativeY);
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
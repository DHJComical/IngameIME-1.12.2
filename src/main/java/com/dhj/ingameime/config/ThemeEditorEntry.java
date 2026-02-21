package com.dhj.ingameime.config;

import com.dhj.ingameime.theme.ThemeEditorGui;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.config.GuiConfigEntries.ButtonEntry;

/**
 * 主题编辑器配置条目
 */
public class ThemeEditorEntry extends ButtonEntry {
    public ThemeEditorEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
        super(owningScreen, owningEntryList, configElement);
        this.btnValue.displayString = "打开主题编辑器";
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
        this.btnValue.displayString = "打开主题编辑器";
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
package com.dhj.ingameime.theme;

import cpw.mods.fml.client.config.ConfigGuiType;
import cpw.mods.fml.client.config.GuiEditArrayEntries;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.config.IConfigElement;

import java.util.List;

/**
 * 主题编辑器配置元素
 */
public class ThemeEditorConfigElement implements IConfigElement {
    @Override
    public boolean isProperty() {
        return true;
    }

    @Override
    public Class<? extends GuiConfigEntries.IConfigEntry> getConfigEntryClass() {
        return ThemeEditorEntry.class;
    }

    @Override
    public String getName() {
        return "Theme Editor";
    }

    @Override
    public String getQualifiedName() {
        return "ingameime.config.theme.editor";
    }

    @Override
    public String getLanguageKey() {
        return "ingameime.config.theme.editor";
    }

    @Override
    public String getComment() {
        return "Open Theme Editor to edit theme that you IME";
    }

    @Override
    public List<IConfigElement> getChildElements() {
        return null;
    }

    @Override
    public boolean isList() {
        return false;
    }

    @Override
    public boolean isListLengthFixed() {
        return false;
    }

    @Override
    public int getMaxListLength() {
        return 0;
    }

    @Override
    public boolean isDefault() {
        return false;
    }

    @Override
    public Object getDefault() {
        return null;
    }

    @Override
    public Object[] getDefaults() {
        return null;
    }

    @Override
    public void setToDefault() {
    }

    @Override
    public boolean requiresWorldRestart() {
        return false;
    }

    @Override
    public boolean showInGui() {
        return true;
    }

    @Override
    public boolean requiresMcRestart() {
        return false;
    }

    @Override
    public Object get() {
        return null;
    }

    @Override
    public Object[] getList() {
        return null;
    }

    @Override
    public void set(Object value) {
    }

    @Override
    public void set(Object[] values) {
    }

    @Override
    public String[] getValidValues() {
        return null;
    }

    @Override
    public Object getMinValue() {
        return null;
    }

    @Override
    public Object getMaxValue() {
        return null;
    }

    @Override
    public java.util.regex.Pattern getValidationPattern() {
        return null;
    }

    @Override
    public ConfigGuiType getType() {
        return ConfigGuiType.CONFIG_CATEGORY;
    }

    @Override
    public Class<? extends GuiEditArrayEntries.IArrayEntry> getArrayEntryClass() {
        return null;
    }
}

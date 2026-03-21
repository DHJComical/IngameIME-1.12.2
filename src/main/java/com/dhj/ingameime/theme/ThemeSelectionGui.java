package com.dhj.ingameime.theme;

import com.dhj.ingameime.mixins.vanilla.MixinGuiSlot;
import com.dhj.ingameime.theme.api.Theme;
import com.dhj.ingameime.theme.api.ThemeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GUI for selecting a theme from a scrollable list.
 */
public class ThemeSelectionGui extends GuiScreen {
    private final ThemeEditorGui parent;
    private ThemeList themeList;
    private final List<Theme> themes = new ArrayList<>();
    private int selectedIndex = -1;
    private GuiButton btnSelect;

    public ThemeSelectionGui(ThemeEditorGui parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        themes.clear();
        Map<String, Theme> map = ThemeManager.getInstance().getAvailableThemes();
        themes.addAll(map.values());

        themes.sort((t1, t2) -> {
            int score1 = getSortScore(t1.getId());
            int score2 = getSortScore(t2.getId());
            if (score1 != score2) return score1 - score2;
            return t1.getName().compareToIgnoreCase(t2.getName());
        });

        this.themeList = new ThemeList(this);

        String currentId = parent.getSelectedThemeId();
        for (int i = 0; i < themes.size(); i++) {
            if (themes.get(i).getId().equals(currentId)) {
                this.selectedIndex = i;
                break;
            }
        }

        this.btnSelect = new GuiButton(1, width / 2 - 154, height - 32, 150, 20, I18n.format("ingameime.theme.selection.confirm"));
        this.buttonList.add(btnSelect);
        this.buttonList.add(new GuiButton(0, width / 2 + 4, height - 32, 150, 20, I18n.format("gui.cancel")));

        updateButtonState();
    }

    private int getSortScore(String id) {
        if ("default".equals(id)) return 1;
        if ("dark".equals(id)) return 2;
        if ("light".equals(id)) return 3;
        return 10;
    }

    private void updateButtonState() {
        btnSelect.enabled = selectedIndex >= 0 && selectedIndex < themes.size();
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0 && this.themeList != null) {
            // Use Mixin to access protected method
            ((MixinGuiSlot) (Object) this.themeList).invokeHandleMouseInput(0, 0);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            if (button.id == 0) {
                this.mc.displayGuiScreen(parent);
            } else if (button.id == 1) {
                confirmSelection();
            }
        }
    }

    private void confirmSelection() {
        if (selectedIndex >= 0 && selectedIndex < themes.size()) {
            parent.setSelectedThemeId(themes.get(selectedIndex).getId());
            this.mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.themeList.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.mc.fontRenderer, I18n.format("ingameime.theme.selection.title"), this.width / 2, 15, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    class ThemeList extends GuiSlot {
        private final ThemeSelectionGui gui;

        public ThemeList(ThemeSelectionGui gui) {
            super(ThemeSelectionGui.this.mc, gui.width, gui.height, 32, gui.height - 40, 24);
            this.gui = gui;
        }

        @Override
        protected int getSize() {
            return themes.size();
        }

        @Override
        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
            selectedIndex = slotIndex;
            updateButtonState();
            if (isDoubleClick) confirmSelection();
        }

        @Override
        protected boolean isSelected(int slotIndex) {
            return slotIndex == selectedIndex;
        }

        @Override
        protected void drawBackground() {}

        @Override
        protected void drawSlot(int slotIndex, int xPos, int yPos, int heightIn, Tessellator tessellator, int mouseXIn, int mouseYIn) {
            Theme theme = themes.get(slotIndex);

            String nameText = theme.getName();
            String idText = " (" + theme.getId() + ")";

            int totalTextWidth = mc.fontRenderer.getStringWidth(nameText + idText);
            int startX = (gui.width - totalTextWidth) / 2;
            int drawY = yPos + 7;

            int nameColor = (slotIndex == selectedIndex) ? 0xFFFF55 : 0xFFFFFF;
            mc.fontRenderer.drawString(nameText, startX, drawY, nameColor);

            int idColor = 0x808080;
            mc.fontRenderer.drawString(idText, startX + mc.fontRenderer.getStringWidth(nameText), drawY, idColor);
        }
    }
}

package com.dhj.ingameime.theme;

import com.dhj.ingameime.IngameIME_Forge;
import com.dhj.ingameime.mixins.vanilla.MixinGuiTextFieldAccess;
import com.dhj.ingameime.theme.api.Theme;
import com.dhj.ingameime.theme.api.ThemeManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;

/**
 * Theme Editor GUI
 */
public class ThemeEditorGui extends GuiScreen {
    private final GuiScreen parent;
    private final ThemeManager themeManager;
    private GuiTextField txtThemeName;
    private GuiTextField txtThemeId;
    private String selectedThemeId = "default";

    private final GuiTextField[] colorFields = new GuiTextField[6];
    private final String[] colorLabels = {
        I18n.format("ingameime.theme.editor.text_color"),
        I18n.format("ingameime.theme.editor.background_color"),
        I18n.format("ingameime.theme.editor.index_color"),
        I18n.format("ingameime.theme.editor.selected_bg"),
        I18n.format("ingameime.theme.editor.cursor_color"),
        I18n.format("ingameime.theme.editor.border_color")
    };

    private GuiTextField txtPadding;
    private GuiTextField txtCandidatePadding;
    private GuiTextField txtBorderWidth;

    private final java.util.List<String> themeIds = new java.util.ArrayList<>();
    private final ThemeNameInputGui nameInputGui;

    // Scroll related items
    private int scrollOffset = 0;
    private int maxScrollOffset = 0;
    private int contentHeight = 400;
    private int viewportHeight = 200;
    private boolean isScrolling = false;

    private final int[] previewColors = new int[6];

    // Color preview click areas
    private final int[] previewX = new int[6];
    private final int[] previewY = new int[6];

    public ThemeEditorGui(GuiScreen parent) {
        this.parent = parent;
        this.themeManager = ThemeManager.getInstance();
        this.nameInputGui = new ThemeNameInputGui(this);

        Theme current = themeManager.getCurrentTheme();
        if (current != null) {
            this.selectedThemeId = current.getId();
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        // Check if entering the theme name in the GUI returns and confirm.
        if (nameInputGui.isConfirmed()) {
                String themeName = nameInputGui.getThemeName();
                if (!themeName.isEmpty()) {
                    String newThemeId = generateThemeIdFromName(themeName);
                    Theme newTheme = Theme.createCustomTheme(newThemeId, themeName);
                    themeManager.saveCustomThemeToResourcePack(newTheme);
                    selectedThemeId = newThemeId;
                    // Reset the name input GUI state after creating the theme
                    nameInputGui.reset();
            }
        }

        // Load the list of available themes
        loadThemeList();

        // Add back button
        GuiButton btnBack = new GuiButton(0, width / 2 - 155, height - 29, 150, 20, I18n.format("gui.back"));
        buttonList.add(btnBack);

        // Add apply button
        GuiButton btnApply = new GuiButton(1, width / 2 + 5, height - 29, 150, 20, I18n.format("ingameime.theme.editor.apply"));
        buttonList.add(btnApply);

        // Add create new theme button
        GuiButton btnCreateNew = new GuiButton(2, width / 2 - 155, 25, 150, 20, I18n.format("ingameime.theme.editor.create"));
        buttonList.add(btnCreateNew);

        // Add delete theme button
        GuiButton btnDelete = new GuiButton(3, width / 2 + 5, 25, 150, 20, I18n.format("ingameime.theme.editor.delete"));
        buttonList.add(btnDelete);

        // Add theme selection button
        GuiButton btnSelectTheme = new GuiButton(4, width / 2 - 155, 50, 150, 20, I18n.format("ingameime.theme.editor.select"));
        buttonList.add(btnSelectTheme);

        // Theme ID Input Box
        txtThemeId = new GuiTextField(mc.fontRenderer, width / 2 - 100, 75, 200, 20);
        txtThemeId.setMaxStringLength(50);
        txtThemeId.setEnabled(false);

        // Theme name input box
        txtThemeName = new GuiTextField(mc.fontRenderer, width / 2 - 100, 100, 200, 20);
        txtThemeName.setMaxStringLength(50);

        // Create color input fields
        int fieldWidth = 100;
        int fieldHeight = 20;
        int labelWidth = 120;
        int x = width / 2 - 160;
        int colorFieldsStartY = 100;
        int y = colorFieldsStartY + 40;

        for (int i = 0; i < colorLabels.length; i++) {
            colorFields[i] = new GuiTextField(mc.fontRenderer, x + labelWidth, y, fieldWidth, fieldHeight);
            colorFields[i].setMaxStringLength(8);
            y += 25;
        }

        // Add padding input fields
        y += 10;
        txtPadding = new GuiTextField(mc.fontRenderer, x + labelWidth, y, fieldWidth, fieldHeight);
        txtPadding.setMaxStringLength(2);

        y += 25;
        txtCandidatePadding = new GuiTextField(mc.fontRenderer, x + labelWidth, y, fieldWidth, fieldHeight);
        txtCandidatePadding.setMaxStringLength(2);

        y += 25;
        txtBorderWidth = new GuiTextField(mc.fontRenderer, x + labelWidth, y, fieldWidth, fieldHeight);
        txtBorderWidth.setMaxStringLength(2);

        loadCurrentTheme();
    }

    private void loadThemeList() {
        themeIds.clear();
        // Scan for new themes before loading the list
        themeManager.scanForNewThemes();
        Map<String, Theme> availableThemes = themeManager.getAvailableThemes();
        themeIds.addAll(availableThemes.keySet());
        IngameIME_Forge.logDebugInfo("[ThemeEditor] Loaded {} themes: {}", themeIds.size(), themeIds);

        int themeSelectionIndex = themeIds.indexOf(selectedThemeId);
        if (themeSelectionIndex == -1 && !themeIds.isEmpty()) {
            selectedThemeId = themeIds.get(0);
        }
    }

    private void loadCurrentTheme() {
        Theme theme = null;

        if (selectedThemeId != null && !selectedThemeId.isEmpty()) {
            theme = themeManager.getTheme(selectedThemeId);
        }

        if (theme == null) {
            theme = themeManager.getCurrentTheme();
            if (theme != null) {
                selectedThemeId = theme.getId();
            }
        }

        if (theme != null) {
            loadThemeToEditor(theme);
        }
    }

    private void loadThemeToEditor(Theme theme) {
        if (theme == null) return;

        selectedThemeId = theme.getId();
        txtThemeId.setText(theme.getId());
        txtThemeName.setText(theme.getName());

        colorFields[0].setText(String.format("%08X", theme.getTextColor()));
        colorFields[1].setText(String.format("%08X", theme.getBackgroundColor()));
        colorFields[2].setText(String.format("%08X", theme.getIndexColor()));
        colorFields[3].setText(String.format("%08X", theme.getSelectedBackgroundColor()));
        colorFields[4].setText(String.format("%08X", theme.getCursorColor()));
        colorFields[5].setText(String.format("%08X", theme.getBorderColor()));

        txtPadding.setText(String.valueOf(theme.getPadding()));
        txtCandidatePadding.setText(String.valueOf(theme.getCandidatePadding()));
        txtBorderWidth.setText(String.valueOf(theme.getBorderWidth()));
        updateColorPreviews();
    }

    private void saveCurrentTheme() {
        try {
            Theme theme = themeManager.getTheme(selectedThemeId);

            if (theme == null) {
                theme = Theme.createCustomTheme(selectedThemeId, txtThemeName.getText());
            }

            theme.setName(txtThemeName.getText());
            theme.setTextColor((int)Long.parseLong(colorFields[0].getText(), 16));
            theme.setBackgroundColor((int)Long.parseLong(colorFields[1].getText(), 16));
            theme.setIndexColor((int)Long.parseLong(colorFields[2].getText(), 16));
            theme.setSelectedBackgroundColor((int)Long.parseLong(colorFields[3].getText(), 16));
            theme.setCursorColor((int)Long.parseLong(colorFields[4].getText(), 16));
            theme.setBorderColor((int)Long.parseLong(colorFields[5].getText(), 16));

            theme.setPadding(Integer.parseInt(txtPadding.getText()));
            theme.setCandidatePadding(Integer.parseInt(txtCandidatePadding.getText()));
            theme.setBorderWidth(Integer.parseInt(txtBorderWidth.getText()));

            themeManager.saveCustomThemeToResourcePack(theme);
            themeManager.setThemeAndNotify(selectedThemeId);
            loadThemeList();

            IngameIME_Forge.logDebugInfo("[ThemeEditor] Theme saved and applied: {} ({})", selectedThemeId, theme.getName());
        } catch (Exception e) {
            IngameIME_Forge.logDebugInfo("[ThemeEditor] Save failed: {}", e.getMessage());
        }
    }

    private int parseColor(String hex) {
        if (hex == null || hex.isEmpty()) {
            return 0;
        }
        try {
            hex = hex.replace("#", "").replace("0x", "").replace("0X", "");
            return (int) Long.parseLong(hex, 16);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void setSelectedThemeId(String themeId) {
        this.selectedThemeId = themeId;
        loadCurrentTheme();
    }

    public String getSelectedThemeId() {
        return this.selectedThemeId;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);

        if (button.id == 0) {
            themeManager.reloadThemes();
            mc.displayGuiScreen(parent);
        } else if (button.id == 1) {
            saveCurrentTheme();
        } else if (button.id == 2) {
            nameInputGui.reset();
            mc.displayGuiScreen(nameInputGui);
        } else if (button.id == 3) {
            if (!selectedThemeId.equals("default") &&
                    !selectedThemeId.equals("dark") &&
                    !selectedThemeId.equals("light")) {
                themeManager.deleteCustomTheme(selectedThemeId);
                themeManager.setThemeAndNotify("default");
                selectedThemeId = "default";
                loadThemeList();
                loadCurrentTheme();
            }
        } else if (button.id == 4) {
            mc.displayGuiScreen(new ThemeSelectionGui(this));
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        int scrollAreaTop = 80;
        int bottomAreaHeight = 50;
        int scrollAreaBottom = height - bottomAreaHeight;

        if (mouseX >= width - 15 && mouseX <= width && maxScrollOffset > 0) {
            if (mouseY >= scrollAreaTop && mouseY <= scrollAreaBottom) {
                this.isScrolling = true;
                updateScrollFromMouse(mouseY);
                return;
            }
        }

        if (mouseY > scrollAreaTop && mouseY < scrollAreaBottom) {
            boolean clickedPreview = false;
            for (int i = 0; i < colorLabels.length; i++) {
                if (previewX[i] > 0 && previewY[i] > 0) {
                    int px = previewX[i] - 1;
                    int py = previewY[i] - 1;
                    int previewSize = 20;
                    if (mouseX >= px && mouseX <= px + previewSize + 2 &&
                        mouseY >= py && mouseY <= py + previewSize + 2) {
                        int currentColor = parseColor(colorFields[i].getText());
                        final int colorIndex = i;
                        mc.displayGuiScreen(new ColorPickerGui(this, currentColor, newColor -> {
                            Theme theme = themeManager.getTheme(selectedThemeId);
                            if (theme != null) {
                                if (colorIndex == 0) theme.setTextColor(newColor);
                                else if (colorIndex == 1) theme.setBackgroundColor(newColor);
                                else if (colorIndex == 2) theme.setIndexColor(newColor);
                                else if (colorIndex == 3) theme.setSelectedBackgroundColor(newColor);
                                else if (colorIndex == 4) theme.setCursorColor(newColor);
                                else if (colorIndex == 5) theme.setBorderColor(newColor);
                            }
                        }));
                        clickedPreview = true;
                        break;
                    }
                }
            }

            if (!clickedPreview) {
                txtThemeId.mouseClicked(mouseX, mouseY, mouseButton);
                txtThemeName.mouseClicked(mouseX, mouseY, mouseButton);
                for (GuiTextField field : colorFields) {
                    if (field != null) field.mouseClicked(mouseX, mouseY, mouseButton);
                }
                if (txtPadding != null) txtPadding.mouseClicked(mouseX, mouseY, mouseButton);
                if (txtCandidatePadding != null) txtCandidatePadding.mouseClicked(mouseX, mouseY, mouseButton);
                if (txtBorderWidth != null) txtBorderWidth.mouseClicked(mouseX, mouseY, mouseButton);
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    protected void mouseReleased(int mouseX, int mouseY, int state) {
        this.isScrolling = false;
    }

    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (this.isScrolling && clickedMouseButton == 0) {
            updateScrollFromMouse(mouseY);
        }
    }

    private void updateScrollFromMouse(int mouseY) {
        int scrollAreaTop = 80;
        int bottomAreaHeight = 50;
        int scrollAreaBottom = height - bottomAreaHeight;
        int trackHeight = scrollAreaBottom - scrollAreaTop;

        float f = (float)(mouseY - scrollAreaTop) / (float)trackHeight;
        this.scrollOffset = (int)(f * (float)contentHeight) - (viewportHeight / 2);
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, maxScrollOffset));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);

        // Handle keyboard input for all text fields
        txtThemeId.textboxKeyTyped(typedChar, keyCode);
        txtThemeName.textboxKeyTyped(typedChar, keyCode);
        for (GuiTextField field : colorFields) {
            if (field != null) {
                field.textboxKeyTyped(typedChar, keyCode);
            }
        }

        if (txtPadding != null) txtPadding.textboxKeyTyped(typedChar, keyCode);
        if (txtCandidatePadding != null) txtCandidatePadding.textboxKeyTyped(typedChar, keyCode);
        if (txtBorderWidth != null) txtBorderWidth.textboxKeyTyped(typedChar, keyCode);

        updateColorPreviews();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawBackground(0);

        int labelWidth = 120;
        int scrollAreaTop = 80;
        int bottomAreaHeight = 45;
        int scrollAreaBottom = height - bottomAreaHeight;

        viewportHeight = scrollAreaBottom - scrollAreaTop;
        contentHeight = 305;
        maxScrollOffset = Math.max(0, contentHeight - viewportHeight);
        scrollOffset = Math.min(scrollOffset, maxScrollOffset);

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int scaleFactor = sr.getScaleFactor();
        GL11.glScissor(0, bottomAreaHeight * scaleFactor, width * scaleFactor, viewportHeight * scaleFactor);

        int x = width / 2 - 150;
        int startY = scrollAreaTop + 10;
        int currentY = startY - scrollOffset;

        ((MixinGuiTextFieldAccess) (Object) txtThemeName).setYPosition(currentY);
        txtThemeName.drawTextBox();
        currentY += 30;

        int previewX = x + labelWidth + 110;
        int previewSize = 20;

        for (int i = 0; i < colorLabels.length; i++) {
            mc.fontRenderer.drawString(colorLabels[i], x, currentY + 5, 0xFFFFFF);
            if (colorFields[i] != null) {
                ((MixinGuiTextFieldAccess) (Object) colorFields[i]).setYPosition(currentY);
                colorFields[i].drawTextBox();
            }
            this.previewX[i] = previewX;
            this.previewY[i] = currentY;
            drawRect(previewX - 1, currentY - 1, previewX + previewSize + 1, currentY + previewSize + 1, 0xFF000000);
            drawRect(previewX, currentY, previewX + previewSize, currentY + previewSize, previewColors[i]);
            mc.fontRenderer.drawString("\u258A", previewX + previewSize + 3, currentY + 5, 0xAAAAAA);
            currentY += 25;
        }

        currentY += 10;
        mc.fontRenderer.drawString(I18n.format("ingameime.theme.editor.padding"), x, currentY + 5, 0xFFFFFF);
        if (txtPadding != null) { ((MixinGuiTextFieldAccess) (Object) txtPadding).setYPosition(currentY); txtPadding.drawTextBox(); }
        currentY += 25;
        mc.fontRenderer.drawString(I18n.format("ingameime.theme.editor.candidate_padding"), x, currentY + 5, 0xFFFFFF);
        if (txtCandidatePadding != null) { ((MixinGuiTextFieldAccess) (Object) txtCandidatePadding).setYPosition(currentY); txtCandidatePadding.drawTextBox(); }
        currentY += 25;
        mc.fontRenderer.drawString(I18n.format("ingameime.theme.editor.border_width"), x, currentY + 5, 0xFFFFFF);
        if (txtBorderWidth != null) { ((MixinGuiTextFieldAccess) (Object) txtBorderWidth).setYPosition(currentY); txtBorderWidth.drawTextBox(); }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        overlayBackground(0, scrollAreaTop);
        overlayBackground(scrollAreaBottom, height);

        drawGradientRect(0, scrollAreaTop, width, scrollAreaTop + 4, 0xFF000000, 0x00000000);
        drawGradientRect(0, scrollAreaBottom - 4, width, scrollAreaBottom, 0x00000000, 0xFF000000);

        drawCenteredString(mc.fontRenderer, I18n.format("ingameime.theme.editor.title"), width / 2, 10, 0xFFFFFF);
        mc.fontRenderer.drawString(I18n.format("ingameime.theme.editor.current") + ": " + selectedThemeId, width / 2 + 5, 56, 0xFFFFFF);

        if (maxScrollOffset > 0) {
            drawScrollBar(scrollAreaTop, scrollAreaBottom);
        }

        for (GuiButton button : buttonList) {
            button.drawButton(mc, mouseX, mouseY);
        }
    }

    protected void overlayBackground(int startY, int endY) {
        Tessellator tessellator = Tessellator.instance;
        this.mc.renderEngine.bindTexture(new ResourceLocation("textures/gui/options_background.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(0.0D, endY, 0.0D, 0.0D, (float)endY / 32.0F);
        tessellator.addVertexWithUV(this.width, endY, 0.0D, (float)this.width / 32.0F, (float)endY / 32.0F);
        tessellator.addVertexWithUV(this.width, startY, 0.0D, (float)this.width / 32.0F, (float)startY / 32.0F);
        tessellator.addVertexWithUV(0.0D, startY, 0.0D, 0.0D, (float)startY / 32.0F);
        tessellator.draw();
    }

    private void drawScrollBar(int top, int bottom) {
        int scrollBarX = width - 6;
        int scrollBarHeight = bottom - top;
        int thumbHeight = Math.max(20, (int) ((float) scrollBarHeight / contentHeight * scrollBarHeight));
        int thumbY = top + (int) ((float) scrollOffset / maxScrollOffset * (scrollBarHeight - thumbHeight));

        drawRect(scrollBarX, top, scrollBarX + 6, bottom, 0xFF000000);
        drawRect(scrollBarX, thumbY, scrollBarX + 6, thumbY + thumbHeight, 0xFF808080);
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();

        int scroll = Mouse.getEventDWheel();
        if (scroll != 0) {
            scroll = scroll > 0 ? -20 : 20;
            scrollOffset = Math.max(0, Math.min(scrollOffset + scroll, maxScrollOffset));
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        // Update cursor counter for all text fields
        txtThemeId.updateCursorCounter();
        txtThemeName.updateCursorCounter();
        for (GuiTextField field : colorFields) {
            if (field != null) {
                field.updateCursorCounter();
            }
        }

        if (txtPadding != null) txtPadding.updateCursorCounter();
        if (txtCandidatePadding != null) txtCandidatePadding.updateCursorCounter();
        if (txtBorderWidth != null) txtBorderWidth.updateCursorCounter();
    }

    private void updateColorPreviews() {
        for (int i = 0; i < colorLabels.length; i++) {
            if (colorFields[i] != null) {
                previewColors[i] = parseColor(colorFields[i].getText());
            }
        }
    }

    private String generateThemeIdFromName(String themeName) {
        if (themeName == null || themeName.isEmpty()) {
            return "custom_theme";
        }

        String safeId = themeName.toLowerCase().replaceAll("[^a-z0-9_]", "_");
        String finalId = safeId;
        int suffix = 1;
        while (themeManager.getTheme(finalId) != null) {
            finalId = safeId + "_" + suffix++;
        }
        return finalId;
    }
}

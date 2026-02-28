package com.dhj.ingameime.theme;

import com.dhj.ingameime.IngameIME_Forge;
import com.dhj.ingameime.theme.api.Theme;
import com.dhj.ingameime.theme.api.ThemeManager;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
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

    private final GuiTextField[] colorFields = new GuiTextField[10];
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
                // Generate a secure theme ID using the theme name.
                String newThemeId = generateThemeIdFromName(themeName);
                Theme newTheme = Theme.createCustomTheme(newThemeId, themeName);
                themeManager.saveCustomTheme(newTheme);
                selectedThemeId = newThemeId;
            }
        }

        // Load the list of available themes
        loadThemeList();

        // Add back button
        GuiButton btnBack = new GuiButton(0, width / 2 - 155, height - 29, 150, 20, I18n.format("gui.back"));
        buttonList.add(btnBack);

        // Add app button
        GuiButton btnApply = new GuiButton(1, width / 2 + 5, height - 29, 150, 20, I18n.format("ingameime.theme.editor.apply"));
        buttonList.add(btnApply);

        // Add a "Create New Theme" button
        GuiButton btnCreateNew = new GuiButton(2, width / 2 - 155, 25, 150, 20, I18n.format("ingameime.theme.editor.create"));
        buttonList.add(btnCreateNew);

        // Add a "Remove Theme" Buttons
        GuiButton btnDelete = new GuiButton(3, width / 2 + 5, 25, 150, 20, I18n.format("ingameime.theme.editor.delete"));
        buttonList.add(btnDelete);

        // Add a "Theme Selection" button
        GuiButton btnSelectTheme = new GuiButton(4, width / 2 - 155, 50, 150, 20, I18n.format("ingameime.theme.editor.select"));
        buttonList.add(btnSelectTheme);

        // Theme ID Input Box
        txtThemeId = new GuiTextField(5, fontRenderer, width / 2 - 100, 75, 200, 20);
        txtThemeId.setMaxStringLength(50);
        txtThemeId.setEnabled(false); // Theme ID is not editable

        // Theme name input box
        txtThemeName = new GuiTextField(6, fontRenderer, width / 2 - 100, 100, 200, 20);
        txtThemeName.setMaxStringLength(50);

        // Create a color input field
        int fieldWidth = 100;
        int fieldHeight = 20;
        int labelWidth = 120;
        int x = width / 2 - 160;
        int colorFieldsStartY = 100;
        int y = colorFieldsStartY + 40;

        for (int i = 0; i < colorLabels.length; i++) {
            // Color input box
            colorFields[i] = new GuiTextField(20 + i, fontRenderer, x + labelWidth, y, fieldWidth, fieldHeight);
            colorFields[i].setMaxStringLength(8); // 8-digit hexadecimal

            y += 25;
        }

        // Add a padding input field
        y += 10;
        txtPadding = new GuiTextField(30, fontRenderer, x + labelWidth, y, fieldWidth, fieldHeight);
        txtPadding.setMaxStringLength(2);

        y += 25;
        txtCandidatePadding = new GuiTextField(31, fontRenderer, x + labelWidth, y, fieldWidth, fieldHeight);
        txtCandidatePadding.setMaxStringLength(2);

        y += 25;
        txtBorderWidth = new GuiTextField(32, fontRenderer, x + labelWidth, y, fieldWidth, fieldHeight);
        txtBorderWidth.setMaxStringLength(2);

        loadCurrentTheme();
    }

    private void loadThemeList() {
        themeIds.clear();
        Map<String, Theme> availableThemes = themeManager.getAvailableThemes();
        themeIds.addAll(availableThemes.keySet());

        // Find the index of the current theme
        int themeSelectionIndex = themeIds.indexOf(selectedThemeId);
        if (themeSelectionIndex == -1 && !themeIds.isEmpty()) {
            selectedThemeId = themeIds.get(0);
        }
    }

    private void loadCurrentTheme() {
        Theme theme = null;

        // First, try loading the selected theme ID.
        if (selectedThemeId != null && !selectedThemeId.isEmpty()) {
            theme = themeManager.getTheme(selectedThemeId);
        }

        // If no theme is selected, load the theme currently being applied.
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

        // Set color value (hexadecimal)
        colorFields[0].setText(String.format("%08X", theme.getTextColor()));
        colorFields[1].setText(String.format("%08X", theme.getBackgroundColor()));
        colorFields[2].setText(String.format("%08X", theme.getIndexColor()));
        colorFields[3].setText(String.format("%08X", theme.getSelectedBackgroundColor()));
        colorFields[4].setText(String.format("%08X", theme.getCursorColor()));
        colorFields[5].setText(String.format("%08X", theme.getBorderColor()));

        // Set fields such as padding
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

            themeManager.saveCustomTheme(theme);

            themeManager.setThemeAndNotify(selectedThemeId);

            loadThemeList();

            IngameIME_Forge.logDebugInfo("[ThemeEditor] Theme saved and applied: {} ({})", selectedThemeId, theme.getName());
        } catch (Exception e) {
            IngameIME_Forge.logDebugInfo("[ThemeEditor] Save failed: {}", e.getMessage());
        }
    }

    private int parseInt(String text, int defaultValue) {
        if (text == null || text.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private int parseColor(String hex) {
        if (hex == null || hex.isEmpty()) {
            return 0;
        }
        try {
            // Remove possible prefixes
            hex = hex.replace("#", "").replace("0x", "").replace("0X", "");
            return (int) Long.parseLong(hex, 16);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Called by ThemeSelectionGui to return the selected theme ID.
     */
    public void setSelectedThemeId(String themeId) {
        this.selectedThemeId = themeId;
        // Refresh editor fields with the newly selected theme data
        loadCurrentTheme();
    }

    /**
     * Returns the currently selected theme ID for the selection GUI to highlight.
     */
    public String getSelectedThemeId() {
        return this.selectedThemeId;
    }

    @Override
    protected void actionPerformed(@Nonnull GuiButton button) throws IOException {
        super.actionPerformed(button);

        if (button.id == 0) {
            themeManager.reloadThemes();
            mc.displayGuiScreen(parent);
        } else if (button.id == 1) {
            // Apply theme
            saveCurrentTheme();
        } else if (button.id == 2) {
            // Open the theme name and enter the GUI.
            nameInputGui.reset();
            mc.displayGuiScreen(nameInputGui);
        } else if (button.id == 3) {
            // Delete theme
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
            // Open the new selection GUI instead of cycling
            mc.displayGuiScreen(new ThemeSelectionGui(this));
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        // Calculate standard area (must be consistent with drawScreen)
        int scrollAreaTop = 80;
        int bottomAreaHeight = 50;
        int scrollAreaBottom = height - bottomAreaHeight;

        // --- Scrollbar click detection ---
        // Set detection range to 15 pixels on the right for easy clicking
        if (mouseX >= width - 15 && mouseX <= width && maxScrollOffset > 0) {
            if (mouseY >= scrollAreaTop && mouseY <= scrollAreaBottom) {
                this.isScrolling = true;
                // Update position immediately when clicked
                updateScrollFromMouse(mouseY);
                return; // Clicked on scrollbar, don't process text fields
            }
        }

        // --- Text field click detection ---
        // Only takes effect when clicking in scroll area (between Header and Footer)
        if (mouseY > scrollAreaTop && mouseY < scrollAreaBottom) {
            // Check if clicking on color preview
            boolean clickedPreview = false;
            for (int i = 0; i < colorLabels.length; i++) {
                if (previewX[i] > 0 && previewY[i] > 0) {
                    int px = previewX[i] - 1;
                    int py = previewY[i] - 1;
                    int previewSize = 20;
                    if (mouseX >= px && mouseX <= px + previewSize + 2 &&
                        mouseY >= py && mouseY <= py + previewSize + 2) {
                        // Open color picker
                        int currentColor = parseColor(colorFields[i].getText());
                        final int colorIndex = i;
                        mc.displayGuiScreen(new ColorPickerGui(this, currentColor, newColor -> {
                            // 1. 获取当前正在编辑的主题对象引用
                            Theme theme = themeManager.getTheme(selectedThemeId);
                            if (theme != null) {
                                // 2. 将新颜色同步到主题对象中
                                // 这样当 initGui -> loadCurrentTheme 运行重绘时，读取的就是新颜色
                                if (colorIndex == 0) theme.setTextColor(newColor);
                                else if (colorIndex == 1) theme.setBackgroundColor(newColor);
                                else if (colorIndex == 2) theme.setIndexColor(newColor);
                                else if (colorIndex == 3) theme.setSelectedBackgroundColor(newColor);
                                else if (colorIndex == 4) theme.setCursorColor(newColor);
                                else if (colorIndex == 5) theme.setBorderColor(newColor);
                            }
                            // 注意：此处不需要手动更新 colorFields[].setText，
                            // 因为 loadCurrentTheme() 会自动根据 theme 对象刷新 UI。
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

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.isScrolling = false;
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

        if (this.isScrolling && clickedMouseButton == 0) {
            updateScrollFromMouse(mouseY);
        }
    }

    /**
     * Calculate and update scroll offset based on mouse Y coordinate
     */
    private void updateScrollFromMouse(int mouseY) {
        int scrollAreaTop = 80;
        int bottomAreaHeight = 50;
        int scrollAreaBottom = height - bottomAreaHeight;
        int trackHeight = scrollAreaBottom - scrollAreaTop;

        // Calculate mouse percentage in track (0.0 ~ 1.0)
        float f = (float)(mouseY - scrollAreaTop) / (float)trackHeight;

        // Map to scroll offset
        // Subtracting viewportHeight/2 makes the thumb center follow the mouse, closest to vanilla feel
        this.scrollOffset = (int)(f * (float)contentHeight) - (viewportHeight / 2);

        // Limit range
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, maxScrollOffset));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        txtThemeName.textboxKeyTyped(typedChar, keyCode);
        for (GuiTextField field : colorFields) {
            if (field != null) {
                field.textboxKeyTyped(typedChar, keyCode);
            }
        }

        // Handle keyboard input for padding-related text fields
        if (txtPadding != null) txtPadding.textboxKeyTyped(typedChar, keyCode);
        if (txtCandidatePadding != null) txtCandidatePadding.textboxKeyTyped(typedChar, keyCode);
        if (txtBorderWidth != null) txtBorderWidth.textboxKeyTyped(typedChar, keyCode);

        updateColorPreviews();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Draw the tiled dirt background for the whole screen
        this.drawBackground(0);

        // Define Slot-style boundaries
        // Header height increased to 80 to cover top buttons
        int labelWidth = 120;
        int scrollAreaTop = 80;
        int bottomAreaHeight = 45;
        int scrollAreaBottom = height - bottomAreaHeight;

        // Update scrolling parameters
        viewportHeight = scrollAreaBottom - scrollAreaTop;
        contentHeight = 305; // Adjust based on your content
        maxScrollOffset = Math.max(0, contentHeight - viewportHeight);
        scrollOffset = Math.min(scrollOffset, maxScrollOffset);

        // Render Scrollable Content (Inside the Scissor box)
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        int scaleFactor = new ScaledResolution(mc).getScaleFactor();
        // The Scissor Y is measured from the bottom of the screen
        GL11.glScissor(0, bottomAreaHeight * scaleFactor, width * scaleFactor, viewportHeight * scaleFactor);

        int x = width / 2 - 150;
        // Content starts below the header
        int startY = scrollAreaTop + 10;
        int currentY = startY - scrollOffset;

        // Draw Theme Name Field
        txtThemeName.y = currentY;
        txtThemeName.drawTextBox();
        currentY += 30;

        // Draw Colors and Labels
        int previewX = x + labelWidth + 110;
        int previewSize = 20;

        for (int i = 0; i < colorLabels.length; i++) {
            fontRenderer.drawString(colorLabels[i], x, currentY + 5, 0xFFFFFF);
            if (colorFields[i] != null) {
                colorFields[i].y = currentY;
                colorFields[i].drawTextBox();
            }
            // Save preview position for click detection
            this.previewX[i] = previewX;
            this.previewY[i] = currentY;
            // Draw Preview Rects
            drawRect(previewX - 1, currentY - 1, previewX + previewSize + 1, currentY + previewSize + 1, 0xFF000000);
            drawRect(previewX, currentY, previewX + previewSize, currentY + previewSize, previewColors[i]);
            // Draw click hint
            fontRenderer.drawString("🖱", previewX + previewSize + 3, currentY + 5, 0xAAAAAA);
            currentY += 25;
        }

        // Additional Fields (Padding, etc.)
        currentY += 10;
        fontRenderer.drawString(I18n.format("ingameime.theme.editor.padding"), x, currentY + 5, 0xFFFFFF);
        if (txtPadding != null) { txtPadding.y = currentY; txtPadding.drawTextBox(); }
        currentY += 25;
        fontRenderer.drawString(I18n.format("ingameime.theme.editor.candidate_padding"), x, currentY + 5, 0xFFFFFF);
        if (txtCandidatePadding != null) { txtCandidatePadding.y = currentY; txtCandidatePadding.drawTextBox(); }
        currentY += 25;
        fontRenderer.drawString(I18n.format("ingameime.theme.editor.border_width"), x, currentY + 5, 0xFFFFFF);
        if (txtBorderWidth != null) { txtBorderWidth.y = currentY; txtBorderWidth.drawTextBox(); }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // Draw Overlays (Standard Header and Footer)
        // These cover the scrolling content to keep the buttons and title clean
        this.overlayBackground(0, scrollAreaTop); // Top Header
        this.overlayBackground(scrollAreaBottom, height); // Bottom Footer

        // Draw Shadows (Gradient effect at the edges of scroll area)
        this.drawGradientRect(0, scrollAreaTop, width, scrollAreaTop + 4, 0xFF000000, 0x00000000);
        this.drawGradientRect(0, scrollAreaBottom - 4, width, scrollAreaBottom, 0x00000000, 0xFF000000);

        // Draw Static UI Elements (Title and Info)
        drawCenteredString(fontRenderer, I18n.format("ingameime.theme.editor.title"), width / 2, 10, 0xFFFFFF);
        // "Current Theme" text - placed inside the header area
        fontRenderer.drawString(I18n.format("ingameime.theme.editor.current") + ": " + selectedThemeId, width / 2 + 5, 56, 0xFFFFFF);

        // Draw Scrollbar
        if (maxScrollOffset > 0) {
            drawScrollBar(scrollAreaTop, scrollAreaBottom);
        }

        // Draw Buttons (Fixed positions)
        for (net.minecraft.client.gui.GuiButton button : buttonList) {
            button.drawButton(mc, mouseX, mouseY, partialTicks);
        }
    }

    /**
     * Helper to draw the standard darkened tiled dirt background for overlays.
     */
    protected void overlayBackground(int startY, int endY) {
        net.minecraft.client.renderer.Tessellator tessellator = net.minecraft.client.renderer.Tessellator.getInstance();
        net.minecraft.client.renderer.BufferBuilder bufferbuilder = tessellator.getBuffer();
        this.mc.getTextureManager().bindTexture(net.minecraft.client.gui.Gui.OPTIONS_BACKGROUND);
        org.lwjgl.opengl.GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        bufferbuilder.begin(7, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(0.0D, endY, 0.0D).tex(0.0D, (float)endY / 32.0F).color(64, 64, 64, 255).endVertex();
        bufferbuilder.pos(this.width, endY, 0.0D).tex((float)this.width / 32.0F, (float)endY / 32.0F).color(64, 64, 64, 255).endVertex();
        bufferbuilder.pos(this.width, startY, 0.0D).tex((float)this.width / 32.0F, (float)startY / 32.0F).color(64, 64, 64, 255).endVertex();
        bufferbuilder.pos(0.0D, startY, 0.0D).tex(0.0D, (float)startY / 32.0F).color(64, 64, 64, 255).endVertex();
        tessellator.draw();
    }

    private void drawScrollBar(int top, int bottom) {
        int scrollBarX = width - 6;
        int scrollBarHeight = bottom - top;
        int thumbHeight = Math.max(20, (int) ((float) scrollBarHeight / contentHeight * scrollBarHeight));
        int thumbY = top + (int) ((float) scrollOffset / maxScrollOffset * (scrollBarHeight - thumbHeight));

        // Background of the scrollbar (Dark grey)
        drawRect(scrollBarX, top, scrollBarX + 6, bottom, 0xFF000000);
        // Thumb of the scrollbar (Light grey)
        drawRect(scrollBarX, thumbY, scrollBarX + 6, thumbY + thumbHeight, 0xFF808080);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int scroll = Mouse.getEventDWheel();
        if (scroll != 0) {
            scroll = scroll > 0 ? -20 : 20; // Reverse direction, scroll 20 pixels per tick
            scrollOffset = Math.max(0, Math.min(scrollOffset + scroll, maxScrollOffset));
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        txtThemeName.updateCursorCounter();
        for (GuiTextField field : colorFields) {
            if (field != null) {
                field.updateCursorCounter();
            }
        }

        // Update cursor for padding-related text fields
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

    /**
     * Generate safe theme ID from theme name
     */
    private String generateThemeIdFromName(String themeName) {
        if (themeName == null || themeName.isEmpty()) {
            return "custom_theme";
        }

        // Convert to lowercase, replace spaces with underscores, remove illegal characters
        String safeId = themeName.toLowerCase()
            .replaceAll("\\s+", "_")
            .replaceAll("[^a-z0-9_]", "");

        // If ID is empty or too short, add prefix
        if (safeId.length() < 2) {
            safeId = "custom_theme";
        }

        // Ensure ID starts with a letter
        if (!Character.isLetter(safeId.charAt(0))) {
            safeId = "theme_" + safeId;
        }

        return safeId;
    }
}
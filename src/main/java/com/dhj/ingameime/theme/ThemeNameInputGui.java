package com.dhj.ingameime.theme;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Theme Name Input GUI
 */
public class ThemeNameInputGui extends GuiScreen {
    private final GuiScreen parent;
    private GuiTextField txtThemeName;

    private String themeName = "";
    private boolean confirmed = false;
    
    public ThemeNameInputGui(GuiScreen parent) {
        this.parent = parent;
    }
    
    @Override
    public void initGui() {
        super.initGui();
        
        // Add confirmation button
        GuiButton btnConfirm = new GuiButton(0, width / 2 - 155, height / 2 + 30, 150, 20, I18n.format("gui.done"));
        buttonList.add(btnConfirm);
        
        // Add cancel button
        GuiButton btnCancel = new GuiButton(1, width / 2 + 5, height / 2 + 30, 150, 20, I18n.format("gui.cancel"));
        buttonList.add(btnCancel);
        
        // Theme name input box
        txtThemeName = new GuiTextField(2, fontRenderer, width / 2 - 100, height / 2 - 10, 200, 20);
        txtThemeName.setMaxStringLength(50);
        txtThemeName.setFocused(true);
    }
    
    @Override
    protected void actionPerformed(@Nonnull GuiButton button) throws IOException {
        super.actionPerformed(button);
        
        if (button.id == 0) {
            // Confirm
            themeName = txtThemeName.getText().trim();
            if (!themeName.isEmpty()) {
                confirmed = true;
                mc.displayGuiScreen(parent);
            }
        } else if (button.id == 1) {
            // Cancel
            confirmed = false;
            mc.displayGuiScreen(parent);
        }
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        txtThemeName.mouseClicked(mouseX, mouseY, mouseButton);
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        txtThemeName.textboxKeyTyped(typedChar, keyCode);
        
        // Press Enter to confirm.
        if (keyCode == 28) { // Enter key
            themeName = txtThemeName.getText().trim();
            if (!themeName.isEmpty()) {
                confirmed = true;
                mc.displayGuiScreen(parent);
            }
        }
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        
        // Title
        drawCenteredString(fontRenderer, I18n.format("ingameime.theme.editor.input_title"), width / 2, height / 2 - 50, 0xFFFFFF);
        
        // Prompt text
        drawCenteredString(fontRenderer, I18n.format("ingameime.theme.editor.input_hint"), width / 2, height / 2 - 30, 0xAAAAAA);
        
        // Draw input box
        txtThemeName.drawTextBox();
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    @Override
    public void updateScreen() {
        super.updateScreen();
        txtThemeName.updateCursorCounter();
    }
    
    /**
     * Get the input theme name
     */
    public String getThemeName() {
        return themeName;
    }
    
    /**
     * Is it confirmed?
     */
    public boolean isConfirmed() {
        return confirmed;
    }
    
    /**
     * Reset state
     */
    public void reset() {
        themeName = "";
        confirmed = false;
    }
}
package com.dhj.ingameime.theme;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

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

        GuiButton btnConfirm = new GuiButton(0, width / 2 - 155, height / 2 + 30, 150, 20, I18n.format("gui.done"));
        buttonList.add(btnConfirm);

        GuiButton btnCancel = new GuiButton(1, width / 2 + 5, height / 2 + 30, 150, 20, I18n.format("gui.cancel"));
        buttonList.add(btnCancel);

        txtThemeName = new GuiTextField(mc.fontRenderer, width / 2 - 100, height / 2 - 10, 200, 20);
        txtThemeName.setMaxStringLength(50);
        txtThemeName.setFocused(true);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            themeName = txtThemeName.getText().trim();
            if (!themeName.isEmpty()) {
                confirmed = true;
                mc.displayGuiScreen(parent);
            }
        } else if (button.id == 1) {
            confirmed = false;
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        txtThemeName.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
        txtThemeName.textboxKeyTyped(typedChar, keyCode);

        if (keyCode == 28) {
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

        drawCenteredString(mc.fontRenderer, I18n.format("ingameime.theme.editor.input_title"), width / 2, height / 2 - 50, 0xFFFFFF);
        drawCenteredString(mc.fontRenderer, I18n.format("ingameime.theme.editor.input_hint"), width / 2, height / 2 - 30, 0xAAAAAA);

        txtThemeName.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        txtThemeName.updateCursorCounter();
    }

    public String getThemeName() {
        return themeName;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void reset() {
        themeName = "";
        confirmed = false;
    }
}

package com.dhj.ingameime.theme;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

import java.io.IOException;

/**
 * 主题名称输入GUI
 */
public class ThemeNameInputGui extends GuiScreen {
    private GuiScreen parent;
    private GuiTextField txtThemeName;
    private GuiButton btnConfirm;
    private GuiButton btnCancel;
    
    private String themeName = "";
    private boolean confirmed = false;
    
    public ThemeNameInputGui(GuiScreen parent) {
        this.parent = parent;
    }
    
    @Override
    public void initGui() {
        super.initGui();
        
        // 添加确认按钮
        btnConfirm = new GuiButton(0, width / 2 - 155, height / 2 + 30, 150, 20, I18n.format("gui.done"));
        buttonList.add(btnConfirm);
        
        // 添加取消按钮
        btnCancel = new GuiButton(1, width / 2 + 5, height / 2 + 30, 150, 20, I18n.format("gui.cancel"));
        buttonList.add(btnCancel);
        
        // 主题名称输入框
        txtThemeName = new GuiTextField(2, fontRenderer, width / 2 - 100, height / 2 - 10, 200, 20);
        txtThemeName.setMaxStringLength(50);
        txtThemeName.setFocused(true);
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        
        if (button.id == 0) {
            // 确认
            themeName = txtThemeName.getText().trim();
            if (!themeName.isEmpty()) {
                confirmed = true;
                mc.displayGuiScreen(parent);
            }
        } else if (button.id == 1) {
            // 取消
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
        
        // 按回车键确认
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
        
        // 标题
        drawCenteredString(fontRenderer, I18n.format("ingameime.theme.editor.input_title"), width / 2, height / 2 - 50, 0xFFFFFF);
        
        // 提示文字
        drawCenteredString(fontRenderer, I18n.format("ingameime.theme.editor.input_hint"), width / 2, height / 2 - 30, 0xAAAAAA);
        
        // 绘制输入框
        txtThemeName.drawTextBox();
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    @Override
    public void updateScreen() {
        super.updateScreen();
        txtThemeName.updateCursorCounter();
    }
    
    /**
     * 获取输入的主题名称
     */
    public String getThemeName() {
        return themeName;
    }
    
    /**
     * 是否已确认
     */
    public boolean isConfirmed() {
        return confirmed;
    }
    
    /**
     * 重置状态
     */
    public void reset() {
        themeName = "";
        confirmed = false;
    }
}
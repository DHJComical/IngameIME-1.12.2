package com.dhj.ingameime.theme;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiConfig;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.Map;

/**
 * 主题编辑器GUI
 */
public class ThemeEditorGui extends GuiScreen {
    private GuiScreen parent;
    private ThemeManager themeManager;
    private GuiButton btnBack;
    private GuiButton btnApply;
    private GuiButton btnCreateNew;
    private GuiButton btnDelete;
    private GuiButton btnSelectTheme;
    private GuiTextField txtThemeName;
    private GuiTextField txtThemeId;
    private String selectedThemeId = "default";
    
    private int colorFieldsStartY = 100;
    private GuiTextField[] colorFields = new GuiTextField[10];
    private String[] colorLabels = {
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
    
    private java.util.List<String> themeIds = new java.util.ArrayList<>();
    private int themeSelectionIndex = 0;
    private ThemeNameInputGui nameInputGui;
    
    // 滚动相关
    private int scrollOffset = 0;
    private int maxScrollOffset = 0;
    private int contentHeight = 400; // 内容总高度
    private int viewportHeight = 200; // 可视区域高度
    private boolean isScrolling = false;
    private int scrollBarDragStartY = 0; // 拖动开始时的鼠标Y坐标
    private int scrollBarDragStartOffset = 0; // 拖动开始时的滚动偏移
    
    public ThemeEditorGui(GuiScreen parent) {
        this.parent = parent;
        this.themeManager = ThemeManager.getInstance();
        this.nameInputGui = new ThemeNameInputGui(this);
    }
    
    @Override
    public void initGui() {
        super.initGui();
        
        // 检查是否从主题名称输入GUI返回并确认
        if (nameInputGui.isConfirmed()) {
            String themeName = nameInputGui.getThemeName();
            if (!themeName.isEmpty()) {
                String newThemeId = "custom_" + System.currentTimeMillis();
                Theme newTheme = Theme.createCustomTheme(newThemeId, themeName);
                themeManager.saveCustomTheme(newTheme);
                selectedThemeId = newThemeId;
            }
        }
        
        // 加载可用主题列表
        loadThemeList();
        
        // 添加返回按钮
        btnBack = new GuiButton(0, width / 2 - 155, height - 29, 150, 20, I18n.format("gui.back"));
        buttonList.add(btnBack);
        
        // 添加应用按钮
        btnApply = new GuiButton(1, width / 2 + 5, height - 29, 150, 20, I18n.format("ingameime.theme.editor.apply"));
        buttonList.add(btnApply);
        
        // 添加创建新主题按钮
        btnCreateNew = new GuiButton(2, width / 2 - 155, 25, 150, 20, I18n.format("ingameime.theme.editor.create"));
        buttonList.add(btnCreateNew);
        
        // 添加删除主题按钮
        btnDelete = new GuiButton(3, width / 2 + 5, 25, 150, 20, I18n.format("ingameime.theme.editor.delete"));
        buttonList.add(btnDelete);
        
        // 添加选择主题按钮
        btnSelectTheme = new GuiButton(4, width / 2 - 155, 50, 150, 20, I18n.format("ingameime.theme.editor.select"));
        buttonList.add(btnSelectTheme);
        
        // 主题ID输入框
        txtThemeId = new GuiTextField(5, fontRenderer, width / 2 - 100, 75, 200, 20);
        txtThemeId.setMaxStringLength(50);
        txtThemeId.setEnabled(false); // 主题ID不可编辑
        
        // 主题名称输入框
        txtThemeName = new GuiTextField(6, fontRenderer, width / 2 - 100, 100, 200, 20);
        txtThemeName.setMaxStringLength(50);
        
        // 创建颜色输入字段
        int fieldWidth = 100;
        int fieldHeight = 20;
        int labelWidth = 80;
        int x = width / 2 - 150;
        int y = colorFieldsStartY + 40;
        
        for (int i = 0; i < colorLabels.length; i++) {
            // 颜色输入框
            colorFields[i] = new GuiTextField(20 + i, fontRenderer, x + labelWidth, y, fieldWidth, fieldHeight);
            colorFields[i].setMaxStringLength(8); // 8位十六进制
            
            y += 25;
        }
        
        // 添加padding输入字段
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
        
        // 找到当前主题的索引
        themeSelectionIndex = themeIds.indexOf(selectedThemeId);
        if (themeSelectionIndex == -1 && !themeIds.isEmpty()) {
            themeSelectionIndex = 0;
            selectedThemeId = themeIds.get(0);
        }
    }
    
    private void loadCurrentTheme() {
        Theme theme = null;
        
        // 如果有选中的主题ID（比如新创建的主题），优先加载该主题
        if (selectedThemeId != null && !selectedThemeId.isEmpty()) {
            theme = themeManager.getTheme(selectedThemeId);
        }
        
        // 如果没有选中的主题，加载当前应用的主题
        if (theme == null) {
            theme = themeManager.getCurrentTheme();
        }
        
        if (theme != null) {
            selectedThemeId = theme.getId();
            loadThemeToEditor(theme);
        }
    }
    
    private void loadThemeToEditor(Theme theme) {
        if (theme == null) return;
        
        selectedThemeId = theme.getId();
        txtThemeId.setText(theme.getId());
        txtThemeName.setText(theme.getName());
        
        // 设置颜色值（十六进制）
        colorFields[0].setText(String.format("%08X", theme.getTextColor()));
        colorFields[1].setText(String.format("%08X", theme.getBackgroundColor()));
        colorFields[2].setText(String.format("%08X", theme.getIndexColor()));
        colorFields[3].setText(String.format("%08X", theme.getSelectedBackgroundColor()));
        colorFields[4].setText(String.format("%08X", theme.getCursorColor()));
        colorFields[5].setText(String.format("%08X", theme.getBorderColor()));
        
        // 设置padding等字段
        txtPadding.setText(String.valueOf(theme.getPadding()));
        txtCandidatePadding.setText(String.valueOf(theme.getCandidatePadding()));
        txtBorderWidth.setText(String.valueOf(theme.getBorderWidth()));
    }
    
    private void saveCurrentTheme() {
        try {
            String name = txtThemeName.getText();
            if (name.isEmpty()) {
                name = selectedThemeId;
            }
            
            // 解析颜色值
            int textColor = parseColor(colorFields[0].getText(), 0xFF000000);
            int backgroundColor = parseColor(colorFields[1].getText(), 0xEBEBEBEB);
            int indexColor = parseColor(colorFields[2].getText(), 0xFF555555);
            int selectedBgColor = parseColor(colorFields[3].getText(), 0xEBEBEBEB);
            int cursorColor = parseColor(colorFields[4].getText(), 0xFF000000);
            int borderColor = parseColor(colorFields[5].getText(), 0x80000000);
            
            // 解析padding等字段
            int padding = parseInt(txtPadding.getText(), 3);
            int candidatePadding = parseInt(txtCandidatePadding.getText(), 5);
            int borderWidth = parseInt(txtBorderWidth.getText(), 1);
            
            // 创建或更新主题
            Theme theme = new Theme(
                selectedThemeId,
                name,
                textColor,
                backgroundColor,
                indexColor,
                selectedBgColor,
                cursorColor,
                padding,
                candidatePadding,
                borderWidth,
                borderColor
            );
            
            themeManager.saveCustomTheme(theme);
            themeManager.setThemeAndNotify(selectedThemeId);
            
        } catch (NumberFormatException e) {
            // 颜色格式错误，忽略
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
    
    private int parseColor(String hex, int defaultValue) {
        if (hex == null || hex.isEmpty()) {
            return defaultValue;
        }
        try {
            // 移除可能的前缀
            hex = hex.replace("#", "").replace("0x", "").replace("0X", "");
            return (int) Long.parseLong(hex, 16);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        
        if (button.id == 0) {
            // 返回
            mc.displayGuiScreen(parent);
        } else if (button.id == 1) {
            // 应用主题
            saveCurrentTheme();
        } else if (button.id == 2) {
            // 打开主题名称输入GUI
            nameInputGui.reset();
            mc.displayGuiScreen(nameInputGui);
        } else if (button.id == 3) {
            // 删除主题
            if (!selectedThemeId.equals("default") && 
                !selectedThemeId.equals("dark") && 
                !selectedThemeId.equals("light")) {
                themeManager.deleteCustomTheme(selectedThemeId);
                selectedThemeId = "default";
                loadThemeList();
                loadCurrentTheme();
            }
        } else if (button.id == 4) {
            // 选择主题 - 循环选择下一个主题
            if (!themeIds.isEmpty()) {
                themeSelectionIndex = (themeSelectionIndex + 1) % themeIds.size();
                selectedThemeId = themeIds.get(themeSelectionIndex);
                themeManager.setThemeAndNotify(selectedThemeId);
                loadCurrentTheme();
            }
        }
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        
        // 检查是否点击了滚动条
        if (maxScrollOffset > 0 && mouseButton == 0) {
            int scrollBarX = width - 20;
            int scrollBarY = 80;
            int scrollBarHeight = viewportHeight;
            int thumbHeight = Math.max(20, (int) ((float) viewportHeight / contentHeight * scrollBarHeight));
            int thumbY = scrollBarY + (int) ((float) scrollOffset / maxScrollOffset * (scrollBarHeight - thumbHeight));
            
            // 检查是否点击了滚动条滑块
            if (mouseX >= scrollBarX && mouseX <= scrollBarX + 6 &&
                mouseY >= thumbY && mouseY <= thumbY + thumbHeight) {
                isScrolling = true;
                scrollBarDragStartY = mouseY;
                scrollBarDragStartOffset = scrollOffset;
            }
            // 检查是否点击了滚动条背景（跳转到该位置）
            else if (mouseX >= scrollBarX && mouseX <= scrollBarX + 6 &&
                     mouseY >= scrollBarY && mouseY <= scrollBarY + scrollBarHeight) {
                // 计算点击位置对应的滚动偏移
                float ratio = (float) (mouseY - scrollBarY) / scrollBarHeight;
                scrollOffset = (int) (ratio * maxScrollOffset);
                scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
            }
        }
        
        // 调整鼠标Y坐标以考虑滚动偏移
        int adjustedMouseY = mouseY + scrollOffset;
        
        txtThemeName.mouseClicked(mouseX, adjustedMouseY, mouseButton);
        for (GuiTextField field : colorFields) {
            if (field != null) {
                field.mouseClicked(mouseX, adjustedMouseY, mouseButton);
            }
        }
    }
    
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        isScrolling = false;
    }
    
    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        
        // 处理滚动条拖动
        if (isScrolling && clickedMouseButton == 0) {
            int scrollBarHeight = viewportHeight;
            int thumbHeight = Math.max(20, (int) ((float) viewportHeight / contentHeight * scrollBarHeight));
            int dragDelta = mouseY - scrollBarDragStartY;
            float dragRatio = (float) dragDelta / (scrollBarHeight - thumbHeight);
            scrollOffset = scrollBarDragStartOffset + (int) (dragRatio * maxScrollOffset);
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
        }
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
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        
        // 计算滚动参数
        viewportHeight = height - 100; // 可视区域高度（减去顶部和底部按钮空间）
        contentHeight = 350; // 内容总高度
        maxScrollOffset = Math.max(0, contentHeight - viewportHeight);
        scrollOffset = Math.min(scrollOffset, maxScrollOffset);
        
        // 启用裁剪区域，只绘制可视区域
        int clipX = 0;
        int clipY = 80; // 从标题下方开始
        int clipWidth = width;
        int clipHeight = viewportHeight;
        
        // 标题（不滚动）
        drawCenteredString(fontRenderer, I18n.format("ingameime.theme.editor.title"), width / 2, 10, 0xFFFFFF);
        
        // 当前主题标签（不滚动）
        fontRenderer.drawString(I18n.format("ingameime.theme.editor.current") + ": " + selectedThemeId, width / 2 - 100, 35, 0xFFFFFF);
        
        // 使用GL裁剪
        net.minecraft.client.renderer.GlStateManager.pushMatrix();
        net.minecraft.client.renderer.GlStateManager.translate(0, -scrollOffset, 0);
        
        // 绘制可滚动内容
        int labelWidth = 80;
        int x = width / 2 - 150;
        int y = colorFieldsStartY + 40;
        
        // 绘制颜色标签
        for (int i = 0; i < colorLabels.length; i++) {
            fontRenderer.drawString(colorLabels[i], x, y + 5, 0xFFFFFF);
            y += 25;
        }
        
        // 绘制其他标签
        y += 10;
        fontRenderer.drawString(I18n.format("ingameime.theme.editor.padding"), x, y + 5, 0xFFFFFF);
        y += 25;
        fontRenderer.drawString(I18n.format("ingameime.theme.editor.candidate_padding"), x, y + 5, 0xFFFFFF);
        y += 25;
        fontRenderer.drawString(I18n.format("ingameime.theme.editor.border_width"), x, y + 5, 0xFFFFFF);
        
        // 绘制输入框（考虑滚动）
        txtThemeName.drawTextBox();
        for (GuiTextField field : colorFields) {
            if (field != null) {
                field.drawTextBox();
            }
        }
        
        // 绘制颜色预览
        int previewX = width / 2 + 100;
        int previewY = colorFieldsStartY + 40;
        int previewSize = 20;
        
        for (int i = 0; i < colorLabels.length; i++) {
            try {
                int color = parseColor(colorFields[i].getText(), 0x00000000);
                drawRect(previewX, previewY, previewX + previewSize, previewY + previewSize, color);
                drawRect(previewX - 1, previewY - 1, previewX + previewSize + 1, previewY + previewSize + 1, 0xFF000000);
            } catch (Exception ignored) {
            }
            previewY += 25;
        }
        
        GlStateManager.popMatrix();
        
        // 绘制滚动条
        if (maxScrollOffset > 0) {
            drawScrollBar();
        }
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    private void drawScrollBar() {
        int scrollBarX = width - 20;
        int scrollBarY = 80;
        int scrollBarHeight = viewportHeight;
        int thumbHeight = Math.max(20, (int) ((float) viewportHeight / contentHeight * scrollBarHeight));
        int thumbY = scrollBarY + (int) ((float) scrollOffset / maxScrollOffset * (scrollBarHeight - thumbHeight));
        
        // 绘制滚动条背景
        drawRect(scrollBarX, scrollBarY, scrollBarX + 6, scrollBarY + scrollBarHeight, 0x80333333);
        
        // 绘制滚动条滑块
        drawRect(scrollBarX, thumbY, scrollBarX + 6, thumbY + thumbHeight, 0xFFAAAAAA);
    }
    
    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        
        int scroll = Mouse.getEventDWheel();
        if (scroll != 0) {
            scroll = scroll > 0 ? -20 : 20; // 反转方向，每次滚动20像素
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
    }
}
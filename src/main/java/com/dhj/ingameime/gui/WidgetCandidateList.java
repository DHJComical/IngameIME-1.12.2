package com.dhj.ingameime.gui;

import com.dhj.ingameime.IngameIME_Forge;
import com.dhj.ingameime.theme.api.Theme;
import com.dhj.ingameime.theme.api.ThemeManager;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.Objects;

// 从 1.17 的 IngameIME 移植个人认为更好看的 UI (已适配 1.12.2)
public class WidgetCandidateList extends Widget {
    private final CandidateEntry drawItem = new CandidateEntry();
    private List<String> Candidates = null;
    private int Selected = -1;

    public WidgetCandidateList() {
        DrawInline = false;
        updateThemeColors();
    }

    @Override
    protected void updateThemeColors() {
        super.updateThemeColors();
        Theme theme = ThemeManager.getInstance().getCurrentTheme();
        if (theme != null) {
            Padding = theme.getCandidatePadding();
        }
    }

    public void setContent(List<String> candidates, int selected) {
        // Check if content or selection changed to avoid spam
        boolean changed = !Objects.equals(this.Candidates, candidates) || this.Selected != selected;

        if (changed) {
            IngameIME_Forge.logDebugInfo("[Java Candidates] Received: {} items, selected={}", candidates != null ? candidates.size() : 0, selected);
            if (candidates != null) {
                for (int i = 0; i < candidates.size(); i++) {
                    IngameIME_Forge.logDebugInfo("[Java Candidates]   [{}] {}", i, candidates.get(i));
                }
            }
        }
        
        Candidates = candidates;
        Selected = selected;
        isDirty = true;
    }

    @Override
    public boolean isActive() {
        return Candidates != null && !Candidates.isEmpty();
    }

    @Override
    public void layout() {
        if (!isDirty) return;
        Height = Width = 0;
        if (!isActive()) {
            isDirty = false;
            return;
        }

        // Total height equals entry content height; panel padding added by base Widget
        Height = drawItem.getTotalHeight();

        // Width is the sum of all entry widths
        int total = 0;
        int index = 1;
        for (String s : Candidates) {
            drawItem.setIndex(index++);
            drawItem.setText(s);
            total += drawItem.getTotalWidth();
        }
        Width = total;

        super.layout();
    }

    @Override
    public void draw() {
        if (!isActive()) return;
        if (isDirty) layout();

        super.draw();

        Theme theme = ThemeManager.getInstance().getCurrentTheme();
        int drawX = X + Padding;
        int drawY = Y + Padding;
        int index = 1;
        for (String s : Candidates) {
            drawItem.setIndex(index++);
            drawItem.setText(s);

            boolean isSelected = (index - 1) == (Selected + 1);
            if (isSelected && theme != null) {
                int entryWidth = drawItem.getTotalWidth();
                drawRect(drawX, Y, drawX + entryWidth, Y + Height + (Padding * 2), theme.getSelectedBackgroundColor());
            }

            drawItem.draw(drawX, drawY, TextColor, theme);
            drawX += drawItem.getTotalWidth();
        }
    }

    private static final class CandidateEntry {
        private final Minecraft mc = Minecraft.getMinecraft();
        private String text = null;
        private int index = 0;

        // Index area width equals width of "00" + 5 in 1.17
        private int getIndexAreaWidth() {
            return mc.fontRenderer.getStringWidth("00") + 5;
        }

        void setText(String text) {
            this.text = text;
        }

        void setIndex(int index) {
            this.index = index;
        }

        int getTextWidth() {
            return mc.fontRenderer.getStringWidth(text);
        }

        int getContentHeight() {
            return mc.fontRenderer.FONT_HEIGHT;
        }

        int getTotalWidth() {
            // 改为类似�?1.17 �?padding
            return 2 + getIndexAreaWidth() + getTextWidth() + 2;
        }

        int getTotalHeight() {
            return getContentHeight();
        }

        void draw(int x, int y, int textColor, Theme theme) {
            // 改为类似�?1.17 �?padding
            int offsetX = x + 2;

            String idx = Integer.toString(index);
            int indexAreaW = getIndexAreaWidth();
            int idxTextW = mc.fontRenderer.getStringWidth(idx);
            int centeredX = offsetX + (indexAreaW - idxTextW) / 2;

            int indexColor = (theme != null) ? theme.getIndexColor() : 0xFF555555;
            mc.fontRenderer.drawString(idx, centeredX, y, indexColor);

            // 渲染 text
            offsetX += indexAreaW;
            mc.fontRenderer.drawString(text, offsetX, y, textColor);
        }
    }

    @Override
    protected String getComponentId() {
        return "candidate";
    }
}

package com.dhj.ingameime.gui;

import net.minecraft.client.Minecraft;
import java.util.List;

// 从1.17的 IngameIME 移植个人认为更好看的 UI (已适配1.12.2)
public class WidgetCandidateList extends Widget {
    private List<String> Candidates = null;
    private int Selected = -1;

    private final CandidateEntry drawItem = new CandidateEntry();

    WidgetCandidateList() {
        Padding = 3;
        DrawInline = false;
    }

    public void setContent(List<String> candidates, int selected) {
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

        int drawX = X + Padding;
        int drawY = Y + Padding;
        int index = 1;
        for (String s : Candidates) {
            drawItem.setIndex(index++);
            drawItem.setText(s);

            boolean isSelected = (index - 1) == (Selected + 1);
            if (isSelected) {
                int entryWidth = drawItem.getTotalWidth();
                drawRect(drawX, Y, drawX + entryWidth, Y + Height + (Padding * 2), 0xEB_B2DAE0);
            }

            drawItem.draw(drawX, drawY, TextColor);
            drawX += drawItem.getTotalWidth();
        }
    }

    private static final class CandidateEntry {
        private String text = null;
        private int index = 0;

        private final Minecraft mc = Minecraft.getMinecraft();

        // Index area width equals width of "00" + 5 in 1.17
        private int getIndexAreaWidth() {
            return mc.fontRenderer.getStringWidth("00") + 5;
        }

        void setText(String text) { this.text = text; }
        void setIndex(int index) { this.index = index; }

        int getTextWidth() {
            return mc.fontRenderer.getStringWidth(text);
        }

        int getContentHeight() {
            return mc.fontRenderer.FONT_HEIGHT;
        }

        int getTotalWidth() {
            // 改为类似于 1.17 的 padding
            return 2 + getIndexAreaWidth() + getTextWidth() + 2;
        }

        int getTotalHeight() {
            return getContentHeight();
        }

        void draw(int x, int y, int textColor) {
            // 改为类似于 1.17 的 padding
            int offsetX = x + 2;
            int baselineY = y;

            String idx = Integer.toString(index);
            int indexAreaW = getIndexAreaWidth();
            int idxTextW = mc.fontRenderer.getStringWidth(idx);
            int centeredX = offsetX + (indexAreaW - idxTextW) / 2;
            mc.fontRenderer.drawString(idx, centeredX, baselineY, 0xFF555555);

            // 渲染text
            offsetX += indexAreaW;
            mc.fontRenderer.drawString(text, offsetX, baselineY, textColor);
        }
    }
}
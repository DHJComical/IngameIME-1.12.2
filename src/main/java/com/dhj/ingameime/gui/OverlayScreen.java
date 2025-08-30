package com.dhj.ingameime.gui;

import com.dhj.ingameime.Internal;
import ingameime.InputContext;
import net.minecraft.client.renderer.GlStateManager;

public class OverlayScreen extends Widget {
    public WidgetPreEdit PreEdit = new WidgetPreEdit();
    public WidgetCandidateList CandidateList = new WidgetCandidateList();
    public WidgetInputMode WInputMode = new WidgetInputMode();

    @Override
    public boolean isActive() {
        InputContext inputCtx = Internal.InputCtx;
        return inputCtx != null && inputCtx.getActivated();
    }

    @Override
    public void layout() {
        // Container does not need layout
    }

    @Override
    public void draw() {
        if (!isActive()) return;

        GlStateManager.pushMatrix();

        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.translate(0, 0, 30f);

        PreEdit.draw();
        CandidateList.draw();
        WInputMode.draw();

        GlStateManager.disableBlend();
        GlStateManager.enableDepth();

        GlStateManager.popMatrix();
    }

    public void setCaretPos(int x, int y) {
        PreEdit.setPos(x, y);
        CandidateList.setPos(x, y);
        WInputMode.setPos(x, y);
    }
}
package com.dhj.ingameime.gui;

import com.dhj.ingameime.Internal;
import com.dhj.ingameime.rust.RustImeLibrary;
import net.minecraft.client.renderer.GlStateManager;

public class OverlayScreen extends Widget {
    public WidgetPreEdit PreEdit = new WidgetPreEdit();
    public WidgetCandidateList CandidateList = new WidgetCandidateList();
    public WidgetInputMode WInputMode = new WidgetInputMode();

    @Override
    public boolean isActive() {
        return Internal.LIBRARY_LOADED && Internal.InputCtx != 0 && RustImeLibrary.isInputContextActivated(Internal.InputCtx);
    }

    @Override
    public void layout() {
        // Container does not need layout
    }

    @Override
    public void draw() {
        if (!isActive()) return;

        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.translate(0, 0, 30f);

        PreEdit.draw();
        CandidateList.draw();
        WInputMode.draw();

        GlStateManager.enableDepth();
        GlStateManager.disableBlend();

        GlStateManager.popMatrix();
    }

    public void setCaretPos(int x, int y) {
        PreEdit.setPos(x, y);
        CandidateList.setPos(x, y);
        WInputMode.setPos(x, y);
    }
}
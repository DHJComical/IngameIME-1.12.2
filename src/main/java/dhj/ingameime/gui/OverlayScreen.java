package dhj.ingameime.gui;

import dhj.ingameime.Internal;
import ingameime.InputContext;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

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
    }

    @Override
    public void draw() {
        if (!isActive()) return;

        boolean depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        GlStateManager.enableDepth();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 1000);
        PreEdit.draw();
        CandidateList.draw();
        WInputMode.draw();
        GlStateManager.popMatrix();

        if (depthTest)
            GlStateManager.enableDepth();
        else
            GlStateManager.disableDepth();
    }

    public void setCaretPos(int x, int y) {
        PreEdit.setPos(x, y);
        WInputMode.setPos(x, y);
    }
}

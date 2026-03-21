package com.dhj.ingameime.theme.api;

import com.dhj.ingameime.IngameIME_Forge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class ThemeRenderer {
    public static void render(Theme theme, int x, int y, int w, int h, String componentId) {
        if (theme == null) return;
        drawBackground(theme, x, y, w, h);
        if (theme.getDecorations() != null) {
            for (ThemeDecoration deco : theme.getDecorations()) {
                if ("all".equals(deco.target) || componentId.equals(deco.target)) {
                    renderDecoration(theme, deco, x, y, w, h);
                }
            }
        }
    }

    private static void drawBackground(Theme theme, int x, int y, int w, int h) {
        ResourceLocation texture = ThemeManager.getInstance().getThemeTexture(theme.getId());

        IngameIME_Forge.logDebugInfo(String.format("[ThemeRenderer] Rendering ID: %s | Texture: %b | Slice: %d", theme.getId(), (texture != null), theme.getSliceSize()));

        if (texture != null && theme.getSliceSize() > 0) {
            draw9Slice(texture, x, y, w, h, theme);
        } else {
            Gui.drawRect(x, y, x + w, y + h, theme.getBackgroundColor());
        }
    }

    /**
     * Render external decoration widget (dual-anchor algorithm)
     */
    private static void renderDecoration(Theme theme, ThemeDecoration deco, int bx, int by, int bw, int bh) {
        String key = theme.getId() + ":" + deco.textureFile;
        ResourceLocation res = ThemeManager.getInstance().getThemeTexture(key);

        if (res == null) {
            IngameIME_Forge.logDebugInfo("[ThemeRenderer] Decoration texture not found: {}", key);
            return;
        }

        // Get original image dimensions
        Integer[] size = ThemeManager.getInstance().getTextureSize(key);
        int tw = size[0];
        int th = size[1];

        // Calculate final display dimensions
        int dw = (deco.width > 0) ? deco.width : (int)(tw * deco.scale);
        int dh = (deco.height > 0) ? deco.height : (int)(th * deco.scale);

        // Anchor calculation formula: based on 3x3 grid 0-8 layout
        int ax = bx + (deco.anchorPoint % 3) * bw / 2;
        int ay = by + (deco.anchorPoint / 3) * bh / 2;

        // Image drawing start point calculation (dx, dy)
        int dx = ax + deco.offsetX - (deco.imageAnchor % 3) * dw / 2;
        int dy = ay + deco.offsetY - (deco.imageAnchor / 3) * dh / 2;

        IngameIME_Forge.logDebugInfo(String.format("[ThemeRenderer] Draw Deco: File=%s | Pos=[%d,%d] | Size=[%d,%d]",
                deco.textureFile, dx, dy, dw, dh));

        Minecraft.getMinecraft().getTextureManager().bindTexture(res);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, deco.alpha);

        // Execute scaled image drawing - 1.7.10 compatible
        drawModalRectWithCustomSizedTexture(dx, dy, 0, 0, dw, dh, (float)dw, (float)dh);

        // State reset
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void draw9Slice(ResourceLocation texture, int x, int y, int w, int h, Theme theme) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);

        int u = theme.getU();
        int v = theme.getV();
        int s = theme.getSliceSize();
        int c = theme.getCornerSize();
        int tw = theme.getTextureWidth();
        int th = theme.getTextureHeight();

        int innerW = w - c * 2;
        int innerH = h - c * 2;
        int texInner = s - c * 2;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        // --- 1. Draw four corners (fixed size) ---
        drawScaledCustomSizeModalRect(x, y, u, v, c, c, c, c, tw, th);
        drawScaledCustomSizeModalRect(x + w - c, y, u + s - c, v, c, c, c, c, tw, th);
        drawScaledCustomSizeModalRect(x, y + h - c, u, v + s - c, c, c, c, c, tw, th);
        drawScaledCustomSizeModalRect(x + w - c, y + h - c, u + s - c, v + s - c, c, c, c, c, tw, th);

        // --- 2. Draw four edges (single-direction stretch) ---
        if (innerW > 0) {
            drawScaledCustomSizeModalRect(x + c, y, u + c, v, texInner, c, innerW, c, tw, th);
            drawScaledCustomSizeModalRect(x + c, y + h - c, u + c, v + s - c, texInner, c, innerW, c, tw, th);
        }
        if (innerH > 0) {
            drawScaledCustomSizeModalRect(x, y + c, u, v + c, c, texInner, c, innerH, tw, th);
            drawScaledCustomSizeModalRect(x + w - c, y + c, u + s - c, v + c, c, texInner, c, innerH, tw, th);
        }

        // --- 3. Draw middle area (bi-directional stretch) ---
        if (innerW > 0 && innerH > 0) {
            drawScaledCustomSizeModalRect(x + c, y + c, u + c, v + c, texInner, texInner, innerW, innerH, tw, th);
        }
    }

    /**
     * Draw rect with custom size - 1.7.10 compatible version
     */
    private static void drawScaledCustomSizeModalRect(int x, int y, int u, int v, int uWidth, int vHeight, int xWidth, int yHeight, int textureWidth, int textureHeight) {
        double xScale = (double) uWidth / (double) textureWidth;
        double yScale = (double) vHeight / (double) textureHeight;

        double minU = (double) u / textureWidth;
        double maxU = (double) (u + uWidth) / textureWidth;
        double minV = (double) v / textureHeight;
        double maxV = (double) (v + vHeight) / textureHeight;

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((double) x, (double) (y + yHeight), 0.0D, minU, maxV);
        tessellator.addVertexWithUV((double) (x + xWidth), (double) (y + yHeight), 0.0D, maxU, maxV);
        tessellator.addVertexWithUV((double) (x + xWidth), (double) y, 0.0D, maxU, minV);
        tessellator.addVertexWithUV((double) x, (double) y, 0.0D, minU, minV);
        tessellator.draw();
    }

    /**
     * Draw modal rect with custom sized texture - 1.7.10 compatible
     */
    private static void drawModalRectWithCustomSizedTexture(int x, int y, int u, int v, int width, int height, float textureWidth, float textureHeight) {
        double xScale = (double) width / textureWidth;
        double yScale = (double) height / textureHeight;

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((double) x, (double) (y + height), 0.0D, (double) u / textureWidth, (double) (v + height) / textureHeight);
        tessellator.addVertexWithUV((double) (x + width), (double) (y + height), 0.0D, (double) (u + width) / textureWidth, (double) (v + height) / textureHeight);
        tessellator.addVertexWithUV((double) (x + width), (double) y, 0.0D, (double) (u + width) / textureWidth, (double) v / textureHeight);
        tessellator.addVertexWithUV((double) x, (double) y, 0.0D, (double) u / textureWidth, (double) v / textureHeight);
        tessellator.draw();
    }

    /**
     * Draw simple rect - 1.7.10 compatible
     */
    private static void drawRect(int x1, int y1, int x2, int y2, int color) {
        Gui.drawRect(x1, y1, x2, y2, color);
    }
}

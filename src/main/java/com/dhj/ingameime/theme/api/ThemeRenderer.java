package com.dhj.ingameime.theme.api;

import com.dhj.ingameime.IngameIME_Forge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class ThemeRenderer {
    // Throttle counter to prevent log spam in debug mode
    private static int logThrottle = 0;
    private static final int LOG_INTERVAL = 60; // Log every 60 frames (~1 second at 60 FPS)

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
        logThrottle++;
    }

    private static void drawBackground(Theme theme, int x, int y, int w, int h) {
        ResourceLocation texture = ThemeManager.getInstance().getThemeTexture(theme.getId());

        if (logThrottle % LOG_INTERVAL == 0) {
            IngameIME_Forge.logDebugInfo(String.format("[ThemeRenderer] Rendering ID: %s | Texture: %b | Slice: %d", theme.getId(), (texture != null), theme.getSliceSize()));
        }

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
            if (logThrottle % LOG_INTERVAL == 0) {
                IngameIME_Forge.logDebugInfo("[ThemeRenderer] Decoration texture not found: {}", key);
            }
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
        // Candidate box anchor calculation (ax, ay)
        int ax = bx + (deco.anchorPoint % 3) * bw / 2;
        int ay = by + (deco.anchorPoint / 3) * bh / 2;

        // Image drawing start point calculation (dx, dy)
        // Subtract the displacement of the image's own anchor
        int dx = ax + deco.offsetX - (deco.imageAnchor % 3) * dw / 2;
        int dy = ay + deco.offsetY - (deco.imageAnchor / 3) * dh / 2;

        if (logThrottle % LOG_INTERVAL == 0) {
            IngameIME_Forge.logDebugInfo(String.format("[ThemeRenderer] Draw Deco: File=%s | Pos=[%d,%d] | Size=[%d,%d]",
                    deco.textureFile, dx, dy, dw, dh));
        }

        Minecraft.getMinecraft().getTextureManager().bindTexture(res);
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, deco.alpha);

        // Execute scaled image drawing
        Gui.drawModalRectWithCustomSizedTexture(dx, dy, 0, 0, dw, dh, (float)dw, (float)dh);

        // State reset
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
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
        int texInner = s - c * 2; // Size of middle part in texture

        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        // --- 1. Draw four corners (fixed size) ---
        Gui.drawScaledCustomSizeModalRect(x, y, u, v, c, c, c, c, tw, th); // Top-left
        Gui.drawScaledCustomSizeModalRect(x + w - c, y, u + s - c, v, c, c, c, c, tw, th); // Top-right
        Gui.drawScaledCustomSizeModalRect(x, y + h - c, u, v + s - c, c, c, c, c, tw, th); // Bottom-left
        Gui.drawScaledCustomSizeModalRect(x + w - c, y + h - c, u + s - c, v + s - c, c, c, c, c, tw, th); // Bottom-right

        // --- 2. Draw four edges (single-direction stretch) ---
        if (innerW > 0) {
            // Top edge
            Gui.drawScaledCustomSizeModalRect(x + c, y, u + c, v, texInner, c, innerW, c, tw, th);
            // Bottom edge
            Gui.drawScaledCustomSizeModalRect(x + c, y + h - c, u + c, v + s - c, texInner, c, innerW, c, tw, th);
        }
        if (innerH > 0) {
            // Left edge
            Gui.drawScaledCustomSizeModalRect(x, y + c, u, v + c, c, texInner, c, innerH, tw, th);
            // Right edge
            Gui.drawScaledCustomSizeModalRect(x + w - c, y + c, u + s - c, v + c, c, texInner, c, innerH, tw, th);
        }

        // --- 3. Draw middle area (bi-directional stretch) ---
        if (innerW > 0 && innerH > 0) {
            Gui.drawScaledCustomSizeModalRect(x + c, y + c, u + c, v + c, texInner, texInner, innerW, innerH, tw, th);
        }
    }
}
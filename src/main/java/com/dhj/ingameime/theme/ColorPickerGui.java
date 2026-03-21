package com.dhj.ingameime.theme;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class ColorPickerGui extends GuiScreen {
    private final GuiScreen parent;
    private final int initialColor;
    private final ColorPickerCallback callback;

    private int selectedColor;
    private float hue, saturation, brightness;
    private int alpha;

    private final int topAreaHeight = 32;
    private final int bottomAreaHeight = 45;
    private int scrollOffset = 0;
    private final int contentHeight = 320;
    private int maxScrollOffset = 0;
    private boolean isScrollingBar = false;

    private int paletteX, paletteY;
    private final int paletteWidth = 180;
    private final int paletteHeight = 120;
    private final int sliderWidth = 180;
    private final int sliderHeight = 12;
    private int hueSliderX;
    private int hueSliderY;
    private int alphaSliderX;
    private int alphaSliderY;
    private int previewX;
    private int previewY;
    private final int previewSize = 40;

    private int selectedX, selectedY;
    private boolean isDraggingPalette = false, isDraggingHue = false, isDraggingAlpha = false;

    public interface ColorPickerCallback {
        void onColorSelected(int color);
    }

    public ColorPickerGui(GuiScreen parent, int initialColor, ColorPickerCallback callback) {
        this.parent = parent;
        this.initialColor = initialColor;
        this.callback = callback;
        this.selectedColor = initialColor;
        extractColorComponents(initialColor);
    }

    private void extractColorComponents(int color) {
        alpha = (color >> 24) & 0xFF;
        float[] hsb = RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF);
        hue = hsb[0]; saturation = hsb[1]; brightness = hsb[2];
        selectedX = (int)(saturation * (paletteWidth - 1));
        selectedY = (int)((1.0f - brightness) * (paletteHeight - 1));
    }

    @Override
    public void initGui() {
        int centerX = width / 2;
        paletteX = centerX - paletteWidth / 2 + 30;
        paletteY = 10;
        hueSliderX = paletteX;
        hueSliderY = paletteY + paletteHeight + 20;
        alphaSliderX = paletteX;
        alphaSliderY = hueSliderY + sliderHeight + 20;
        previewX = paletteX - 100;
        previewY = paletteY;

        buttonList.clear();
        buttonList.add(new GuiButton(1, centerX + 5, height - 32, 100, 20, I18n.format("gui.done")));
        buttonList.add(new GuiButton(0, centerX - 105, height - 32, 100, 20, I18n.format("gui.cancel")));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawBackground(0);

        int scrollAreaTop = topAreaHeight;
        int scrollAreaBottom = height - bottomAreaHeight;
        int viewportHeight = scrollAreaBottom - scrollAreaTop;
        maxScrollOffset = Math.max(0, contentHeight - viewportHeight);
        if (scrollOffset > maxScrollOffset) scrollOffset = maxScrollOffset;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int sf = sr.getScaleFactor();
        GL11.glScissor(0, bottomAreaHeight * sf, width * sf, viewportHeight * sf);

        GL11.glPushMatrix();
        GL11.glTranslatef(0, -scrollOffset + topAreaHeight, 0);

        drawColorPalette();
        drawHueSlider();
        drawAlphaSlider();
        drawPreview();
        drawColorValues();

        GL11.glPopMatrix();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        overlayBackground(0, topAreaHeight);
        overlayBackground(height - bottomAreaHeight, height);
        drawGradientRect(0, topAreaHeight, width, topAreaHeight + 4, 0xFF000000, 0x00000000);
        drawGradientRect(0, height - bottomAreaHeight - 4, width, height - bottomAreaHeight, 0x00000000, 0xFF000000);

        drawCenteredString(mc.fontRenderer, I18n.format("ingameime.colorpicker.title"), width / 2, 10, 0xFFFFFF);

        if (maxScrollOffset > 0) {
            drawScrollBar(scrollAreaTop, scrollAreaBottom);
        }

        for (GuiButton button : buttonList) {
            button.drawButton(mc, mouseX, mouseY);
        }
    }

    private void drawScrollBar(int top, int bottom) {
        int h = bottom - top;
        int th = Math.max(20, (int)((float)h / contentHeight * h));
        int ty = top + (int)((float)scrollOffset / maxScrollOffset * (h - th));
        drawRect(width - 6, top, width, bottom, 0xFF000000);
        drawRect(width - 6, ty, width, ty + th, 0xFF808080);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0) {
            if (mouseX >= width - 15 && mouseY >= topAreaHeight && mouseY <= height - bottomAreaHeight) {
                isScrollingBar = true;
                return;
            }

            int adjustedY = mouseY + scrollOffset - topAreaHeight;
            if (mouseY > topAreaHeight && mouseY < height - bottomAreaHeight) {
                if (mouseX >= paletteX && mouseX <= paletteX + paletteWidth && adjustedY >= paletteY && adjustedY <= paletteY + paletteHeight) {
                    isDraggingPalette = true;
                    handlePaletteInput(mouseX, adjustedY);
                }
                if (mouseX >= hueSliderX && mouseX <= hueSliderX + sliderWidth && adjustedY >= hueSliderY && adjustedY <= hueSliderY + sliderHeight) {
                    isDraggingHue = true;
                    handleHueInput(mouseX);
                }
                if (mouseX >= alphaSliderX && mouseX <= alphaSliderX + sliderWidth && adjustedY >= alphaSliderY && adjustedY <= alphaSliderY + sliderHeight) {
                    isDraggingAlpha = true;
                    handleAlphaInput(mouseX);
                }
            }
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (isScrollingBar) {
            float f = (float)(mouseY - topAreaHeight) / (height - topAreaHeight - bottomAreaHeight);
            scrollOffset = (int)(f * contentHeight) - ((height - topAreaHeight - bottomAreaHeight) / 2);
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
        } else {
            int adjustedY = mouseY + scrollOffset - topAreaHeight;
            if (isDraggingPalette) handlePaletteInput(mouseX, adjustedY);
            if (isDraggingHue) handleHueInput(mouseX);
            if (isDraggingAlpha) handleAlphaInput(mouseX);
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int dw = Mouse.getEventDWheel();
        if (dw != 0 && maxScrollOffset > 0) {
            scrollOffset += (dw > 0 ? -20 : 20);
            scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
        }
    }

    protected void mouseReleased(int mouseX, int mouseY, int state) {
        isScrollingBar = isDraggingPalette = isDraggingHue = isDraggingAlpha = false;
    }

    private void handlePaletteInput(int mx, int my) {
        selectedX = Math.max(0, Math.min(paletteWidth - 1, mx - paletteX));
        selectedY = Math.max(0, Math.min(paletteHeight - 1, my - paletteY));
        saturation = (float) selectedX / (paletteWidth - 1);
        brightness = 1.0f - (float) selectedY / (paletteHeight - 1);
        updateSelectedColor();
    }

    private void handleHueInput(int mx) {
        hue = Math.max(0.0f, Math.min(1.0f, (float)(mx - hueSliderX) / sliderWidth));
        updateSelectedColor();
    }

    private void handleAlphaInput(int mx) {
        alpha = (int)(Math.max(0.0f, Math.min(1.0f, (float)(mx - alphaSliderX) / sliderWidth)) * 255);
        updateSelectedColor();
    }

    private void updateSelectedColor() {
        int[] rgb = HSBtoRGB(hue, saturation, brightness);
        selectedColor = (alpha << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
    }

    private void drawColorPalette() {
        int step = 2;
        for (int x = 0; x < paletteWidth; x += step) {
            for (int y = 0; y < paletteHeight; y += step) {
                float sat = (float) x / (paletteWidth - 1);
                float bri = 1.0f - (float) y / (paletteHeight - 1);
                int[] rgb = HSBtoRGB(hue, sat, bri);
                drawRect(paletteX + x, paletteY + y, paletteX + x + step,
                        paletteY + y + step, (255 << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2]);
            }
        }
        drawRect(paletteX + selectedX - 2, paletteY + selectedY - 2,
                paletteX + selectedX + 2, paletteY + selectedY + 2, 0xFF000000);
        drawRect(paletteX + selectedX - 1, paletteY + selectedY - 1,
                paletteX + selectedX + 1, paletteY + selectedY + 1, 0xFFFFFFFF);
    }

    private void drawHueSlider() {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.fontRenderer.drawString(I18n.format("ingameime.colorpicker.hue"), hueSliderX,
                hueSliderY - 12, 0xFFFFFF);

        for (int x = 0; x < sliderWidth; x++) {
            float h = (float) x / sliderWidth;
            int[] rgb = HSBtoRGB(h, 1.0f, 1.0f);
            drawRect(hueSliderX + x, hueSliderY, hueSliderX + x + 1, hueSliderY + sliderHeight,
                    (255 << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2]);
        }

        int sx = hueSliderX + (int)(hue * sliderWidth);
        drawRect(sx - 2, hueSliderY - 2, sx + 2, hueSliderY + sliderHeight + 2, 0xFF000000);
        drawRect(sx - 1, hueSliderY - 1, sx + 1, hueSliderY + sliderHeight + 1, 0xFFFFFFFF);
    }

    private void drawAlphaSlider() {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.fontRenderer.drawString(I18n.format("ingameime.colorpicker.alpha"), alphaSliderX,
                alphaSliderY - 12, 0xFFFFFF);

        drawCheckerboard(alphaSliderX, alphaSliderY, sliderWidth, sliderHeight);

        for (int x = 0; x < sliderWidth; x++) {
            int a = (int)((float)x / sliderWidth * 255);
            int[] rgb = HSBtoRGB(hue, saturation, brightness);
            drawRect(alphaSliderX + x, alphaSliderY, alphaSliderX + x + 1, alphaSliderY + sliderHeight,
                    (a << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2]);
        }

        int sx = alphaSliderX + (int)((float)alpha / 255 * sliderWidth);
        drawRect(sx - 2, alphaSliderY - 2, sx + 2, alphaSliderY + sliderHeight + 2, 0xFF000000);
        drawRect(sx - 1, alphaSliderY - 1, sx + 1, alphaSliderY + sliderHeight + 1, 0xFFFFFFFF);
    }

    private void drawPreview() {
        drawCheckerboard(previewX, previewY, previewSize, previewSize);
        drawRect(previewX, previewY, previewX + previewSize, previewY + previewSize, selectedColor);
        int origY = previewY + previewSize + 20;
        drawCheckerboard(previewX, origY, previewSize, previewSize);
        drawRect(previewX, origY, previewX + previewSize, origY + previewSize, initialColor);
    }

    private void drawColorValues() {
        mc.fontRenderer.drawString(String.format("#%08X", selectedColor), previewX, previewY + previewSize * 2 + 30,
                0xFFFFFF);
    }

    protected void overlayBackground(int startY, int endY) {
        Tessellator tess = Tessellator.instance;
        this.mc.renderEngine.bindTexture(new ResourceLocation("textures/gui/options_background.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        tess.startDrawingQuads();
        tess.addVertexWithUV(0, endY, 0, 0, endY / 32.0F);
        tess.addVertexWithUV(width, endY, 0, width / 32.0F, endY / 32.0F);
        tess.addVertexWithUV(width, startY, 0, width / 32.0F, startY / 32.0F);
        tess.addVertexWithUV(0, startY, 0, 0, startY / 32.0F);
        tess.draw();
    }

    private void drawCheckerboard(int x, int y, int w, int h) {
        int size = 4;
        for (int i = 0; i < w; i += size) {
            for (int j = 0; j < h; j += size) {
                boolean light = ((i / size) + (j / size)) % 2 == 0;
                drawRect(x + i, y + j, x + Math.min(i + size, w), y + Math.min(j + size, h), light ? 0xFFCCCCCC : 0xFF888888);
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) mc.displayGuiScreen(parent);
        else if (button.id == 1) {
            if (callback != null) callback.onColorSelected(selectedColor);
            mc.displayGuiScreen(parent);
        }
    }

    private float[] RGBtoHSB(int r, int g, int b) {
        float min = Math.min(r, Math.min(g, b)), max = Math.max(r, Math.max(g, b));
        float v = max / 255.0f;
        float s = (max == 0) ? 0 : (max - min) / max;
        float h;
        if (max == min) h = 0;
        else if (max == r) h = (g - b) / (max - min) + (g < b ? 6 : 0);
        else if (max == g) h = (b - r) / (max - min) + 2;
        else h = (r - g) / (max - min) + 4;
        return new float[]{h / 6, s, v};
    }

    private int[] HSBtoRGB(float h, float s, float v) {
        float i = (float)Math.floor(h * 6), f = h * 6 - i, p = v * (1 - s), q = v * (1 - f * s), t = v * (1 - (1 - f) * s);
        int r=0, g=0, b=0;
        switch ((int)i % 6) {
            case 0: r=(int)(v*255); g=(int)(t*255); b=(int)(p*255); break;
            case 1: r=(int)(q*255); g=(int)(v*255); b=(int)(p*255); break;
            case 2: r=(int)(p*255); g=(int)(v*255); b=(int)(t*255); break;
            case 3: r=(int)(p*255); g=(int)(q*255); b=(int)(v*255); break;
            case 4: r=(int)(t*255); g=(int)(p*255); b=(int)(v*255); break;
            case 5: r=(int)(v*255); g=(int)(p*255); b=(int)(q*255); break;
        }
        return new int[]{r, g, b};
    }
}

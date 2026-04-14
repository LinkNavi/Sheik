package net.minecraft.sheik.ui.render;

import net.minecraft.client.gui.Gui;

public final class RenderUtil {
    private RenderUtil() {}

    public static void drawRect(int x, int y, int width, int height, int color) {
        Gui.drawRect(x, y, x + width, y + height, color);
    }

    public static void drawOutline(int x, int y, int width, int height, int color) {
        Gui.drawRect(x, y, x + width, y + 1, color);
        Gui.drawRect(x, y + height - 1, x + width, y + height, color);
        Gui.drawRect(x, y, x + 1, y + height, color);
        Gui.drawRect(x + width - 1, y, x + width, y + height, color);
    }

    public static void drawRoundedRect(int x, int y, int width, int height, int radius, int color) {
        // Radius is currently ignored to keep drawing compatible with existing utility primitives.
        drawRect(x, y, width, height, color);
    }

    public static void drawShadowedRoundedRect(int x, int y, int width, int height, int radius, int color) {
        // Lightweight shadow fallback before the panel body draw.
        drawRect(x + 1, y + 1, width, height, 0x40000000);
        drawRoundedRect(x, y, width, height, radius, color);
    }
}

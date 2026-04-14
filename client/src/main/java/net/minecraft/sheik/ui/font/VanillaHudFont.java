package net.minecraft.sheik.ui.font;

import net.minecraft.client.gui.FontRenderer;

public class VanillaHudFont implements HudFont {
    private final FontRenderer renderer;

    public VanillaHudFont(FontRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public int drawStringWithShadow(String text, int x, int y, int color) {
        return renderer.drawStringWithShadow(text, x, y, color);
    }

    @Override
    public int getStringWidth(String text) {
        return renderer.getStringWidth(text);
    }

    @Override
    public int getFontHeight() {
        return renderer.FONT_HEIGHT;
    }
}

package net.minecraft.sheik.ui.hud;

import net.minecraft.client.gui.Gui;
import net.minecraft.sheik.ui.font.Fonts;
import net.minecraft.sheik.ui.font.HudFont;

public final class HudUi {
    private HudUi() {}

    public static HudPanel panel(int x, int y, int width, String title) {
        return panel(x, y, width, title, null);
    }

    public static HudPanel panel(int x, int y, int width, String title, String fontId) {
        HudFont font = fontId == null ? Fonts.defaultFont() : Fonts.get(fontId);
        return new HudPanel(x, y, width, title, font);
    }


    public static HudLabel label(int x, int y, HudFont font, String text, int color, int width, int height) {
        HudFont resolvedFont = font == null ? Fonts.defaultFont() : font;
        return new HudLabel(x, y, resolvedFont, text, color, width, height);
    }

    public static HudLabel label(int x, int y, String text) {
        HudFont font = Fonts.defaultFont();
        int width = font.getStringWidth(text) + 8;
        int height = font.getFontHeight() + 6;
        return new HudLabel(x, y, font, text, HudColors.TEXT, width, height);
    }

    public static HudLabel labelTopLeft(String text) {
        return label(4, 4, text);
    }

    static void rect(int x, int y, int width, int height, int color) {
        Gui.drawRect(x, y, x + width, y + height, color);
    }

    static void outline(int x, int y, int width, int height, int color) {
        rect(x, y, width, 1, color);
        rect(x, y + height - 1, width, 1, color);
        rect(x, y, 1, height, color);
        rect(x + width - 1, y, 1, height, color);
    }
}

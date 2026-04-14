package net.minecraft.sheik.ui.font;

public interface HudFont {
    int drawStringWithShadow(String text, int x, int y, int color);

    int getStringWidth(String text);

    int getFontHeight();
}

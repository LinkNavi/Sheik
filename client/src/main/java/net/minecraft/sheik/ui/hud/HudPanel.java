package net.minecraft.sheik.ui.hud;

import net.minecraft.sheik.ui.font.HudFont;

public class HudPanel {
    private static final int PADDING = 5;
    private static final int ROW_HEIGHT = 11;

    private final int x;
    private final int y;
    private final int width;
    private final HudFont font;
    private int cursorY;

    HudPanel(int x, int y, int width, String title, HudFont font) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.font = font;
        this.cursorY = y + 17;

        HudUi.rect(x, y, width, 13, HudColors.HEADER_BG);
        HudUi.rect(x, y + 13, width, 2, HudColors.ACCENT);
        HudUi.outline(x, y, width, 15, HudColors.PANEL_BORDER);
        this.font.drawStringWithShadow(title, x + PADDING, y + 3, HudColors.TEXT);
    }

    public HudPanel text(String text) {
        return text(text, HudColors.TEXT);
    }

    public HudPanel text(String text, int color) {
        int h = Math.max(ROW_HEIGHT, font.getFontHeight() + 3);
        HudUi.rect(x, cursorY, width, h, HudColors.PANEL_BG);
        HudUi.outline(x, cursorY, width, h, HudColors.PANEL_BORDER);
        font.drawStringWithShadow(text, x + PADDING, cursorY + 2, color);
        cursorY += h;
        return this;
    }

    public HudPanel row(String label, String value) {
        return row(label, value, HudColors.TEXT, HudColors.TEXT_MUTED);
    }

    public HudPanel row(String label, String value, int leftColor, int rightColor) {
        int h = Math.max(ROW_HEIGHT, font.getFontHeight() + 3);
        HudUi.rect(x, cursorY, width, h, HudColors.PANEL_BG);
        HudUi.outline(x, cursorY, width, h, HudColors.PANEL_BORDER);

        font.drawStringWithShadow(label, x + PADDING, cursorY + 2, leftColor);
        int valueX = x + width - PADDING - font.getStringWidth(value);
        font.drawStringWithShadow(value, valueX, cursorY + 2, rightColor);
        cursorY += h;
        return this;
    }

    public HudPanel toggle(String label, boolean enabled) {
        String value = enabled ? "ON" : "OFF";
        int color = enabled ? HudColors.GOOD : HudColors.BAD;
        return row(label, value, HudColors.TEXT, color);
    }

    public int getBottomY() {
        return cursorY;
    }
}

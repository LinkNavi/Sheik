package net.minecraft.sheik.ui.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
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

    public HudPanel bar(String label, float current, float max, int barColor) {
        int h = Math.max(ROW_HEIGHT, font.getFontHeight() + 3);
        HudUi.rect(x, cursorY, width, h, HudColors.PANEL_BG);
        HudUi.outline(x, cursorY, width, h, HudColors.PANEL_BORDER);

        font.drawStringWithShadow(label, x + PADDING, cursorY + 2, HudColors.TEXT);

        int barX = x + PADDING + font.getStringWidth(label) + 3;
        int barW = x + width - PADDING - barX;
        int barY = cursorY + (h - 5) / 2;
        float pct = max > 0 ? Math.min(current / max, 1.0f) : 0f;

        HudUi.rect(barX, barY, barW, 5, HudColors.PANEL_BORDER);
        HudUi.rect(barX, barY, (int)(barW * pct), 5, barColor);

        cursorY += h;
        return this;
    }

    public HudPanel bar(String label, float current, float max) {
        float pct = max > 0 ? current / max : 0f;
        int color = pct > 0.5f ? HudColors.GOOD : (pct > 0.25f ? 0xFFFFAA00 : HudColors.BAD);
        return bar(label, current, max, color);
    }

    /**
     * Renders a row of item icons. Null entries are skipped.
     * Each icon is 16x16 with 2px spacing. The row height is 20px.
     */
    public HudPanel items(ItemStack... stacks) {
        final int ICON_SIZE = 16;
        final int ROW_H = ICON_SIZE + 4;
        HudUi.rect(x, cursorY, width, ROW_H, HudColors.PANEL_BG);
        HudUi.outline(x, cursorY, width, ROW_H, HudColors.PANEL_BORDER);

        int iconX = x + PADDING;
        int iconY = cursorY + (ROW_H - ICON_SIZE) / 2;
        for (ItemStack stack : stacks) {
            if (stack == null) continue;
            Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(stack, iconX, iconY);
            iconX += ICON_SIZE + 2;
        }

        cursorY += ROW_H;
        return this;
    }

    public int getBottomY() {
        return cursorY;
    }
}

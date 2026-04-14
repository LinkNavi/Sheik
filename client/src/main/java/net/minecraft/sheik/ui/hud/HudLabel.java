package net.minecraft.sheik.ui.hud;
import net.minecraft.sheik.ui.font.HudFont;

public class HudLabel {
    private static final int PADDING_X = 4;
    private static final int PADDING_Y = 3;

    private final int x;
    private final int y;
    private final HudFont font;
    private final int width;
    private final int height;
    private final String text;
    private final int color;

    public HudLabel(int x, int y, HudFont font, String text, int color, int width, int height) {
        this.x = x;
        this.y = y; 
        this.font = font;
        this.width = width;
        this.height = height;
        this.text = text;
        this.color = color;
    }
    
    public void draw(int mouseX, int mouseY) {
        HudUi.rect(x, y, width, height, HudColors.PANEL_BG);
        HudUi.outline(x, y, width, height, HudColors.PANEL_BORDER);
        font.drawStringWithShadow(text, x + PADDING_X, y + PADDING_Y, color);
    }
}

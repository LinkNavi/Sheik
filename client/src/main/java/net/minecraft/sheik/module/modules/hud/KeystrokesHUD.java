package net.minecraft.sheik.module.modules.hud;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.sheik.module.HudPositionable;
import net.minecraft.sheik.module.Module;
import org.lwjgl.input.Keyboard;

public class KeystrokesHUD extends Module implements HudPositionable {
    private static final int KEY_SIZE = 18;
    private static final int GAP = 4;
    private static final int BG_IDLE = 0x80151515;
    private static final int BG_PRESSED = 0xC050A0FF;
    private static final int BORDER = 0xA0FFFFFF;
    private static final int TEXT = 0xFFFFFFFF;

    private int hudX = 8;
    private int hudY = 24;

    public KeystrokesHUD() {
        super("KeystrokesHUD", "HUD", "Shows movement and mouse keys", Keyboard.KEY_NONE);
    }

    @Override
    public int getHudX() { return hudX; }

    @Override
    public int getHudY() { return hudY; }

    @Override
    public void setHudX(int x) { this.hudX = x; }

    @Override
    public void setHudY(int y) { this.hudY = y; }

    @Override
    public void onRender2D(ScaledResolution sr, float partialTicks) {
        int topRowX = hudX + KEY_SIZE + GAP;
        int secondRowY = hudY + KEY_SIZE + GAP;

        drawKey(topRowX, hudY, KEY_SIZE, KEY_SIZE, "W", mc.gameSettings.keyBindForward.isKeyDown());
        drawKey(hudX, secondRowY, KEY_SIZE, KEY_SIZE, "A", mc.gameSettings.keyBindLeft.isKeyDown());
        drawKey(hudX + KEY_SIZE + GAP, secondRowY, KEY_SIZE, KEY_SIZE, "S", mc.gameSettings.keyBindBack.isKeyDown());
        drawKey(hudX + (KEY_SIZE + GAP) * 2, secondRowY, KEY_SIZE, KEY_SIZE, "D", mc.gameSettings.keyBindRight.isKeyDown());

        int mouseRowY = secondRowY + KEY_SIZE + GAP;
        int totalWidth = KEY_SIZE * 3 + GAP * 2;
        int mouseKeyWidth = (totalWidth - GAP) / 2;

        drawKey(hudX, mouseRowY, mouseKeyWidth, KEY_SIZE, "LMB", mc.gameSettings.keyBindAttack.isKeyDown());
        drawKey(hudX + mouseKeyWidth + GAP, mouseRowY, mouseKeyWidth, KEY_SIZE, "RMB", mc.gameSettings.keyBindUseItem.isKeyDown());
    }

    private void drawKey(int x, int y, int width, int height, String text, boolean pressed) {
        int bgColor = pressed ? BG_PRESSED : BG_IDLE;
        Gui.drawRect(x, y, x + width, y + height, bgColor);
        Gui.drawRect(x, y, x + width, y + 1, BORDER);
        Gui.drawRect(x, y + height - 1, x + width, y + height, BORDER);
        Gui.drawRect(x, y, x + 1, y + height, BORDER);
        Gui.drawRect(x + width - 1, y, x + width, y + height, BORDER);

        int textX = x + (width - mc.fontRendererObj.getStringWidth(text)) / 2;
        int textY = y + (height - mc.fontRendererObj.FONT_HEIGHT) / 2;
        mc.fontRendererObj.drawStringWithShadow(text, textX, textY, TEXT);
    }
}


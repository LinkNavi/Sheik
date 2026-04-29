package net.minecraft.sheik.module.modules.hud;

import java.util.ArrayDeque;
import java.util.Deque;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.sheik.module.HudPositionable;
import net.minecraft.sheik.module.Module;
import net.minecraft.sheik.ui.hud.HudUi;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class CPSDisplay extends Module implements HudPositionable {
    private final Deque<Long> clickTimes = new ArrayDeque<>();
    private boolean wasLeftDown;
    private int hudX = 8;
    private int hudY = 96;

    public CPSDisplay() {
        super("CPSDisplay", "HUD", "Displays your left-clicks per second", Keyboard.KEY_NONE);
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
    public void onDisable() {
        clickTimes.clear();
        wasLeftDown = false;
    }

    @Override
    public void onTick() {
        boolean leftDown = Mouse.isButtonDown(0);
        if (leftDown && !wasLeftDown) {
            clickTimes.addLast(System.currentTimeMillis());
        }
        wasLeftDown = leftDown;
        pruneOldClicks(System.currentTimeMillis());
    }

    @Override
    public void onRender2D(ScaledResolution sr, float partialTicks) {
        pruneOldClicks(System.currentTimeMillis());
        if (mc.getMinecraft().theWorld == null || mc.getMinecraft().thePlayer == null) return;
        HudUi.label(hudX, hudY, "CPS: " + clickTimes.size()).draw(0, 0);
    }

    private void pruneOldClicks(long now) {
        while (!clickTimes.isEmpty() && now - clickTimes.peekFirst() > 1000L) {
            clickTimes.removeFirst();
        }
    }
}


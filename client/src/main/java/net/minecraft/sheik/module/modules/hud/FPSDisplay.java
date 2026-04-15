package net.minecraft.sheik.module.modules.hud;

import net.minecraft.sheik.module.Module;
import net.minecraft.sheik.module.HudPositionable;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.Minecraft;
import net.minecraft.sheik.ui.hud.HudUi;
import org.lwjgl.input.Keyboard;

public class FPSDisplay extends Module implements HudPositionable {
    public FPSDisplay() {
        super("FPSDisplay", "HUD", "Displays the FPS in the top left corner of the screen", Keyboard.KEY_F);
    }
 private int hudX, hudY;

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
        int fps = Minecraft.getDebugFPS();
        if (mc.getMinecraft().theWorld == null || Minecraft.getMinecraft().thePlayer == null) return;
        HudUi.label(hudX, hudY, "FPS: " + fps).draw(0, 0);
    }
}

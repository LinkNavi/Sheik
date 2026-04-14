package net.minecraft.sheik.module.modules.hud;

import net.minecraft.sheik.module.HudPositionable;
import net.minecraft.sheik.module.Module;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.sheik.ui.hud.HudUi;
import org.lwjgl.input.Keyboard;

public class ComboDisplay extends Module  implements HudPositionable{
    public ComboDisplay() {
        super("ComboDisplay", "HUD", "Displays your current hit combo", Keyboard.KEY_F);
    }
    private int combo;
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
    public void onEnable() {
        combo = 0;
    }

    @Override
    public void onRender2D(ScaledResolution sr, float partialTicks) {
        HudUi.label(hudX, hudY, "Combo: " + combo).draw(0, 0);
    }

    @Override
    public void onHit(Entity target){
        combo++;
    }

    @Override
    public void onHurt(DamageSource source, float amount){
        combo = 0;
    }
}

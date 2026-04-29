package net.minecraft.sheik.module.modules.hud;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;
import net.minecraft.sheik.module.HudPositionable;
import net.minecraft.sheik.module.Module;
import net.minecraft.sheik.ui.hud.HudUi;

public class ArmourHud extends Module implements HudPositionable {

    private final ItemStack[] armor = new ItemStack[4];
    private ItemStack heldItem;
    private int hudX = 10;
    private int hudY = 10;

    public ArmourHud() {
        super(
            "ArmourHud",
            "HUD",
            "Displays armor and weapon information about your current target",
            -1
        );
    }

    @Override
    public int getHudX() {
        return hudX;
    }

    @Override
    public int getHudY() {
        return hudY;
    }

    @Override
    public void setHudX(int x) {
        this.hudX = x;
    }

    @Override
    public void setHudY(int y) {
        this.hudY = y;
    }

    @Override
    public void onTick() {
        if (mc.theWorld == null || mc.thePlayer == null) return;
        armor[0] = mc.thePlayer.getCurrentArmor(0);
        armor[1] = mc.thePlayer.getCurrentArmor(1);
        armor[2] = mc.thePlayer.getCurrentArmor(2);
        armor[3] = mc.thePlayer.getCurrentArmor(3);
        heldItem = mc.thePlayer.getHeldItem();
    }

    @Override
    public void onRender2D(ScaledResolution sr, float partialTicks) {
        if (mc.theWorld == null || mc.thePlayer == null) return;

        HudUi.panel(hudX, hudY, 120, "").items(
            armor[3],
            armor[2],
            armor[1],
            armor[0],
            heldItem
        );
    }
}

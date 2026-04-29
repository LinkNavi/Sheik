package net.minecraft.sheik.module.modules.hud;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.sheik.module.HudPositionable;
import net.minecraft.sheik.module.Module;
import net.minecraft.util.DamageSource;

public class TradeDetector extends Module implements HudPositionable {

    // Max tick gap between hitting and being hit to count as a trade
    private static final int TRADE_WINDOW = 2;
    // How long to show the warning (~3 seconds)
    private static final int SHOW_TICKS = 60;

    private int hudX = 10, hudY = 10;

    private int hitTick  = -100;
    private int hurtTick = -100;
    private int tradeTick = -100;

    public TradeDetector() {
        super("TradeDetector", "HUD", "Warns when you are trading hits with an opponent", -1);
    }

    @Override public int getHudX() { return hudX; }
    @Override public int getHudY() { return hudY; }
    @Override public void setHudX(int x) { this.hudX = x; }
    @Override public void setHudY(int y) { this.hudY = y; }

    @Override
    public void onHit(Entity target) {
        hitTick = mc.thePlayer.ticksExisted;
        checkTrade();
    }

    @Override
    public void onHurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (!(attacker instanceof EntityPlayer) || attacker == mc.thePlayer) return;
        hurtTick = mc.thePlayer.ticksExisted;
        checkTrade();
    }

    private void checkTrade() {
        if (hitTick > 0 && hurtTick > 0 && Math.abs(hitTick - hurtTick) <= TRADE_WINDOW) {
            tradeTick = mc.thePlayer.ticksExisted;
        }
    }

    @Override
    public void onRender2D(ScaledResolution sr, float partialTicks) {
        if (mc.getMinecraft().theWorld == null |mc.getMinecraft().thePlayer == null) return;
        if (mc.thePlayer.ticksExisted - tradeTick > SHOW_TICKS) return;

        String msg = "TRADE - hold S";
        int w = mc.fontRendererObj.getStringWidth(msg) + 8;
        int h = mc.fontRendererObj.FONT_HEIGHT + 6;

        Gui.drawRect(hudX, hudY, hudX + w, hudY + h, 0xC0200000);
        mc.fontRendererObj.drawStringWithShadow(msg, hudX + 4, hudY + 3, 0xFFFF5050);
    }
}

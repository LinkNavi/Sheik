package net.minecraft.sheik.module.modules.hud;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.sheik.module.HudPositionable;
import net.minecraft.sheik.module.Module;
import net.minecraft.sheik.module.ModuleOption;
import net.minecraft.sheik.ui.hud.HudUi;

public class TargetPanel extends Module implements HudPositionable {
    private float enemyHp;
    private float enemyMaxHp;
    private String targetName;
    private ItemStack heldItem;
    private final ItemStack[] armor = new ItemStack[4];

    private int hudX = 10;
    private int hudY = 10;

    private final ModuleOption<Boolean> showHealth  = new ModuleOption<>("showHealth",  true, true, Boolean.class);
    private final ModuleOption<Boolean> showArmor   = new ModuleOption<>("showArmor",   true, true, Boolean.class);
    private final ModuleOption<Boolean> showWeapon  = new ModuleOption<>("showWeapon",  true, true, Boolean.class);

    public TargetPanel() {
        super("TargetPanel", "HUD", "Displays health information about your current target", -1);
        addOption(showHealth);
        addOption(showArmor);
        addOption(showWeapon);
    }

    @Override public int getHudX() { return hudX; }
    @Override public int getHudY() { return hudY; }
    @Override public void setHudX(int x) { this.hudX = x; }
    @Override public void setHudY(int y) { this.hudY = y; }

    @Override
    public void onHit(Entity target) {
        if (target instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase) target;
            enemyHp    = living.getHealth();
            enemyMaxHp = living.getMaxHealth();
            targetName = living.getName();
            heldItem   = living.getHeldItem();
            for (int i = 0; i < 4; i++) armor[i] = living.getCurrentArmor(i);
        }
    }

    @Override
    public void onRender2D(ScaledResolution sr, float partialTicks) {
        if (mc.getMinecraft().theWorld == null || mc.getMinecraft().thePlayer == null) return;
        if (targetName == null) return;

        net.minecraft.sheik.ui.hud.HudPanel panel =
            HudUi.panel(hudX, hudY, 120, targetName);

        if (showHealth.getValue())
            panel.bar("HP", enemyHp, enemyMaxHp);

        // Armor row: helmet (3) → chestplate (2) → leggings (1) → boots (0), then held weapon
        if (showArmor.getValue() || showWeapon.getValue()) {
            panel.items(
                showArmor.getValue()  ? armor[3] : null,
                showArmor.getValue()  ? armor[2] : null,
                showArmor.getValue()  ? armor[1] : null,
                showArmor.getValue()  ? armor[0] : null,
                showWeapon.getValue() ? heldItem  : null
            );
        }
    }
}

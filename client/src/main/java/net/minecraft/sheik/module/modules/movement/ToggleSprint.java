package net.minecraft.sheik.module.modules.movement;

import net.minecraft.sheik.module.Module;
import org.lwjgl.input.Keyboard;

public class ToggleSprint extends Module {
    private boolean forceSprint = false;

    public ToggleSprint() {
        super("ToggleSprint", "Movement", "Allows you to toggle sprinting", Keyboard.KEY_I);
    }

    @Override
    public void onTick() {
        if (mc.gameSettings.keyBindSprint.isKeyDown()) {
            forceSprint = !forceSprint;
        }

        if (this.isEnabled() && mc.thePlayer != null && forceSprint) {
            if (mc.thePlayer.moveForward > 0 && !mc.thePlayer.isSneaking()) {
                mc.thePlayer.setSprinting(true);
            }
        }
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer != null) {
            mc.thePlayer.setSprinting(false);
        }
    }
}
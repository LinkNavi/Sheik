package net.minecraft.sheik.module.modules.utility;

import net.minecraft.client.Minecraft;
import net.minecraft.sheik.module.Module;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Field;

public class NoClickDelay extends Module {
    private final Field leftClickCounterField;

    public NoClickDelay() {
        super("NoClickDelay", "Utility", "Removes left-click cooldown between swings", Keyboard.KEY_L);

        Field field = null;
        try {
            field = Minecraft.class.getDeclaredField("leftClickCounter");
            field.setAccessible(true);
        } catch (NoSuchFieldException ignored) {
            // If mapping changes, module safely does nothing instead of crashing.
        }
        this.leftClickCounterField = field;
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null || leftClickCounterField == null) {
            return;
        }
        try {
            leftClickCounterField.setInt(mc, 0);
        } catch (IllegalAccessException ignored) {
            // Reflection access failed; skip this tick.
        }
    }
}
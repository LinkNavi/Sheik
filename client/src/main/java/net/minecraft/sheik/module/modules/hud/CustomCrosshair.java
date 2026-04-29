package net.minecraft.sheik.module.modules.hud;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.sheik.module.HudPositionable;
import net.minecraft.sheik.module.Module;
import net.minecraft.sheik.module.ModuleOption;

public class CustomCrosshair extends Module {

	// options: size, gap, thickness, color
	private final ModuleOption<Integer> size = new ModuleOption<>(
		"Size",
		6,
		6,
		Integer.class
	);
	private final ModuleOption<Integer> gap = new ModuleOption<>(
		"Gap",
		2,
		2,
		Integer.class
	);
	private final ModuleOption<Integer> thickness = new ModuleOption<>(
		"Thickness",
		1,
		1,
		Integer.class
	);

	@Override
	public void onRender2D(ScaledResolution sr, float partialTicks) {
		int cx = sr.getScaledWidth() / 2;
		int cy = sr.getScaledHeight() / 2;
		int s = size.getValue();
		int g = gap.getValue();
		int t = thickness.getValue();
		int color = 0xFFFFFFFF;

		// horizontal bar
		Gui.drawRect(cx - s - g, cy - t, cx - g, cy + t, color); // left
		Gui.drawRect(cx + g, cy - t, cx + s + g, cy + t, color); // right
		// vertical bar
		Gui.drawRect(cx - t, cy - s - g, cx + t, cy - g, color); // top
		Gui.drawRect(cx - t, cy + g, cx + t, cy + s + g, color); // bottom
	}

	public CustomCrosshair() {
		super(
			"CustomCrosshair",
			"HUD",
			"Replaces the default crosshair with a customizable one",
			-1
		);
		addOption(size);
		addOption(gap);
		addOption(thickness);
	}
}

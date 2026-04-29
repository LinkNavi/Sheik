package net.minecraft.sheik.module;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.sheik.module.modules.combat.*;
import net.minecraft.sheik.module.modules.hud.*;
import net.minecraft.sheik.module.modules.movement.*;
import net.minecraft.sheik.module.modules.performance.*;
import net.minecraft.sheik.module.modules.utility.*;
import net.minecraft.util.DamageSource;

public class ModuleManager {

	private final List<Module> modules = new ArrayList<>();

	public ModuleManager() {
		registerModules();
	}

	private void registerModules() {
		register(new ToggleSprint());
		register(new NoClickDelay());
		register(new FPSDisplay());
		register(new CPSDisplay());
		register(new KeystrokesHUD());
		register(new ArmourHud());
		register(new ComboDisplay());
		register(new EntityCulling());
		register(new TargetPanel());
		register(new AimDisplay());
		register(new TradeDetector());
		register(new ReachVisualizer());
		register(new CustomCrosshair());
	}

	private void register(Module m) {
		modules.add(m);
	}

	public void onTick() {
		for (Module m : modules) {
			if (m.enabled) m.onTick();
		}
	}

	public void onRender2D(float partialTicks) {
		if (Minecraft.getMinecraft().currentScreen != null) return;
		ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
		for (Module m : modules) {
			if (m.enabled) m.onRender2D(sr, partialTicks);
		}
	}

	public void onRender3D(float partialTicks) {
		for (Module m : modules) {
			if (m.enabled) m.onRender3D(partialTicks);
		}
	}

	public void onHit(Entity target) {
		for (Module m : modules) {
			if (m.enabled) m.onHit(target);
		}
	}

	public void onHurt(DamageSource source, float amount) {
		for (Module m : modules) {
			if (m.enabled) m.onHurt(source, amount);
		}
	}

	public void onKeyPress(int key) {
		for (Module m : modules) {
			if (m.enabled) m.onKeyPress(key);
		}
	}

	public void onKeyDown(int key) {
		for (Module m : modules) {
			if (m.keybind == key) m.toggle();
			if (m.enabled) m.onKeyDown(key);
		}
	}

	public Module getModule(String name) {
		for (Module m : modules) {
			if (m.name.equalsIgnoreCase(name)) return m;
		}
		return null;
	}

	public <T extends Module> T getModule(Class<T> clazz) {
		for (Module m : modules) {
			if (clazz.isInstance(m)) return clazz.cast(m);
		}
		return null;
	}

	public List<Module> getModules() {
		return modules;
	}
}

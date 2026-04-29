package net.minecraft.sheik;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.sheik.config.ConfigManager;
import net.minecraft.sheik.external.IPC;
import net.minecraft.sheik.module.ModuleManager;
import net.minecraft.sheik.ui.ModMenuScreen;
import net.minecraft.sheik.ui.font.Fonts;
import net.minecraft.sheik.ui.hud.NotificationManager;

public class SheikClient {

	private static SheikClient INSTANCE;
	protected final Minecraft mc = Minecraft.getMinecraft();
	private ModuleManager moduleManager;
	private ConfigManager configManager;
	private NotificationManager notificationManager;
	private IPC ipcController;

	public void init() {
		configManager = new ConfigManager();
		moduleManager = new ModuleManager();
		notificationManager = new NotificationManager();
		configManager.loadModules(moduleManager.getModules());
		ipcController = new IPC();
		Fonts.registerTtfFont("clean", "sheik/fonts/Raleway.ttf", 18f);
		System.out.println("Sheik Client v1.0.0 by Sheik");
	}

	public void onTick() {
		while (mc.gameSettings.keyBindModMenu.isPressed()) {
			if (mc.currentScreen instanceof ModMenuScreen) {
				mc.displayGuiScreen(null);
			} else if (mc.currentScreen == null) {
				mc.displayGuiScreen(new ModMenuScreen());
			}
		}
		if (mc.thePlayer != null && mc.theWorld != null) {
			ipcController.writeState();
		}
		moduleManager.onTick();
	}

	public void onRender2D(float partialTicks) {
		moduleManager.onRender2D(partialTicks);
		ScaledResolution sr = new ScaledResolution(mc);
		notificationManager.onRender2D(sr);
	}

	public void onShutdown() {
		configManager.save(moduleManager.getModules());
	}

	public ModuleManager getModuleManager() {
		return moduleManager;
	}

	public ConfigManager getConfigManager() {
		return configManager;
	}

	public NotificationManager getNotificationManager() {
		return notificationManager;
	}

	public static SheikClient getInstance() {
		if (INSTANCE == null) INSTANCE = new SheikClient();
		return INSTANCE;
	}
}

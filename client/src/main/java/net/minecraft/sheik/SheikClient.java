package net.minecraft.sheik;

import net.minecraft.sheik.config.ConfigManager;
import net.minecraft.sheik.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.sheik.ui.ModMenuScreen;
import net.minecraft.sheik.ui.font.Fonts;

public class SheikClient {
    private static SheikClient INSTANCE;
    protected final Minecraft mc = Minecraft.getMinecraft();
    private ModuleManager moduleManager;
    private ConfigManager configManager;

    public void init() {
        configManager = new ConfigManager();
        moduleManager = new ModuleManager();
        configManager.loadModules(moduleManager.getModules());
        Fonts.registerTtfFont("clean", "sheik/fonts/Raleway.ttf", 18f);
        
    }

    public void onTick() {
        while (mc.gameSettings.keyBindModMenu.isPressed()) {
            if (mc.currentScreen instanceof ModMenuScreen) {
                mc.displayGuiScreen(null);
            } else if (mc.currentScreen == null) {
                mc.displayGuiScreen(new ModMenuScreen());
            }
        }
        moduleManager.onTick();
    }

    public void onRender2D(float partialTicks) {
        moduleManager.onRender2D(partialTicks);
    }

    public void onShutdown() {
        configManager.save(moduleManager.getModules());
    }

    public ModuleManager getModuleManager() { return moduleManager; }
    public ConfigManager getConfigManager() { return configManager; }

    public static SheikClient getInstance() {
        if (INSTANCE == null) INSTANCE = new SheikClient();
        return INSTANCE;
    }
}

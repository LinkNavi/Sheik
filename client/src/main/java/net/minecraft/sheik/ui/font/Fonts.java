package net.minecraft.sheik.ui.font;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.awt.FontFormatException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class Fonts {
    private static final Map<String, HudFont> CUSTOM = new HashMap<>();

    private Fonts() {}

    public static HudFont defaultFont() {
        return new VanillaHudFont(Minecraft.getMinecraft().fontRendererObj);
    }

    public static HudFont get(String id) {
        HudFont custom = CUSTOM.get(id);
        return custom != null ? custom : defaultFont();
    }

    public static boolean registerBitmapFont(String id, String resourcePath) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            net.minecraft.client.gui.FontRenderer renderer = new net.minecraft.client.gui.FontRenderer(mc.gameSettings, new ResourceLocation(resourcePath), mc.getTextureManager(), false);
            renderer.onResourceManagerReload(mc.getResourceManager());
            CUSTOM.put(id, new VanillaHudFont(renderer));
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static boolean registerTtfFont(String id, String resourcePath, float size) {
        try {
            CUSTOM.put(id, new TtfHudFont(resourcePath, size));
            return true;
        } catch (IOException | FontFormatException ignored) {
            return false;
        }
    }

    public static void clearCustomFonts() {
        CUSTOM.clear();
    }
}

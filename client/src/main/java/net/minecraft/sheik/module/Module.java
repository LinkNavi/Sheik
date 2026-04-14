package net.minecraft.sheik.module;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;

public abstract class Module {
    protected final Minecraft mc = Minecraft.getMinecraft();
private List<ModuleOption<?>> options = new ArrayList<>();
public List<ModuleOption<?>> getOptions() { return options; }
public void addOption(ModuleOption<?> option) { options.add(option); }
    public final String name;
    public final String category;
    public final String description;
    public int keybind;
    public boolean enabled;

    public Module(String name, String category,String description, int keybind) {
        this.name = name;
        this.category = category;
        this.keybind = keybind;
        this.description = description;
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
    public void onKeyDown(int key) {}
    public void onRender2D(ScaledResolution sr, float partialTicks) {}
    public void onRender3D(float partialTicks) {}
    public void onKeyPress(int key) {}
    public void onHit(Entity target) {}
    public void onHurt(DamageSource source, float amount) {}

    public void toggle() {
        enabled = !enabled;
        if (enabled) onEnable();
        else onDisable();
    }

    public boolean isEnabled() { return enabled; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public int getKeybind() { return keybind; }
    public void setKeybind(int key) { this.keybind = key; }
}

package net.minecraft.sheik.config;

import com.google.gson.*;
import java.io.*;
import java.util.List;
import net.minecraft.sheik.module.HudPositionable;
import net.minecraft.sheik.module.Module;
import net.minecraft.sheik.module.ModuleOption;

public class ConfigManager {

    private static final String CONFIG_PATH = "sheik/config.json";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void save(List<Module> modules) {
        File dir = new File("sheik");
        if (!dir.exists()) dir.mkdirs();

        JsonArray arr = new JsonArray();
        for (Module m : modules) {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", m.getName());
            obj.addProperty("enabled", m.isEnabled());
            obj.addProperty("keybind", m.getKeybind());
            if (m instanceof HudPositionable) {
                int x = ((HudPositionable) m).getHudX();
                int y = ((HudPositionable) m).getHudY();
                obj.addProperty("hudX", x);
                obj.addProperty("hudY", y);
            }

            for (ModuleOption<?> option : m.getOptions()) {
                obj.addProperty(option.getName(), option.getValue().toString());
            }

            arr.add(obj);
        }

        try (Writer w = new FileWriter(CONFIG_PATH)) {
            gson.toJson(arr, w);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadModules(List<Module> modules) {
        File file = new File(CONFIG_PATH);
        if (!file.exists()) return;
        try (Reader r = new FileReader(file)) {
            JsonArray arr = new JsonParser().parse(r).getAsJsonArray();
            for (JsonElement el : arr) {
                JsonObject obj = el.getAsJsonObject();
                String name = obj.get("name").getAsString();
                for (Module m : modules) {
                    if (m.getName().equals(name)) {
                        m.enabled = obj.get("enabled").getAsBoolean();
                        m.setKeybind(obj.get("keybind").getAsInt());

                        if (
                            m instanceof HudPositionable &&
                            obj.has("hudX") &&
                            obj.has("hudY")
                        ) {
                            ((HudPositionable) m).setHudX(
                                obj.get("hudX").getAsInt()
                            );
                            ((HudPositionable) m).setHudY(
                                obj.get("hudY").getAsInt()
                            );
                        }

                        for (ModuleOption<?> option : m.getOptions()) {
                            if (obj.has(option.getName())) {
                                String valueStr = obj
                                    .get(option.getName())
                                    .getAsString();
                                option.setValueFromString(valueStr);
                            }
                        }

                        break; // found the matching module, no need to keep searching
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
